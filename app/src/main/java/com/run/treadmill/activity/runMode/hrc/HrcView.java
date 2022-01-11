package com.run.treadmill.activity.runMode.hrc;

import com.run.treadmill.activity.runMode.BaseRunView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/03
 */
public interface HrcView extends BaseRunView {

    /**
     * 显示无心率
     */
    void showNoPulse();

    /**
     * 显示心率超载
     */
    void showOverPulse();

    /**
     * 隐藏心率提示
     */
    void hidePulseTip();

    /**
     * 根据心率调整速度
     *
     * @param speed 改变的速度值
     */
    void changeSpeedByPulse(float speed);
}