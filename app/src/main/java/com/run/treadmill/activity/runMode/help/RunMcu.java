package com.run.treadmill.activity.runMode.help;

import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.util.Logger;

public class RunMcu {
    private BaseRunActivity activity;

    public RunMcu(BaseRunActivity baseRunActivity) {
        this.activity = baseRunActivity;
    }

    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        // 每次进入暂停页面, 保持1秒禁用continue
        if (activity.disFlag) {
            if (activity.btn_pause_continue.isEnabled()) {
                activity.btn_pause_continue.setEnabled(false);
            }
            return;
        }
        if (activity.disPauseBtn) {
            if (activity.btn_start_stop_skip.isEnabled()) {
                activity.btn_start_stop_skip.setEnabled(false);
            }
            return;
        }

        if (beltStatus != 0) {
            if (activity.mRunningParam.isNormal() && activity.btn_start_stop_skip.isEnabled()) {
                Logger.e("runStatus == " + activity.mRunningParam.runStatus);
                activity.btn_start_stop_skip.setEnabled(false);
            }
            if (activity.mRunningParam.isStopStatus() && activity.btn_pause_continue.isEnabled()) {
                activity.btn_pause_continue.setEnabled(false);
            }
            return;
        }

        //有扬升错误，不管扬升状态
        if (ErrorManager.getInstance().isHasInclineError()) {
            if (activity.mRunningParam.isNormal() && !activity.btn_start_stop_skip.isEnabled()) {
                activity.btn_start_stop_skip.setEnabled(true);
            }
            if (activity.mRunningParam.isStopStatus() && !activity.btn_pause_continue.isEnabled()) {
                activity.btn_pause_continue.setEnabled(true);
            }
            return;
        }

        if (inclineStatus == 0) {
            if (activity.mRunningParam.isNormal() && !activity.btn_start_stop_skip.isEnabled()) {
                activity.btn_start_stop_skip.setEnabled(true);
            }
            if (activity.mRunningParam.isStopStatus() && !activity.btn_pause_continue.isEnabled()) {
                activity.btn_pause_continue.setEnabled(true);
            }
            return;
        }

        if (activity.mRunningParam.isNormal() && activity.btn_start_stop_skip.isEnabled()) {
            activity.btn_start_stop_skip.setEnabled(false);
        }
        if (activity.mRunningParam.isStopStatus() && activity.btn_pause_continue.isEnabled()) {
            activity.btn_pause_continue.setEnabled(false);
        }
    }

}