package com.run.treadmill.widget.calculator;

import android.content.Context;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.util.UnitUtil;

public class CalculatorOfFactory extends BaseCalculator {

    public CalculatorOfFactory(Context context) {
        super(context);
    }

    @Override
    public String checkOutEnter(String str) {
        float result = Float.valueOf(str);
        switch (getEditType()) {
            default:
                break;
            case CTConstant.TYPE_FACTORY_HIGH_SPEED:
                if (isMetric) {
                    if (InitParam.MAX_SPEED_MAX_METRIC <= result) {
                        result = InitParam.MAX_SPEED_MAX_METRIC;
                    } else if (InitParam.MAX_SPEED_MIN_METRIC >= result) {
                        result = InitParam.MAX_SPEED_MIN_METRIC;
                    }
                } else {
                    if (InitParam.MAX_SPEED_MAX_IMPERIAL <= result) {
                        result = InitParam.MAX_SPEED_MAX_IMPERIAL;
                    } else if (InitParam.MAX_SPEED_MIN_IMPERIAL >= result) {
                        result = InitParam.MAX_SPEED_MIN_IMPERIAL;
                    }
                }
                break;
            case CTConstant.TYPE_FACTORY_LOW_SPEED:
                if (isMetric) {
                    if (InitParam.MIN_SPEED_MAX_METRIC <= result) {
                        result = InitParam.MIN_SPEED_MAX_METRIC;
                    } else if (InitParam.MIN_SPEED_MIN_METRIC >= result) {
                        result = InitParam.MIN_SPEED_MIN_METRIC;
                    }
                } else {
                    if (InitParam.MIN_SPEED_MAX_IMPERIAL <= result) {
                        result = InitParam.MIN_SPEED_MAX_IMPERIAL;
                    } else if (InitParam.MIN_SPEED_MIN_IMPERIAL >= result) {
                        result = InitParam.MIN_SPEED_MIN_IMPERIAL;
                    }
                }
                break;
            case CTConstant.TYPE_FACTORY_WHEEL_SIZE:
                if (InitParam.MAX_WHEEL_SIZE <= result) {
                    result = InitParam.MAX_WHEEL_SIZE;
                } else if (InitParam.MIN_WHEEL_SIZE >= result) {
                    result = InitParam.MIN_WHEEL_SIZE;
                }
                break;
            case CTConstant.TYPE_FACTORY_SPEED_RATE:
                if (InitParam.MAX_SPEED_RATE <= result) {
                    result = InitParam.MAX_SPEED_RATE;
                } else if (InitParam.MIN_SPEED_RATE >= result) {
                    result = InitParam.MIN_SPEED_RATE;
                }
                break;
            case CTConstant.TYPE_FACTORY_MAX_INCLINE:
                if (InitParam.MAX_INCLINE_MAX <= result) {
                    result = InitParam.MAX_INCLINE_MAX;
                } else if (InitParam.MAX_INCLINE_MIN >= result) {
                    result = InitParam.MAX_INCLINE_MIN;
                }
                break;
            case CTConstant.TYPE_FACTORY_LUBE:
                if (InitParam.MAX_LUBE_DISTANCE <= result) {
                    result = InitParam.MAX_LUBE_DISTANCE;
                } else if (InitParam.MIN_LUBE_DISTANCE >= result) {
                    result = InitParam.MIN_LUBE_DISTANCE;
                }
                break;
            case CTConstant.TYPE_FACTORY_RPM:
                if (InitParam.MAX_RPM <= result) {
                    result = InitParam.MAX_RPM;
                } else if (InitParam.MIN_RPM >= result) {
                    result = InitParam.MIN_RPM;
                }
                break;
        }
        return String.valueOf(UnitUtil.getPoint(result, getFloatPoint()));
    }
}
