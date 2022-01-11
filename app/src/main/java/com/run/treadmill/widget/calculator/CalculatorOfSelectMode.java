package com.run.treadmill.widget.calculator;

import android.content.Context;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;

public class CalculatorOfSelectMode extends BaseCalculator {

    public CalculatorOfSelectMode(Context context) {
        super(context);
    }

    @Override
    public String checkOutEnter(String str) {
        String result;
        switch (getEditType()) {
            case CTConstant.TYPE_AGE:
                int age = Integer.valueOf(str);
                if (age > InitParam.MAX_AGE) {
                    age = InitParam.MAX_AGE;
                } else if (age < InitParam.MIN_AGE) {
                    age = InitParam.MIN_AGE;
                }
                result = String.valueOf(age);
                break;
            case CTConstant.TYPE_TIME:
                int time = Integer.valueOf(str);
                if (time > InitParam.MAX_TIME_MIN) {
                    time = InitParam.MAX_TIME_MIN;
                } else if (time < 5) {
                    time = 0;
                }
                result = String.valueOf(time);
                break;
            case CTConstant.TYPE_WEIGHT:
                int weight = Integer.valueOf(str);
                if (isMetric) {
                    if (weight > InitParam.MAX_WEIGHT_METRIC) {
                        weight = InitParam.MAX_WEIGHT_METRIC;
                    } else if (weight < InitParam.MIN_WEIGHT_METRIC) {
                        weight = InitParam.MIN_WEIGHT_METRIC;
                    }
                } else {
                    if (weight > InitParam.MAX_WEIGHT_IMPERIAL) {
                        weight = InitParam.MAX_WEIGHT_IMPERIAL;
                    } else if (weight < InitParam.MIN_WEIGHT_IMPERIAL) {
                        weight = InitParam.MIN_WEIGHT_IMPERIAL;
                    }
                }
                result = String.valueOf(weight);
                break;
            case CTConstant.TYPE_THR:
                int thr = Integer.valueOf(str);
                if (thr > InitParam.MAX_TARGET_HEART_RATE) {
                    thr = InitParam.MAX_TARGET_HEART_RATE;
                } else if (thr < InitParam.MIN_TARGET_HEART_RATE) {
                    thr = InitParam.MIN_TARGET_HEART_RATE;
                }
                result = String.valueOf(thr);
                break;
            default:
                result = str;
                break;
        }
        return result;
    }
}
