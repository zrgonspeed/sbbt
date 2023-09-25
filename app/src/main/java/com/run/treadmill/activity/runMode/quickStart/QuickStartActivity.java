package com.run.treadmill.activity.runMode.quickStart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.fitShow.treadmill.FsTreadmillCommand;
import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.summary.SummaryActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.common.MsgWhat;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowTreadmillManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.KeyUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.widget.HistogramListView;

import java.util.Arrays;

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
        long start = System.currentTimeMillis();
        {
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
        long end = System.currentTimeMillis();
        Logger.i("QuickStartActivity onResume() time == " + (end - start));
    }

    @Override
    protected void onPause() {
        long start = System.currentTimeMillis();
        super.onPause();
        //FitShowTreadmillManager.getInstance().setFitShowRunningCallBack(null);
        long end = System.currentTimeMillis();
        Logger.i("QuickStartActivity onPause() time == " + (end - start));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
        FitShowTreadmillManager.getInstance().beltStopping = true;
        startActivity(new Intent(this, SummaryActivity.class));
        finish();
    }

    @Override
    public void afterPrepare() {
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_PREPARE) {
            mRunningParam.runStatus = CTConstant.RUN_STATUS_RUNNING;
            Logger.e(TAG, Arrays.toString(mRunningParam.mInclineArray));

            mRunningParam.setLcCurStageNum(0);
            mRunningParam.startRefreshData();

            // 设置运动秀APP 的程序模式 设定的速度扬升值。
            if (FitShowTreadmillManager.getInstance().isProgramMode) {
                fitShowSetSpeed(FitShowTreadmillManager.getInstance().targetSpeed);
                fitShowSetIncline(FitShowTreadmillManager.getInstance().targetIncline);
                FitShowTreadmillManager.getInstance().isProgramMode = false;
            }
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
        tv_incline.setText(StringUtil.valueAndUnit(String.valueOf((int) incline + InitParam.MY_MIN_INCLINE), getString(R.string.string_unit_percent), runParamUnitTextSize));
        refreshLineChart();
    }

    @Override
    public void afterInclineChanged(float incline) {
        if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
            return;
        }
        if (incline <= -5) {
            if (btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(false);
            }
            if (!btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(true);
            }
        } else if (incline >= maxIncline + minIncline) {
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
            getPresenter().setInclineValue(0, -InitParam.MY_MIN_INCLINE, false);
            if (FitShowTreadmillManager.getInstance().isConnect()) {
                int incline = 0;
                try {
                    incline = (int) mFloatWindowManager.mRunningParam.getCurrIncline();
                }catch (Exception e) {
                    incline = 0;
                }
                // FitShowTreadmillManager.getInstance().sendPauseSpeedAndIncline(0, incline);
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
        } else {
            BuzzerManager.getInstance().buzzerRingOnce();
        }

        getPresenter().setSpeedValue(0, speed, false);
    }

    @Override
    public void fitShowStopRunning() {
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING) {
            ControlManager.getInstance().resetIncline();
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

        if (incline > maxIncline || incline < InitParam.MY_MIN_INCLINE) {
            return;
        }
        if (ErrorManager.getInstance().isHasInclineError()) {
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
    protected void runCmdKeyValue(int keyValue) {
        if (KeyUtils.isInclineKeyAndHasInclineError(keyValue)) {
            return;
        }
        if (KeyUtils.isStopSetSpeed(keyValue) || KeyUtils.isStopSetIncline(keyValue)) {
            return;
        }
        switch (keyValue) {
            case SerialKeyValue.HAND_START_CLICK:
            case SerialKeyValue.START_CLICK:
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                if ((mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP)
                        && btn_pause_continue.isEnabled()) {
                    btn_pause_continue.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                break;
            case SerialKeyValue.HAND_STOP_CLICK:
            case SerialKeyValue.STOP_CLICK:
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP
                        && btn_pause_quit.isEnabled()) {
                    btn_pause_quit.performClick();
//                    BuzzerManager.getInstance().buzzerRingOnce();
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
            case SerialKeyValue.INCLINE_UP_CLICK:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_2:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_2:
                if (btn_incline_up.isEnabled()) {
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
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
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
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
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
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
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(-1, 0, false);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_3_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_9_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK:

                if (btn_incline_up.isEnabled() || btn_incline_down.isEnabled()) {
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_3_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_9_CLICK:
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