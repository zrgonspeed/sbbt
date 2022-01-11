package com.run.treadmill.widget.calculator;

import android.content.Context;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;

public class CalculatorOfGoal extends BaseCalculator {

    public CalculatorOfGoal(Context context) {
        super(context);
    }

    @Override
    public String checkOutEnter(String str) {
        String result;
        switch (getEditType()) {
            default:
                result = str;
                break;
            case CTConstant.TYPE_TIME:
                int time = Integer.valueOf(str);
                if (time > InitParam.MAX_TIME_MIN) {
                    time = InitParam.MAX_TIME_MIN;
                } else if (time < InitParam.MIN_TIME_MIN) {
                    time = InitParam.MIN_TIME_MIN;
                }
                result = String.valueOf(time);
                break;
            case CTConstant.TYPE_DISTANCE:
                int distance = Integer.valueOf(str);
                if (distance > InitParam.MAX_DISTANCE) {
                    distance = InitParam.MAX_DISTANCE;
                }
                if (distance <= 1) {
                    distance = 1;
                }
                result = String.valueOf(distance);
                break;
            case CTConstant.TYPE_CALORIES:
                int calories = Integer.valueOf(str);
                if (calories > InitParam.MAX_CALORIES) {
                    calories = InitParam.MAX_CALORIES;
                } else if (calories < InitParam.MIN_CALORIES) {
                    calories = InitParam.MIN_CALORIES;
                }
                result = String.valueOf(calories);
                break;
        }
        return result;
    }
}
