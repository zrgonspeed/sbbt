package com.run.treadmill.activity.runMode.fitness;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.summary.SummaryActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.widget.HistogramListView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/03
 */
@CreatePresenter(FitnessTestPresenter.class)
public class FitnessTestActivity extends BaseRunActivity<FitnessTestView, FitnessTestPresenter> implements FitnessTestView {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRunningParam.runStatus = CTConstant.RUN_STATUS_PREPARE;

        btn_media = (TextView) findViewById(R.id.btn_media);
        btn_line_chart_incline = (TextView) findViewById(R.id.btn_line_chart_incline);
        btn_line_chart_speed = (TextView) findViewById(R.id.btn_line_chart_speed);
        img_unit = (ImageView) findViewById(R.id.img_unit);
        lineChartView = (HistogramListView) findViewById(R.id.lineChartView);

        lineChartView.setModeName(getString(R.string.string_mode_fitness));
    }

    @Override
    protected void onResume() {
        super.onResume();
        btn_incline_down.setEnabled(false);
        btn_incline_up.setEnabled(false);
        btn_incline_roller.setEnabled(false);
        btn_speed_down.setEnabled(false);
        btn_speed_up.setEnabled(false);
        btn_speed_roller.setEnabled(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP || mRunningParam.runStatus == CTConstant.RUN_STATUS_COOL_DOWN) {
            showPopTip();
        }
    }

    @Override
    protected void showPopTip() {
        if (ErrorManager.getInstance().isHasInclineError()) {
            showInclineError();
        } else {
            tv_incline.setText(StringUtil.valueAndUnit("0", getString(R.string.string_unit_percent), runParamUnitTextSize));
        }
        super.showPopTip();
    }

    @Override
    public void dataCallback() {
        super.dataCallback();
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_WARM_UP && mRunningParam.getWarmUpTime() == 0) {
            warmUpToRunning();
        }
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_COOL_DOWN && mRunningParam.getCoolDownTime() == 0) {
            btn_start_stop_skip.performClick();
        }
    }

    @Override
    public void click(View view) {
        super.click(view);
        switch (view.getId()) {
            case R.id.btn_line_chart_incline:
                if (isLineChartIncline) {
                    break;
                }
                isLineChartIncline = true;

                settingLineChart();
                refreshLineChart();
                break;
            case R.id.btn_line_chart_speed:
                if (!isLineChartIncline) {
                    break;
                }
                isLineChartIncline = false;

                settingLineChart();
                refreshLineChart();
                break;
            case R.id.btn_media:
                showMediaPopWin(CTConstant.FITNESS_TEST);
                break;
            default:
                break;
        }
    }

    @Override
    public void finishRunning() {
        super.finishRunning();
        ControlManager.getInstance().resetIncline();
        Intent intent = new Intent(this, SummaryActivity.class);
        intent.putExtra(CTConstant.NEED_VO2, true);
        startActivity(intent);
        finish();
    }

    @Override
    public void afterPrepare() {
        btn_speed_roller.setEnabled(false);
        btn_incline_roller.setEnabled(false);
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_PREPARE) {
            mRunningParam.runStatus = CTConstant.RUN_STATUS_WARM_UP;
            showPopTip();

            mRunningParam.startRefreshData();
        }
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_CONTINUE) {
            mRunningParam.runStatus = CTConstant.RUN_STATUS_RUNNING;
            mRunningParam.notifyRefreshData();
        }
        ControlManager.getInstance().startRun();
    }

    @Override
    public void onSpeedChange(float speed) {
        tv_speed.setText(getSpeedValue(String.valueOf(speed)));
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING) {
            refreshLineChart();
        }
    }

    @Override
    public void onInclineChange(float incline) {
        tv_incline.setText(StringUtil.valueAndUnit(String.valueOf((int) incline + InitParam.MY_MIN_INCLINE), getString(R.string.string_unit_percent), runParamUnitTextSize));
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING) {
            refreshLineChart();
        }
    }

    @Override
    public void afterInclineChanged(float incline) {
    }

    @Override
    public void afterSpeedChanged(float speed) {
    }

    @Override
    public void hideTips() {
    }

    @Override
    protected void runCmdKeyValue(int keyValue) {
        if (rl_tip.getVisibility() == View.VISIBLE) {
            return;
        }
        switch (keyValue) {
            case SerialKeyValue.HAND_START_CLICK:
            case SerialKeyValue.START_CLICK:
                if ((mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP)
                        && btn_pause_continue.isEnabled()) {
                    btn_pause_continue.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_WARM_UP
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                break;
            case SerialKeyValue.HAND_STOP_CLICK:
            case SerialKeyValue.STOP_CLICK:
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_COOL_DOWN
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP
                        && btn_pause_quit.isEnabled()) {
                    btn_pause_quit.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                break;
            case SerialKeyValue.STOP_CLICK_LONG_2:

                break;
            default:
                break;
        }

    }

    @Override
    public void showNoPulsePop() {
        hideMediaPopWin();
        if (rl_tip.getVisibility() == View.GONE) {
            rl_tip.setVisibility(View.VISIBLE);
        }
        tv_tip.setText(R.string.string_running_no_pulse);
    }

    @Override
    public void hidePulseTip() {
        if (rl_tip.getVisibility() == View.VISIBLE) {
            rl_tip.setVisibility(View.GONE);
        }
    }
}