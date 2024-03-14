package com.run.treadmill.activity.runMode.help;

import static com.run.treadmill.util.ResourceUtils.getColor;

import android.widget.RelativeLayout;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.util.ResourceUtils;
import com.run.treadmill.widget.MyYaxisViewManager;

public class RunLineChart {
    private BaseRunActivity ac;

    public RunLineChart(BaseRunActivity baseRunActivity) {
        this.ac = baseRunActivity;
    }

    public synchronized void refreshLineChart() {
        ac.lineChartView.setRunStageNum(ac.mRunningParam.getRunLccurStageNum());//当前段数
        ac.lineChartView.setValueArray(ac.isLineChartIncline ? ac.mRunningParam.mInclineArray : ac.mRunningParam.mSpeedArray);//30段的数据
        ac.lineChartView.postInvalidate();
    }

    public void settingLineChart() {
        if (ac.isLineChartIncline) {
            ac.btn_line_chart_speed.setTextColor(getColor(R.color.gray));
            ac.btn_line_chart_incline.setTextColor(getColor(R.color.running_text_orange));
            ac.btn_line_chart_incline.setBackground(ResourceUtils.getDraw(R.drawable.tx_fillet_max_bg));
            ac.btn_line_chart_speed.setBackground(ResourceUtils.getDraw(R.drawable.tx_fillet_small_bg));

            RelativeLayout.LayoutParams speedParams = (RelativeLayout.LayoutParams) ac.btn_line_chart_speed.getLayoutParams();
            speedParams.width = ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_400_x);
            speedParams.setMarginStart(ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_1120_x));
            ac.btn_line_chart_speed.setLayoutParams(speedParams);

            RelativeLayout.LayoutParams inclineParams = (RelativeLayout.LayoutParams) ac.btn_line_chart_incline.getLayoutParams();
            inclineParams.width = ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_720_x);
            ac.btn_line_chart_incline.setLayoutParams(inclineParams);
        } else {
            ac.btn_line_chart_speed.setTextColor(getColor(R.color.running_text_orange));
            ac.btn_line_chart_incline.setTextColor(getColor(R.color.gray));
            ac.btn_line_chart_incline.setBackground(ResourceUtils.getDraw(R.drawable.tx_fillet_small_bg));
            ac.btn_line_chart_speed.setBackground(ResourceUtils.getDraw(R.drawable.tx_fillet_max_bg));

            RelativeLayout.LayoutParams speedParams = (RelativeLayout.LayoutParams) ac.btn_line_chart_speed.getLayoutParams();
            speedParams.width = ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_720_x);
            speedParams.setMarginStart(ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_800_x));
            ac.btn_line_chart_speed.setLayoutParams(speedParams);

            RelativeLayout.LayoutParams inclineParams = (RelativeLayout.LayoutParams) ac.btn_line_chart_incline.getLayoutParams();
            inclineParams.width = ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_400_x);
            ac.btn_line_chart_incline.setLayoutParams(inclineParams);
        }

        MyYaxisViewManager.selectYaxis(ac.isLineChartIncline, ac.yv_unit);
        // img_unit.setImageResource(isLineChartIncline ? R.drawable.img_sportmode_profile_incline_calibration_1 : (isMetric ? R.drawable.img_sportmode_profile_speed_calibration_km_1 : R.drawable.img_sportmode_profile_speed_calibration_mile_1));
        ac.lineChartView.setMaxValue(ac.isLineChartIncline ? InitParam.MAX_INCLINE_MAX : (ac.isMetric ? InitParam.MAX_SPEED_MAX_METRIC : InitParam.MAX_SPEED_MAX_IMPERIAL));
    }

}
