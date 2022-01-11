package com.run.treadmill.activity.runMode.fitness;

import com.run.treadmill.activity.runMode.BaseRunView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/03
 */
public interface FitnessTestView extends BaseRunView {

    /**
     * 显示无心率提示
     */
    void showNoPulsePop();

    /**
     * 隐藏无心率提示
     */
    void hidePulseTip();
}