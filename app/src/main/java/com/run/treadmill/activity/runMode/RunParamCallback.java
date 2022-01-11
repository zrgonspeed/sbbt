package com.run.treadmill.activity.runMode;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/21
 */
public interface RunParamCallback {

    void dataCallback();

    /**
     * 当段数发生变化回调
     */
    void onCurStageNumChange();

    void cooldown10Callback();
}