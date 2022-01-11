package com.run.treadmill.activity.home;

import com.run.treadmill.base.BaseView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/05/29
 */
public interface HomeView extends BaseView {
    /**
     * 显示加油提示
     */
    void showLube();

    void showMachineLue(int type);

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

    /**
     * 进入setting的Lock界面
     */
    void enterSettingLock();

    /**
     * 进入工程模式二
     */
    void enterFactoryTwo();

    void reSetSleepTime();

    void startMachineLubeTimer();

    void wakeUpSleep();

    boolean isQuickStartEnable();
}
