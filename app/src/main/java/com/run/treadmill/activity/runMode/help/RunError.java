package com.run.treadmill.activity.runMode.help;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.runMode.BaseRunPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.util.DataTypeConversion;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ResourceUtils;

public class RunError {
    private BaseRunActivity activity;

    public RunError(BaseRunActivity baseRunActivity) {
        this.activity = baseRunActivity;
    }

    public BaseRunPresenter getPresenter() {
        return (BaseRunPresenter) activity.getPresenter();
    }

    public void showInclineError() {
        RunningParam mRunningParam = activity.mRunningParam;

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            if (activity.tv_incline.getText().toString().equals("E5")) {
                return;
            }
            activity.tv_incline.setText("E5");
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            if (activity.tv_incline.getText().toString().equals("E5")) {
                return;
            }
            activity.tv_incline.setText("E5");
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            if (activity.tv_incline.getText().toString().equals(String.format("E%s", DataTypeConversion.intLowToByte(ErrorManager.ERR_INCLINE_ADJUST)))) {
                return;
            }
            activity.tv_incline.setText(String.format("E%s", DataTypeConversion.intLowToByte(ErrorManager.ERR_INCLINE_ADJUST)));
        }
        mRunningParam.setInclineError();
        activity.tv_incline.setTextColor(ResourceUtils.getColor(R.color.red));
        activity.txt_running_incline_ctrl.setTextColor(ResourceUtils.getColor(R.color.red));
        activity.txt_running_incline_param.setTextColor(ResourceUtils.getColor(R.color.red));
        if (activity.isCalcDialogShowing()) {
            activity.mCalcBuilder.stopPopWin();
        }
    }

    public void safeError() {
        RunningParam mRunningParam = activity.mRunningParam;

        activity.prepare321Go.safeError();
        //出现安全key时扬升处理动作
        ControlManager.getInstance().stopRun(activity.gsMode);
        if (mRunningParam != null) {
            mRunningParam.interrupted();
            mRunningParam.recodePreRunData();
        }
        //如果速度感应线从一开始就没插好，常态包速度一直返回0，并且处于非stop状态，则取最后下发的速度
        getPresenter().checkLastSpeedOnRunning(activity.isMetric);
        if (activity.currentPro != -1) {
            //音量恢复
            restoreVolume();
        }
    }

    public void commOutError() {
        RunningParam mRunningParam = activity.mRunningParam;

        activity.prepare321Go.commOutError();
        if (mRunningParam != null) {
            mRunningParam.interrupted();
            mRunningParam.recodePreRunData();
        }
        //如果速度感应线从一开始就没插好，常态包速度一直返回0，并且处于非stop状态，则取最后下发的速度
        getPresenter().checkLastSpeedOnRunning(activity.isMetric);
        //音量恢复
        restoreVolume();
    }

    public void showError(int errCode) {
        RunningParam mRunningParam = activity.mRunningParam;

        if (ErrorManager.getInstance().isHasInclineError() || ErrorManager.getInstance().isInclineError()) {
            showInclineError();
            if (activity.btn_incline_down.isEnabled()) {
                activity.btn_incline_down.setEnabled(false);
            }
            if (activity.btn_incline_up.isEnabled()) {
                activity.btn_incline_up.setEnabled(false);
            }
            if (activity.btn_incline_roller.isEnabled()) {
                activity.btn_incline_roller.setEnabled(false);
            }
            if (ErrorManager.getInstance().isInclineError()) {
                return;
            }
        }
        activity.prepare321Go.error();

        if (mRunningParam != null) {
            mRunningParam.recodePreRunData();
        }

        //如果速度感应线从一开始就没插好，常态包速度一直返回0，并且处于非stop状态，则取最后下发的速度
        getPresenter().checkLastSpeedOnRunning(activity.isMetric);
        //音量恢复
        restoreVolume();
    }

    /**
     * 出现错误只执行一次
     */
    private boolean isActionVolume;

    /**
     * 音量恢复
     */
    public void restoreVolume() {
        // zrg 打印
        Logger.e("restoreVolume()--isActionVolume = " + isActionVolume + " currentPro = " + activity.currentPro);
        if (isActionVolume) {
            return;
        }
        isActionVolume = true;
        new Thread() {
            @Override
            public void run() {
                super.run();
                SystemSoundManager.getInstance().setAudioVolume(activity.currentPro, SystemSoundManager.maxVolume);
            }
        }.start();
    }
}
