package com.run.treadmill.activity.factory;

import com.run.treadmill.base.BaseView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/24
 */
public interface FactoryView extends BaseView {

    /**
     * 返回校正的 ad
     *
     * @param max 校正上升
     * @param min 校正下降
     */
    void onCalibrationAd(int max, int min);

    /**
     * 校正成功
     */
    void onCalibrationSuccess();

    /**
     * 校正完成回idle
     * 主要是为了延时
     */
    void onCalibrationSuccessGoBackHome();

    /**
     * rpm+-是否可按
     *
     * @param type 可按类型
     */
    void setRpmEnable(int type);
}