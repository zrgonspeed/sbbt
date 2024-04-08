package com.run.treadmill.activity.home;

import com.run.treadmill.base.BaseView;


public interface HomeView extends BaseView {
    /**
     * 显示加油提示
     */
    void showLube();

    /**
     * 显示加锁
     */
    void showLock();

    /**
     * 显示更新
     */
    void showUpdateApk();

    /**
     * 显示提示
     */
    void showTipsPoint();


    void reSetSleepTime();

    void startMachineLubeTimer();

    void wakeUpSleep();

    boolean isQuickStartEnable();
}
