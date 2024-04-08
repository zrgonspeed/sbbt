package com.run.treadmill.activity.home.help;

import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.update.homeupdate.third.HomeThirdAppUpdateManager;

public class HomeError extends BaseHomeHelp {

    public HomeError(HomeActivity activity) {
        super(activity);
    }

    public void showError(int errCode) {
        if (ErrorManager.getInstance().isInclineError()) {
            return;
        }

        activity.showTipPop(CTConstant.SHOW_TIPS_OTHER_ERROR);
        HomeThirdAppUpdateManager.getInstance().hideDialog();
    }

    public void safeError() {
        if (!activity.tipsPop.isSafeError()) {
            activity.wakeUpSleep();
        }
        activity.showTipPop(CTConstant.SHOW_TIPS_SAFE_ERROR);
        startTimerOfSafe();
    }

    public void commOutError() {
        HomeThirdAppUpdateManager.getInstance().hideDialog();
        activity.showTipPop(CTConstant.SHOW_TIPS_COMM_ERROR);
    }

    public void startTimerOfSafe() {
        activity.disableQuickStart();
        SafeKeyTimer.getInstance().registerSafeCb(activity);
        if (SafeKeyTimer.getInstance().getIsSafe()) {
            SafeKeyTimer.getInstance().startTimer(HomeSafeKeyTimeManager.getDelayTime(), activity);
        }
    }
}
