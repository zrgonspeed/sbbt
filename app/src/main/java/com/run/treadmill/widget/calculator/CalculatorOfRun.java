package com.run.treadmill.widget.calculator;

import android.content.Context;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.sp.SpManager;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/05
 */
public class CalculatorOfRun extends BaseCalculator {

    public CalculatorOfRun(Context context) {
        super(context);
    }

    @Override
    String checkOutEnter(String str) {
//        float value = (float) (Float.valueOf(str) / Math.pow(10,getFloatPoint()));
        float value = Float.valueOf(str);
        switch (getEditType()) {
            default:
                break;
            case CTConstant.TYPE_SPEED:
                if (value >= SpManager.getMaxSpeed(SpManager.getIsMetric())) {
                    value = SpManager.getMaxSpeed(SpManager.getIsMetric());
                } else if (value <= SpManager.getMinSpeed(SpManager.getIsMetric())) {
                    value = SpManager.getMinSpeed(SpManager.getIsMetric());
                }
                break;
            case CTConstant.TYPE_INCLINE:
                if (value >= SpManager.getMaxIncline()) {
                    value = SpManager.getMaxIncline();
                } else if (value <= 0) {
                    value = 0f;
                }
                break;
        }
        return String.valueOf(value);
    }
}