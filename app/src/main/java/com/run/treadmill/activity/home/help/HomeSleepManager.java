package com.run.treadmill.activity.home.help;

import android.view.View;

import com.run.treadmill.Custom;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.update.homeupdate.main.HomeApkUpdateManager;
import com.run.treadmill.update.homeupdate.third.HomeThirdAppUpdateManager;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;

public class HomeSleepManager extends BaseHomeHelp implements CustomTimer.TimerCallBack, Custom.HomeSleep {
    public static final int SLEEP_TIME = Custom.HomeSleep.SLEEP_TIME;

    private final String sleepTag = "sleep";
    private CustomTimer mSleepTimer;

    public HomeSleepManager(HomeActivity homeActivity) {
        super(homeActivity);
    }

    public void startTimerOfSleep() {
        if (SpManager.getSleep()) {
            if (mSleepTimer == null) {
                mSleepTimer = new CustomTimer();
                mSleepTimer.setTag(sleepTag);
            }
            mSleepTimer.closeTimer();
            mSleepTimer.setTag(sleepTag);
            mSleepTimer.startTimer(1000, 1000, this);
        }
    }

    public void closeTimer() {
        if (mSleepTimer != null) {
            mSleepTimer.closeTimer();
        }
    }

    @Override
    public void timerComply(long lastTime, String tag) {
        Logger.d("lastTime == " + lastTime);
        if (tag.equals(sleepTag)) {
            if (lastTime < SLEEP_TIME) {
                return;
            }
            if (HomeApkUpdateManager.getInstance().isUpDateYes()) {
                return;
            }
            Logger.d("==========     睡眠     ==========");
            activity.getPresenter().inOnSleep = true;

            GpIoUtils.setCloseScreen();
            activity.runOnUiThread(() -> activity.tv_sleep.setVisibility(View.VISIBLE));

            activity.runOnUiThread(() -> {
                // 隐藏第三方app更新弹窗
                if (HomeThirdAppUpdateManager.getInstance().isShow()) {
                    HomeThirdAppUpdateManager.getInstance().hideDialog();
                    HomeThirdAppUpdateManager.getInstance().setNewCheck(true);
                }
            });

            mSleepTimer.closeTimer();
            // KeyLight.closeLight();
        }
    }

    public void resetTime() {
        if (mSleepTimer != null) {
            mSleepTimer.setmAllTime(0L);
        }
    }

    public void wakeUpSleep() {
        if (GpIoUtils.checkScreenState() == GpIoUtils.IO_STATE_0) {
            GpIoUtils.setOpenScreen();
            activity.getPresenter().inOnSleep = false;
            activity.tv_sleep.setVisibility(View.GONE);
            // KeyLight.openLight();
        }
        startTimerOfSleep();
    }
}
