package com.run.treadmill.activity.runMode.help;

import android.view.View;

import com.run.treadmill.AppDebug;
import com.run.treadmill.R;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ResourceUtils;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.util.TimeStringUtil;

public class RunPause implements CustomTimer.TimerCallBack {
    private BaseRunActivity activity;

    public RunPause(BaseRunActivity baseRunActivity) {
        this.activity = baseRunActivity;
    }

    private long PAUSE_TIME = 3 * 1000 * 60;
    private final String pauseTimerTag = "pauseTimerTag";
    private CustomTimer pauseTimer;

    public void startPauseTimer() {
        if (pauseTimer == null) {
            pauseTimer = new CustomTimer();
            pauseTimer.setTag(pauseTimerTag);
        }
        pauseTimer.closeTimer();
        pauseTimer.startTimer(PAUSE_TIME, this);
    }

    public void stopPauseTimer() {
        if (pauseTimer != null) {
            pauseTimer.closeTimer();
        }
    }

    @Override
    public void timerComply(long lastTime, String tag) {
        Logger.d(tag + "=== 定时器回调 ===>   " + lastTime);
        if (tag.equals(pauseTimerTag)) {
            if (activity.mRunningParam.isStopStatus()) {
                activity.runOnUiThread(() -> {
                    activity.btn_pause_quit.performClick();
                    stopPauseTimer();
                });
            }
        }
    }

    public void clickPause() {
        if (activity.btn_home.getVisibility() == View.VISIBLE) {
            activity.btn_home.setVisibility(View.GONE);
        }
        if (activity.mRunningParam.isNormal()) {
            activity.run_pop_pause.setVisibility(View.GONE);

            activity.btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
            activity.mRunningParam.setToPrepare();
            activity.showPrepare(0);
            return;
        } else if (activity.mRunningParam.isStopStatus()
                || activity.mRunningParam.isPrepare()) {
            return;
        } else if (activity.mRunningParam.isWarmStatus()) {
            BuzzerManager.getInstance().buzzerRingOnce();
            activity.warmUpToRunning();
        } else if (activity.mRunningParam.isCoolDownStatus()) {
            BuzzerManager.getInstance().buzzerRingOnce();
            activity.btn_start_stop_skip.setEnabled(false);
            activity.finishRunning();
        } else {
            // 进入暂停状态
            Logger.i("进入暂停状态");
            activity.disFlag = true;
            // Logger.i("disFlag = true");
            ThreadUtils.runInThread(() -> {
                activity.disFlag = false;
                // Logger.i("disFlag = false");

                if (AppDebug.debug) {
                    if (!activity.isDestroyed()) {
                        activity.runOnUiThread(() -> {
                            activity.btn_pause_continue.setEnabled(true);
                        });
                    }
                }
            }, 1000);

            BuzzerManager.getInstance().buzzerRingOnce();
            activity.mRunningParam.setToStopStatus();
            activity.btn_pause_continue.setEnabled(false);
            // gsMode默认false
            // 客户要求修改扬升机制
            ControlManager.getInstance().stopRun(activity.gsMode);
            // ControlManager.getInstance().resetIncline();

            if (activity.mVideoPlayerSelf != null) {
                activity.mVideoPlayerSelf.videoPlayerStartPause();
            }
        }
    }

    public void showPopTip() {
        RunningParam mRunningParam = activity.mRunningParam;

        if (activity.isCalcDialogShowing()) {
            activity.mCalcBuilder.stopPopWin();
        }

        if (mRunningParam.isQuickToSummary) {
            quickToSummary();
            return;
        }
        mRunningParam.recodePreRunData();
        if (mRunningParam.isStopStatus()) {
            enterPause();
        } else if (mRunningParam.isWarmStatus()) {
            enterWarm();
        } else if (mRunningParam.isCoolDownStatus()) {
            enterCoolDown();
        }
    }

    private void enterPause() {
        activity.btn_pause_continue.setEnabled(false);
        activity.setSpeed(activity.getSpeedValue(String.valueOf(0.0f)));
        activity.img_run_pop_tip.setImageResource(R.drawable.img_pop_pause);
        activity.run_pop_pause.setVisibility(View.VISIBLE);
        //暂停倒计时
        startPauseTimer();
        activity.setControlEnable(false);
        activity.btn_incline_roller.setEnabled(false);
        activity.btn_speed_roller.setEnabled(false);
        //停止心跳动画
        if (activity.img_pulse.getAnimation() != null && activity.img_pulse.getAnimation().hasStarted()) {
            activity.img_pulse.clearAnimation();
        }
        if (activity.rl_tip.getVisibility() == View.VISIBLE) {
            activity.rl_tip.setVisibility(View.GONE);
        }
        activity.runMedia.dismissPopWin();
    }

    private void quickToSummary() {
        activity.rl_main.setVisibility(View.GONE);
        activity.setControlEnable(false);
        activity.btn_pause_continue.setEnabled(false);
        activity.btn_pause_quit.setEnabled(false);
        if (activity.mVideoPlayerSelf != null) {
            activity.mVideoPlayerSelf.onRelease();
        }
        stopPauseTimer();
        activity.finishRunning();
    }

    private void enterWarm() {
        activity.img_run_pop_tip.setImageResource(R.drawable.img_pop_warmup);
        activity.btn_start_stop_skip.setImageResource(R.drawable.btn_skip_warmup);
        activity.rl_center_tip.setVisibility(View.VISIBLE);
        activity.btn_media.setEnabled(false);
        activity.runMedia.dismissPopWin();

        activity.mRunningParam.setCurrIncline(InitParam.WARM_UP_INCLIEN);
        activity.mRunningParam.setCurrSpeed(activity.isMetric ? InitParam.WARM_UP_SPEED_METRIC : InitParam.WARM_UP_SPEED_IMPERIAL);
        activity.onSpeedChange(activity.mRunningParam.getCurrSpeed());
        if (ErrorManager.getInstance().isHasInclineError()) {
            activity.runError.showInclineError();
        } else {
            activity.setIncline(StringUtil.valueAndUnit("0", ResourceUtils.getString(R.string.string_unit_percent), activity.runParamUnitTextSize));
        }
    }

    private void enterCoolDown() {
        activity.img_run_pop_tip.setImageResource(R.drawable.img_pop_cooldown);
        activity.btn_start_stop_skip.setImageResource(R.drawable.btn_skip_cooldown);
        activity.rl_center_tip.setVisibility(View.VISIBLE);
        activity.tv_time.setText(TimeStringUtil.getMsToMinSecValue(activity.mRunningParam.getCoolDownTime() * 1000f));
        if (activity.btn_media != null) {
            activity.btn_media.setEnabled(false);
        }
        activity.runMedia.hideMediaPopWin();
        if (activity.rl_tip.getVisibility() == View.VISIBLE) {
            activity.rl_tip.setVisibility(View.GONE);
        }
        //进入cool down要归零
        if (!ErrorManager.getInstance().isHasInclineError()) {
            activity.setIncline(StringUtil.valueAndUnit("0", ResourceUtils.getString(R.string.string_unit_percent), activity.runParamUnitTextSize));
            ControlManager.getInstance().resetIncline();
        }
    }

}
