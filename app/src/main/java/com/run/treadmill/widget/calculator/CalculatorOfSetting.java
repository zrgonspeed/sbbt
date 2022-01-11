package com.run.treadmill.widget.calculator;

import android.content.Context;

public class CalculatorOfSetting extends BaseCalculator {

    public CalculatorOfSetting(Context context) {
        super(context);
    }

    @Override
    public String checkOutEnter(String str) {
//        switch (getEditType()) {
//            default:
//                break;
//            case CTConstant.TYPE_SETTING_TIME:
//                break;
//            case CTConstant.TYPE_SETTING_DISTANCE:
//                break;
//            case CTConstant.TYPE_SETTING_LOCK:
//                break;
//        }

        return str;
    }
}