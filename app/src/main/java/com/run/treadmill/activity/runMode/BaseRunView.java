package com.run.treadmill.activity.runMode;

import com.run.treadmill.base.BaseView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/04
 */
public interface BaseRunView extends BaseView {

    /**
     * 速度调整(显示数值)
     *
     * @param speed
     */
    void onSpeedChange(float speed);

    /**
     * 扬升调整(显示数值)
     *
     * @param incline
     */
    void onInclineChange(float incline);

    /**
     * 监听incline数值变化后的动作
     *
     * @param incline
     */
    void afterInclineChanged(float incline);

    /**
     * 监听speed数值变化后的动作
     *
     * @param speed
     */
    void afterSpeedChanged(float speed);

    /**
     * 3-2-1-go的后续动作
     */
    void afterPrepare();

    /**
     * 进入cool down 页面,但是quick start模式没有
     */
    void enterCoolDown();

    /**
     * 运动结束
     */
    void finishRunning();
}