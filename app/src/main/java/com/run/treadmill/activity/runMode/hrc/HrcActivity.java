package com.run.treadmill.activity.runMode.hrc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.summary.SummaryActivity;
import com.run.treadmill.util.MsgWhat;
import com.run.treadmill.base.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.mcu.control.ControlManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.KeyUtils;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.widget.HistogramListView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/03
 */
@CreatePresenter(HrcPresenter.class)
public class HrcActivity extends BaseRunActivity<HrcView, HrcPresenter> implements HrcView {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRunningParam.setToPrepare();

        btn_media = (TextView) findViewById(R.id.btn_media);
        btn_line_chart_incline = (TextView) findViewById(R.id.btn_line_chart_incline);
        btn_line_chart_speed = (TextView) findViewById(R.id.btn_line_chart_speed);
        img_unit = (ImageView) findViewById(R.id.img_unit);
        lineChartView = (HistogramListView) findViewById(R.id.lineChartView);
        lineChartView.setModeName(getString(R.string.string_mode_hrc));
    }

    @Override
    protected void onResume() {
        super.onResume();
        btn_speed_down.setEnabled(false);
        btn_speed_up.setEnabled(false);
        btn_speed_roller.setEnabled(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mRunningParam.isStopStatus() || mRunningParam.isCoolDownStatus()) {
            showPopTip();
        }
    }

    @Override
    public void dataCallback() {
        super.dataCallback();
        if (mRunningParam.isCoolDownStatus() && mRunningParam.getCoolDownTime() == 0) {
            btn_start_stop_skip.performClick();
        }
    }

    @Override
    public void click(View view) {
        super.click(view);
        switch (view.getId()) {
            case R.id.btn_incline_up:
                
                speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                getPresenter().setInclineValue(1, 0, false);
                break;
            case R.id.btn_incline_down:
                
                speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                getPresenter().setInclineValue(-1, 0, false);
                break;
            case R.id.btn_speed_up:
                
                speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                getPresenter().setSpeedValue(1, 0, false);
                break;
            case R.id.btn_speed_down:
                
                speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                getPresenter().setSpeedValue(-1, 0, false);
                break;
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
        }
    }

    @Override
    public void showNoPulse() {
        if (rl_tip.getVisibility() == View.GONE) {
            rl_tip.setVisibility(View.VISIBLE);
            btn_incline_down.setPressed(false);
            btn_incline_up.setPressed(false);
        }
        tv_tip.setText(R.string.string_running_no_pulse);
    }

    @Override
    public void showOverPulse() {
        if (rl_tip.getVisibility() == View.GONE) {
            rl_tip.setVisibility(View.VISIBLE);
        }
        tv_tip.setText(R.string.string_running_over_pulse);
    }

    @Override
    public void hidePulseTip() {
        if (rl_tip.getVisibility() == View.VISIBLE) {
            rl_tip.setVisibility(View.GONE);
        }
    }

    @Override
    public void changeSpeedByPulse(float speed) {
        if ((mRunningParam.getCurrSpeed() + speed) > maxSpeed) {
            getPresenter().setSpeedValue(0, maxSpeed, false);
        } else if ((mRunningParam.getCurrSpeed() + speed) < minSpeed) {
            getPresenter().setSpeedValue(0, minSpeed, false);
        } else {
            getPresenter().setSpeedValue(0, mRunningParam.getCurrSpeed() + speed, false);
        }
    }

    @Override
    public void finishRunning() {
        super.finishRunning();
        // ControlManager.getInstance().resetIncline();
        startActivity(new Intent(this, SummaryActivity.class));
        finish();
    }


    @Override
    public void afterPrepare() {
        btn_speed_roller.setEnabled(false);
        if (mRunningParam.isPrepare()) {
            mRunningParam.setToRunning();
            mRunningParam.setLcCurStageNum(0);
            mRunningParam.startRefreshData();
        }
        if (mRunningParam.isContinue()) {
            mRunningParam.setToRunning();
            mRunningParam.notifyRefreshData();
        }

        ControlManager.getInstance().startRun();
    }

    @Override
    public void onSpeedChange(float speed) {
        setSpeed(getSpeedValue(String.valueOf(speed)));
        refreshLineChart();
    }

    @Override
    public void onInclineChange(float incline) {
        setIncline(StringUtil.valueAndUnit(String.valueOf((int) incline), getString(R.string.string_unit_percent), runParamUnitTextSize));
        refreshLineChart();
    }

    @Override
    public void afterInclineChanged(float incline) {
        if (isCalcDialogShowing()
                || mRunningParam.isWarmStatus()
                || mRunningParam.isCoolDownStatus()) {
            return;
        }
        if (incline <= 0) {
            if (btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(false);
            }
            if (!btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(true);
            }
        } else if (incline >= maxIncline) {
            if (!btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(true);
            }
            if (btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(false);
            }
        } else {
            if (!btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(true);
            }
            if (!btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(true);
            }
        }
    }

    @Override
    public void showPopTip() {
        if (mRunningParam.isStopStatus()) {
            // getPresenter().setSpeedValue(0, minSpeed, false);
            // getPresenter().setInclineValue(0, 0, false);
        }
        super.showPopTip();
    }

    @Override
    public void afterSpeedChanged(float speed) {

    }

    @Override
    public void hideTips() {

    }

    @Override
    public void runCmdKeyValue(int keyValue) {
        if (rl_tip.getVisibility() == View.VISIBLE) {
            return;
        }
        if (KeyUtils.isInclineKeyAndHasInclineError(keyValue)) {
            return;
        }
        if (KeyUtils.isStopSetSpeed(keyValue) || KeyUtils.isStopSetIncline(keyValue)) {
            return;
        }
        switch (keyValue) {
            case SerialKeyValue.HAND_START_CLICK:
            case SerialKeyValue.START_CLICK:
                if ((mRunningParam.isStopStatus())
                        && btn_pause_continue.isEnabled()) {
                    btn_pause_continue.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                break;
            case SerialKeyValue.HAND_STOP_CLICK:
            case SerialKeyValue.STOP_CLICK:
                if (mRunningParam.isCoolDownStatus()
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                if (mRunningParam.isStopStatus()
                        && btn_pause_quit.isEnabled()) {
                    btn_pause_quit.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                if (mRunningParam.isRunning()
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                break;
            case SerialKeyValue.STOP_CLICK_LONG_2:

                break;
            case SerialKeyValue.INCLINE_UP_CLICK:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_2:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_2:
                if (btn_incline_up.isEnabled() && rl_tip.getVisibility() == View.GONE) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(1, 0, false);
                }
                break;
            case SerialKeyValue.INCLINE_DOWN_CLICK:
            case SerialKeyValue.INCLINE_DOWN_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_DOWN_CLICK_LONG_2:
            case SerialKeyValue.INCLINE_DOWN_HAND_CLICK:
            case SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_2:
                if (btn_incline_down.isEnabled() && rl_tip.getVisibility() == View.GONE) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(-1, 0, false);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_2_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK:

                if ((btn_incline_up.isEnabled() || btn_incline_down.isEnabled()) && rl_tip.getVisibility() == View.GONE) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;
            default:
                break;
        }
    }

}