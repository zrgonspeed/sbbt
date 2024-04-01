package com.run.treadmill.activity.runMode.help;

import android.view.View;
import android.widget.RadioButton;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.clicktime.RunGraphUtils;
import com.run.treadmill.widget.chart.SportGraph;

/**
 * 速度和扬升的柱状图
 */
public class RunGraph {
    private SportGraph bar_graph;
    private RadioButton rb_graph_left;
    private RadioButton rb_graph_right;

    public void initGraph(SportGraph bar_graph) {
        this.bar_graph = bar_graph;

        rb_graph_left = bar_graph.findViewById(R.id.rb_graph_left);
        rb_graph_right = bar_graph.findViewById(R.id.rb_graph_right);

        ClickRadio clickRadio = new ClickRadio();
        rb_graph_left.setOnClickListener(clickRadio);
        rb_graph_right.setOnClickListener(clickRadio);

        // 默认先显示速度
        rb_graph_left.setVisibility(View.GONE);
        rb_graph_right.setVisibility(View.GONE);
        bar_graph.setCurrSelectType(1);
        checkedSpeed();
    }

    private class ClickRadio implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!RunGraphUtils.canResponse()) {
                return;
            }

            if (v.getId() == R.id.rb_graph_left) {
                // Logger.i("checkedId == 点扬升，要切为速度");
                checkedSpeed();
            }
            if (v.getId() == R.id.rb_graph_right) {
                // Logger.i("checkedId == 点速度, 要切为扬升");
                checkedIncline();
            }
        }
    }

    private int getMaxIncline() {
        return SpManager.getMaxIncline();
    }

    private float getMaxSpeed() {
        return SpManager.getMaxSpeed();
    }

    public synchronized void checkedIncline() {
        if (rb_graph_left.getVisibility() == View.VISIBLE) {
            return;
        }
        rb_graph_right.setVisibility(View.GONE);
        rb_graph_left.setVisibility(View.VISIBLE);

        bar_graph.setCurrSelectType(0);
        bar_graph.setUnitRange(0f, getMaxIncline(), 6, 0);
        bar_graph.setValueRange(0f, getMaxIncline());
        inclineRefresh();
    }

    public synchronized void checkedSpeed() {
        if (rb_graph_right.getVisibility() == View.VISIBLE) {
            return;
        }
        rb_graph_right.setVisibility(View.VISIBLE);
        rb_graph_left.setVisibility(View.GONE);
        bar_graph.setCurrSelectType(1);
        bar_graph.setUnitRange(0f, getMaxSpeed(), 7, 0);
        bar_graph.setValueRange(0f, getMaxSpeed());
        speedRefresh();
    }

    public synchronized void afterInclineChanged() {
        if (curIsIncline()) {
            inclineRefresh();
        }
    }

    public synchronized void afterSpeedChanged() {
        if (curIsSpeed()) {
            speedRefresh();
        }
    }

    private synchronized void speedRefresh() {
        bar_graph.setCurrSelectValue(1, ac.getSpeed());
        bar_graph.setArrayData(
                ac.mRunningParam.mSpeedArray,
                ac.mRunningParam.getLcCurStageNum(),
                1
        );
    }

    private synchronized void inclineRefresh() {
        bar_graph.setCurrSelectValue(0, ac.getIncline());
        bar_graph.setArrayData(
                ac.mRunningParam.mInclineArray,
                ac.mRunningParam.getLcCurStageNum(),
                0
        );
    }

    private synchronized boolean curIsIncline() {
        return bar_graph.getCurrSelectType() == 0;
    }

    private synchronized boolean curIsSpeed() {
        return bar_graph.getCurrSelectType() == 1;
    }

    private BaseRunActivity ac;

    public RunGraph(BaseRunActivity baseRunActivity) {
        this.ac = baseRunActivity;
    }
}
