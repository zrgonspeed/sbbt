package com.run.treadmill.activity.runMode.quickStart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chuhui.btcontrol.CbData;
import com.fitShow.treadmill.FitShowCommand;
import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.summary.SummaryActivity;
import com.run.treadmill.base.factory.CreatePresenter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.manager.fitshow.other.FitShowRunningCallBack;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.KeyUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.MsgWhat;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.widget.HistogramListView;

@CreatePresenter(QuickStartPresenter.class)
public class QuickStartActivity extends BaseRunActivity<QuickStartView, QuickStartPresenter> implements QuickStartView, FitShowRunningCallBack {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void fitShowStartRunning() {
        if (mRunningParam.isNormal() && btn_start_stop_skip.isEnabled()) {
            btn_start_stop_skip.performClick();
        } else if (mRunningParam.isStopStatus() && btn_pause_continue.isEnabled()) {
            btn_pause_continue.performClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!mRunningParam.isPrepare()) {
            btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
        }
        if (mRunningParam.isStopStatus()) {
            showPopTip();
        }
    }

    @Override
    public synchronized void click(View view) {
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
    public void finishRunning() {
        super.finishRunning();
        FitShowManager.getInstance().setRunStart(FitShowCommand.STATUS_NORMAL_0x00);
        FitShowManager.getInstance().beltStopping = true;
        startActivity(new Intent(this, SummaryActivity.class));
        finish();
    }

    @Override
    public void afterPrepare() {
        if (mRunningParam.isPrepare()) {
            mRunningParam.setToRunning();
            mRunningParam.setLcCurStageNum(0);
            mRunningParam.startRefreshData();

            // 设置运动秀APP 的程序模式 设定的速度扬升值。
            if (FitShowManager.getInstance().isProgramMode) {
                fitShowSetSpeed(FitShowManager.getInstance().targetSpeed);
                fitShowSetIncline(FitShowManager.getInstance().targetIncline);
                FitShowManager.getInstance().isProgramMode = false;
            }
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
        runGraph.afterSpeedChanged();
        // refreshLineChart();
    }

    @Override
    public void onInclineChange(float incline) {
        setIncline(StringUtil.valueAndUnit(String.valueOf((int) incline), getString(R.string.string_unit_percent), runParamUnitTextSize));
        // refreshLineChart();
        runGraph.afterInclineChanged();

    }


    @Override
    public void showPopTip() {
        if (mRunningParam.isStopStatus()) {
            // getPresenter().setSpeedValue(0, minSpeed, false);
            // getPresenter().setInclineValue(0, 0, false);
            if (FitShowManager.getInstance().isConnect()) {
                FitShowManager.getInstance().isBeforePauseSendZero = true;
            }
            ThreadUtils.runInThread(() -> {
                FitShowManager.getInstance().isBeforePauseSendZero = false;
                FitShowManager.getInstance().setRunStart(FitShowCommand.STATUS_PAUSED_0x0A);
            }, 600);
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
        } else {
            BuzzerManager.getInstance().buzzerRingOnce();
        }

        getPresenter().setSpeedValue(0, speed, false);
    }

    @Override
    public void fitShowStopRunning() {
        if (mRunningParam.isRunning()) {
            ControlManager.getInstance().resetIncline();
            finishRunning();
        } else if (mRunningParam.isStopStatus()) {
            btn_pause_quit.performClick();
        }

    }

    @Override
    public void fitShowPausedRunning() {
        if (mRunningParam.isRunning()) {
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
        } else {
            BuzzerManager.getInstance().buzzerRingOnce();
        }
        getPresenter().setInclineValue(0, incline, false);
    }

    @Override
    public void onDataCallback(CbData data) {
        if (data.dataType == CbData.TYPE_INCLINE) {
            int incline = (int) data.inclien;
            Logger.i("onDataCallback " + incline);
            fitShowSetIncline(incline);
        }
    }

    @Override
    public void runCmdKeyValue(int keyValue) {
        if (KeyUtils.isInclineKeyAndHasInclineError(keyValue)) {
            return;
        }
        if (KeyUtils.isStopSetSpeed(keyValue) || KeyUtils.isStopSetIncline(keyValue)) {
            return;
        }
        switch (keyValue) {
            case SerialKeyValue.HAND_START_CLICK:
            case SerialKeyValue.START_CLICK:
                if (mRunningParam.isNormal()
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                if ((mRunningParam.isStopStatus())
                        && btn_pause_continue.isEnabled()) {
                    btn_pause_continue.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                break;
            case SerialKeyValue.HAND_STOP_CLICK:
            case SerialKeyValue.STOP_CLICK:
                if (mRunningParam.isStopStatus()
                        && btn_pause_quit.isEnabled()) {
                    btn_pause_quit.performClick();
//                    BuzzerManager.getInstance().buzzerRingOnce();
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
                if (btn_incline_up.isEnabled()) {
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
                if (btn_incline_down.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(-1, 0, false);
                }
                break;
            case SerialKeyValue.SPEED_UP_CLICK:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_2:
            case SerialKeyValue.SPEED_UP_HAND_CLICK:
            case SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_2:
                if (btn_speed_up.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(1, 0, false);
                }
                break;
            case SerialKeyValue.SPEED_DOWN_CLICK:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_2:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_2:
                if (btn_speed_down.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(-1, 0, false);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_2_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK:

                if (btn_incline_up.isEnabled() || btn_incline_down.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_15_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK:
                if (btn_speed_up.isEnabled() || btn_speed_down.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
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
        FitShowManager.getInstance().setFitShowRunningCallBack(null);
    }
}