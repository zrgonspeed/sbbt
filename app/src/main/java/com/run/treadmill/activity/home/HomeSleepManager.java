package com.run.treadmill.activity.home;

import android.view.View;

import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.homeupdate.main.HomeApkUpdateManager;
import com.run.treadmill.homeupdate.third.HomeThirdAppUpdateManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;

public class HomeSleepManager implements CustomTimer.TimerCallBack {
    private HomeActivity homeActivity;
    public static final int SLEEP_TIME = 60 * 10;

    private final String sleepTag = "sleep";
    private CustomTimer mSleepTimer;

    public HomeSleepManager(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    protected void startTimerOfSleep() {
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
            homeActivity.getPresenter().inOnSleep = true;

            GpIoUtils.setScreen_0();
            homeActivity.runOnUiThread(() -> homeActivity.tv_sleep.setVisibility(View.VISIBLE));

            homeActivity.runOnUiThread(() -> {
                // 隐藏第三方app更新弹窗
                if (HomeThirdAppUpdateManager.getInstance().isShow()) {
                    HomeThirdAppUpdateManager.getInstance().hideDialog();
                    HomeThirdAppUpdateManager.getInstance().setNewCheck(true);
                }
            });

            mSleepTimer.closeTimer();
        }
    }

    public void resetTime() {
        if (mSleepTimer != null) {
            mSleepTimer.setmAllTime(0L);
        }
    }

    public void wakeUpSleep() {
        if (GpIoUtils.checkScreenState() == GpIoUtils.IO_STATE_0) {
            GpIoUtils.setScreen_1();
            homeActivity.getPresenter().inOnSleep = false;
            homeActivity.tv_sleep.setVisibility(View.GONE);
        }
        startTimerOfSleep();
    }
}
