package com.run.treadmill.activity.runMode.quickStart;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fitShow.treadmill.FsTreadmillCommand;
import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.summary.SummaryActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.MsgWhat;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowTreadmillManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.widget.HistogramListView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/11
 */
@CreatePresenter(QuickStartPresenter.class)
public class QuickStartActivity extends BaseRunActivity<QuickStartView, QuickStartPresenter> implements QuickStartView, FitShowTreadmillManager.FitShowRunningCallBack {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ErrorManager.getInstance().exitError) {
            finish();
            return;
        }
        if (ErrorManager.getInstance().isNoInclineError()) {
            finish();
            return;
        }
        quickToMedia = getIntent().getBooleanExtra(CTConstant.IS_MEDIA, false);

        if (quickToMedia) {
            String pkgName = getIntent().getStringExtra(CTConstant.PK_NAME);
            enterThirdApk(CTConstant.QUICKSTART, pkgName);
            rl_main.setVisibility(View.GONE);
        } else {
            mRunningParam.runStatus = CTConstant.RUN_STATUS_PREPARE;
        }

        btn_media = (TextView) findViewById(R.id.btn_media);
        btn_line_chart_incline = (TextView) findViewById(R.id.btn_line_chart_incline);
        btn_line_chart_speed = (TextView) findViewById(R.id.btn_line_chart_speed);
        img_unit = (ImageView) findViewById(R.id.img_unit);
        lineChartView = (HistogramListView) findViewById(R.id.lineChartView);
        lineChartView.setModeName(getString(R.string.string_mode_quick_start));
    }

    @Override
    public void fitShowStartRunning() {
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL && btn_start_stop_skip.isEnabled()) {
            btn_start_stop_skip.performClick();
        } else if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP && btn_pause_continue.isEnabled()) {
            btn_pause_continue.performClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (quickToMedia) {
            quickToMedia = false;
            mFloatWindowManager.regRxDataCallBackAgain();
        } else {
            FitShowTreadmillManager.getInstance().setFitShowRunningCallBack(this);
            if (mRunningParam.runStatus == CTConstant.RUN_STATUS_PREPARE) {
                FitShowTreadmillManager.getInstance().setRunStart(FsTreadmillCommand.STATUS_START);
            }
        }
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL) {
            btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_start);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //FitShowTreadmillManager.getInstance().setFitShowRunningCallBack(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (mRunningParam.runStatus != CTConstant.RUN_STATUS_PREPARE) {
            btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
        }
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP) {
            showPopTip();
        }
    }

    @Override
    public synchronized void click(View view) {
        super.click(view);
        switch (view.getId()) {
            case R.id.btn_incline_up:
                longClickBuzzer(btn_incline_up);
                myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                getPresenter().setInclineValue(1, 0, false);
                break;
            case R.id.btn_incline_down:
                longClickBuzzer(btn_incline_down);
                myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                getPresenter().setInclineValue(-1, 0, false);
                break;
            case R.id.btn_speed_up:
                longClickBuzzer(btn_speed_up);
                myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                getPresenter().setSpeedValue(1, 0, false);
                break;
            case R.id.btn_speed_down:
                longClickBuzzer(btn_speed_down);
                myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
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
                showMediaPopWin(CTConstant.QUICKSTART);
        }
    }

    @Override
    public void finishRunning() {
        super.finishRunning();
        FitShowTreadmillManager.getInstance().setRunStart(FsTreadmillCommand.STATUS_NORMAL);
        startActivity(new Intent(this, SummaryActivity.class));
        finish();
    }

    @Override
    public void afterPrepare() {
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_PREPARE) {
            mRunningParam.runStatus = CTConstant.RUN_STATUS_RUNNING;
            mRunningParam.setLcCurStageNum(0);
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
        refreshLineChart();
    }

    @Override
    public void onInclineChange(float incline) {
        tv_incline.setText(StringUtil.valueAndUnit(String.valueOf((int) incline), getString(R.string.string_unit_percent), runParamUnitTextSize));
        refreshLineChart();
    }

    @Override
    public void afterInclineChanged(float incline) {
        if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
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
    public void afterSpeedChanged(float speed) {
        if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
            return;
        }
        if (speed <= minSpeed) {
            if (btn_speed_down.isEnabled()) {
                btn_speed_down.setEnabled(false);
            }
            if (!btn_speed_up.isEnabled()) {
                btn_speed_up.setEnabled(true);
            }
        } else if (speed >= maxSpeed) {
            if (!btn_speed_down.isEnabled()) {
                btn_speed_down.setEnabled(true);
            }
            if (btn_speed_up.isEnabled()) {
                btn_speed_up.setEnabled(false);
            }
        } else {
            if (!btn_speed_down.isEnabled()) {
                btn_speed_down.setEnabled(true);
            }
            if (!btn_speed_up.isEnabled()) {
                btn_speed_up.setEnabled(true);
            }
        }
    }

    @Override
    protected void showPopTip() {
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP) {
            getPresenter().setSpeedValue(0, minSpeed, false);
            getPresenter().setInclineValue(0, 0, false);
            if (FitShowTreadmillManager.getInstance().isConnect()) {
                FitShowTreadmillManager.getInstance().sendPauseSpeedAndIncline();
            }
            FitShowTreadmillManager.getInstance().setRunStart(FsTreadmillCommand.STATUS_PAUSED);
        }
        super.showPopTip();
    }

    @Override
    public void hideTips() {

    }

    @Override
    public void fitShowSetSpeed(float speed) {
        if (mRunningParam.getCurrSpeed() == speed) {
            return;
        }
        if (isLineChartIncline) {
            btn_line_chart_speed.performClick();
        }

        getPresenter().setSpeedValue(0, speed, false);
    }

    @Override
    public void fitShowStopRunning() {
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING) {
            finishRunning();
        } else if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP) {
            btn_pause_quit.performClick();
        }

    }

    @Override
    public void fitShowPausedRunning() {
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING) {
            btn_start_stop_skip.performClick();
        }
    }

    @Override
    public void fitShowSetIncline(float incline) {
        if (mRunningParam.getCurrIncline() == incline) {
            return;
        }
        if (!isLineChartIncline) {
            btn_line_chart_incline.performClick();
        }
        getPresenter().setInclineValue(0, incline, false);
    }

    @Override
    protected void runCmdKeyValue(int keyValue) {
        switch (keyValue) {
            case SerialKeyValue.START_CLICK:
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                }
                if ((mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP)
                        && btn_pause_continue.isEnabled()) {
                    btn_pause_continue.performClick();
                }
                break;
            case SerialKeyValue.STOP_CLICK:
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP
                        && btn_pause_quit.isEnabled()) {
                    btn_pause_quit.performClick();
                    break;
                }
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    break;
                }
                break;
            case SerialKeyValue.STOP_CLICK_LONG_2:

                break;
            case SerialKeyValue.INCLINE_UP_CLICK:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_2:
                if (btn_incline_up.isEnabled()) {
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(1, 0, false);
                }
                break;
            case SerialKeyValue.INCLINE_DOWN_CLICK:
            case SerialKeyValue.INCLINE_DOWN_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_DOWN_CLICK_LONG_2:
                if (btn_incline_down.isEnabled()) {
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(-1, 0, false);
                }
                break;
            case SerialKeyValue.SPEED_UP_CLICK:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_2:
                if (btn_speed_up.isEnabled()) {
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(1, 0, false);
                }
                break;
            case SerialKeyValue.SPEED_DOWN_CLICK:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_2:
                if (btn_speed_down.isEnabled()) {
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(-1, 0, false);
                }
                break;
          /*  case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_2_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_5_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_10_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_15_CLICK:
                if (btn_incline_up.isEnabled() || btn_incline_down.isEnabled()) {
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;*/
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_10_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_14_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK:
                if (btn_speed_up.isEnabled() || btn_speed_down.isEnabled()) {
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FitShowTreadmillManager.getInstance().setFitShowRunningCallBack(null);
    }
}