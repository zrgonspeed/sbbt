package com.run.treadmill.activity.runMode.help;

import android.view.View;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.activity.runMode.StepManager;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.mcu.control.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.util.FormulaUtil;
import com.run.treadmill.util.ResourceUtils;
import com.run.treadmill.util.StringUtil;

public class RunRefresh {
    private BaseRunActivity activity;
    private RunningParam mRunningParam;

    public RunRefresh(BaseRunActivity baseRunActivity) {
        this.activity = baseRunActivity;
    }

    public void onResumeInitRunParam() {
        this.mRunningParam = activity.mRunningParam;

        if (StepManager.showStep) {
            activity.tv_setnum.setVisibility(View.VISIBLE);
        } else {
            activity.tv_setnum.setVisibility(View.GONE);
        }
        activity.btn_pause_continue.setEnabled(false);

        activity.iv_run_profile.setSelected(true);
        activity.btn_incline_down.setIntervalTime(110);
        activity.btn_incline_up.setIntervalTime(110);
        activity.btn_speed_down.setIntervalTime(110);
        activity.btn_speed_up.setIntervalTime(110);

        activity.btn_incline_down.setTag(-1);
        activity.btn_incline_up.setTag(-1);
        activity.btn_speed_down.setTag(-1);
        activity.btn_speed_up.setTag(-1);

        activity.btn_start_stop_skip.setOnLongClickListener(v -> true);
        activity.btn_pause_continue.setOnLongClickListener(v -> true);
        activity.btn_pause_quit.setOnLongClickListener(v -> true);

        if (mRunningParam.isNormal()) {
            activity.btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_start);
        } else if (!mRunningParam.isCoolDownStatus()) {
            activity.btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
        }

        mRunningParam.currSpeedInx = FormulaUtil.getInxBySpeed(mRunningParam.getCurrSpeed(), activity.minSpeed);

        activity.setTextWatcher();

        if (mRunningParam.isPrepare() || mRunningParam.isNormal()) {
            if (ErrorManager.getInstance().isHasInclineError()) {
                activity.runError.showInclineError();
            } else {
                activity.setIncline(StringUtil.valueAndUnit("0", ResourceUtils.getString(R.string.string_unit_percent), activity.runParamUnitTextSize));
            }
            activity.setSpeed(activity.getSpeedValue("0.0"));
        } else {
            if (ErrorManager.getInstance().isHasInclineError()) {
                activity.runError.showInclineError();
            } else if (!mRunningParam.isCoolDownStatus()) {
                activity.setIncline(StringUtil.valueAndUnit(String.valueOf((int) mRunningParam.getCurrIncline()), ResourceUtils.getString(R.string.string_unit_percent), activity.runParamUnitTextSize));
            }
            activity.setSpeed(activity.getSpeedValue(String.valueOf(mRunningParam.getCurrSpeed())));
        }
        refreshRunParam();
    }

    public void refreshRunParam() {
        checkStop();

        activity.tv_time.setText(mRunningParam.getShowTime());
        activity.tv_distance.setText(activity.getDistanceValue(mRunningParam.getShowDistance()));
        activity.tv_calories.setText(StringUtil.valueAndUnit(mRunningParam.getShowCalories(), ResourceUtils.getString(R.string.string_unit_kcal), activity.runParamUnitTextSize));
        activity.tv_pulse.setText(mRunningParam.getShowPulse());
        activity.tv_mets.setText(mRunningParam.getShowMets());
        activity.tv_setnum.setText(String.valueOf(mRunningParam.stepManager.getCurStep()));

        if (activity.lineChartView != null) {
            activity.refreshLineChart();
        }
        // WifiBTStateManager.setBTWifiStatus(activity.img_wifi, activity.img_bt, activity);

        pulseAnimation();
    }

    private void pulseAnimation() {
        if (Integer.parseInt(mRunningParam.getShowPulse()) <= 0) {
            if (activity.img_pulse.getAnimation() != null && activity.img_pulse.getAnimation().hasStarted()) {
                activity.img_pulse.clearAnimation();
            }
            return;
        }
        if (mRunningParam.isRunning() || mRunningParam.isWarmStatus()
                || mRunningParam.isCoolDownStatus()) {
            if (activity.img_pulse.getAnimation() == null) {
                activity.img_pulse.startAnimation(activity.pulseAnimation);
            }
        }
    }

    private void checkStop() {
        this.mRunningParam = activity.mRunningParam;

        if (mRunningParam.stepManager.isStopRunning) {
            if (mRunningParam.isWarmStatus()) {
                BuzzerManager.getInstance().buzzerRingOnce();
                activity.btn_pause_quit.setEnabled(false);
                if (activity.mVideoPlayerSelf != null) {
                    activity.mVideoPlayerSelf.onRelease();
                }
                activity.runPause.stopPauseTimer();
                activity.finishRunning();
            } else {
                activity.btn_start_stop_skip.performClick();
                mRunningParam.stepManager.clean();
            }
            ControlManager.getInstance().resetIncline();
        }
    }
}
