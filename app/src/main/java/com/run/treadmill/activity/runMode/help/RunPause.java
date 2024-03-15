package com.run.treadmill.activity.runMode.help;

import android.view.View;

import com.run.treadmill.R;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ResourceUtils;
import com.run.treadmill.util.StringUtil;
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

    public void showPopTip() {
        RunningParam mRunningParam = activity.mRunningParam;

        if (activity.isCalcDialogShowing()) {
            activity.mCalcBuilder.stopPopWin();
        }
        if (mRunningParam.isQuickToSummary) {
            activity.rl_main.setVisibility(View.GONE);
            activity.setControlEnable(false);
            activity.btn_pause_continue.setEnabled(false);
            activity.btn_pause_quit.setEnabled(false);
            if (activity.mVideoPlayerSelf != null) {
                activity.mVideoPlayerSelf.onRelease();
            }
            stopPauseTimer();
            activity.finishRunning();
            return;
        }
        mRunningParam.recodePreRunData();
        if (mRunningParam.isStopStatus()) {
            activity.btn_pause_continue.setEnabled(false);
            activity.tv_speed.setText(activity.getSpeedValue(String.valueOf(0.0f)));
            activity.img_run_pop_tip.setImageResource(R.drawable.img_pop_pause);
            activity.rl_mask.setVisibility(View.VISIBLE);
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
        } else if (mRunningParam.isWarmStatus()) {
            activity.img_run_pop_tip.setImageResource(R.drawable.img_pop_warmup);
            activity.btn_start_stop_skip.setImageResource(R.drawable.btn_skip_warmup);
            activity.rl_center_tip.setVisibility(View.VISIBLE);
            activity.btn_media.setEnabled(false);
            activity.runMedia.dismissPopWin();

            mRunningParam.setCurrIncline(InitParam.WARM_UP_INCLIEN);
            mRunningParam.setCurrSpeed(activity.isMetric ? InitParam.WARM_UP_SPEED_METRIC : InitParam.WARM_UP_SPEED_IMPERIAL);
            activity.onSpeedChange(mRunningParam.getCurrSpeed());
            if (ErrorManager.getInstance().isHasInclineError()) {
                activity.runError.showInclineError();
            } else {
                activity.setIncline(StringUtil.valueAndUnit("0", ResourceUtils.getString(R.string.string_unit_percent), activity.runParamUnitTextSize));
            }
        } else if (mRunningParam.isCoolDownStatus()) {
            activity.img_run_pop_tip.setImageResource(R.drawable.img_pop_cooldown);
            activity.btn_start_stop_skip.setImageResource(R.drawable.btn_skip_cooldown);
            activity.rl_center_tip.setVisibility(View.VISIBLE);
            activity.tv_time.setText(TimeStringUtil.getMsToMinSecValue(mRunningParam.getCoolDownTime() * 1000f));
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
}
