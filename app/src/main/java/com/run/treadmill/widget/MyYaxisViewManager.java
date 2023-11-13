package com.run.treadmill.widget;

import com.run.treadmill.R;
import com.run.treadmill.base.MyApplication;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.SpManager;

public class MyYaxisViewManager {
    // 最大值和最小值之间的值
    private static float[] inclineArr = new float[]{5, 10, 15};
    private static float[] speedMetricArr = new float[]{5, 10, 15, 20};
    private static float[] speedImperialArr = new float[]{5, 10};

    public static void selectYaxis(boolean isLineChartIncline, YaxisView yv_unit) {
        if (isLineChartIncline) {
            yv_unit.setyLineHeight(getDimen(R.dimen.dp_px_360_y));
            yv_unit.setMaxValue(InitParam.MAX_INCLINE_MAX);
            yv_unit.setDrawMaxValue(String.valueOf(InitParam.MAX_INCLINE_MAX));
            yv_unit.setMinValue(0);
            yv_unit.setOtherValue(inclineArr);
        } else {
            if (SpManager.getIsMetric()) {
                yv_unit.setyLineHeight(getDimen(R.dimen.dp_px_360_y));
                yv_unit.setMaxValue(InitParam.MAX_SPEED_MAX_METRIC);
                yv_unit.setDrawMaxValue(String.valueOf((int) InitParam.MAX_SPEED_MAX_METRIC));
                yv_unit.setOtherValue(speedMetricArr);
            } else {
                yv_unit.setyLineHeight(getDimen(R.dimen.dp_px_350_y));
                yv_unit.setDrawMaxValue(String.valueOf(InitParam.MAX_SPEED_MAX_IMPERIAL));
                yv_unit.setMaxValue(InitParam.MAX_SPEED_MAX_IMPERIAL);
                yv_unit.setOtherValue(speedImperialArr);
            }
            yv_unit.setMinValue(0);
        }
        yv_unit.postInvalidate();
    }

    private static float getDimen(int id) {
        return MyApplication.getContext().getResources().getDimension(id);
    }
}
