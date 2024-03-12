package com.run.treadmill.activity.home.help;

import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.update.homeupdate.third.HomeThirdAppUpdateManager;
import com.run.treadmill.util.Logger;

public class HomeMcuCallBack extends BaseHomeHelp {

    public HomeMcuCallBack(HomeActivity activity) {
        super(activity);
    }

    public void cmdKeyValue(int keyValue) {
        if (activity.getPresenter().inOnSleep) {
            activity.wakeUpSleep();
            return;
        }

        if (keyValue == SerialKeyValue.START_CLICK ||
                keyValue == SerialKeyValue.HAND_START_CLICK
        ) {
            if (activity.tipsPop.isShowTips() || ((MyApplication) activity.getApplication()).isFirst) {
                return;
            }
            // 第三方更新弹窗，不给进入start
            if (HomeThirdAppUpdateManager.getInstance().isShow()) {
                Logger.i("return; 第三方更新弹窗，不给进入start");
                return;
            }
        }
    }

    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        //Log.d("beltAndInclineStatus", ",beltStatus=" + beltStatus + ",inclineStatus=" + inclineStatus + ",curInclineAd=" + curInclineAd);
        if (activity.isOnPause) {//防止切换界面还调用该方法（运动秀受影响）
            return;
        }
        if (!SafeKeyTimer.getInstance().getIsSafe()) {
            activity.disableQuickStart();
            return;
        }

        if (activity.tipsPop.isShowTips()) {
            activity.disableQuickStart();
            return;
        }
        if (beltStatus != 0) {
            activity.disableQuickStart();

            if (activity.isFirst) {
                ControlManager.getInstance().stopRun(SpManager.getGSMode());
                activity.isFirst = false;
            }
            return;
        }

        if (ErrorManager.getInstance().isHasInclineError()) {
            activity.enableQuickStart();
            return;
        }

        if (inclineStatus == 0) {
            activity.enableQuickStart();
            return;
        }

        activity.disableQuickStart();
    }
}
