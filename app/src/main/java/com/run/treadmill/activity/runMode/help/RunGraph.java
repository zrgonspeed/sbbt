package com.run.treadmill.activity.runMode.help;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.widget.chart.SportGraph;

/**
 * 速度和扬升的柱状图
 */
public class RunGraph {
    private SportGraph bar_graph;

    public void initGraph(SportGraph bar_graph) {
        this.bar_graph = bar_graph;

        /*扬升和速度点击切换事件*/
        bar_graph.setOnGraphTypeChangeListerer((group, checkedId) -> {
            if (checkedId == R.id.rb_graph_left) {
                checkIncline();
            }
            if (checkedId == R.id.rb_graph_right) {
                checkSpeed();
            }
        });

        bar_graph.setCheckBarGraph(0);
    }

    private int getMaxIncline() {
        return SpManager.getMaxIncline();
    }

    private float getMaxSpeed() {
        return SpManager.getMaxSpeed();
    }

    private void checkIncline() {
        bar_graph.setCurrSelectType(0);
        bar_graph.setUnitRange(0f, getMaxIncline(), 6, 0);
        bar_graph.setValueRange(0f, getMaxIncline());
        bar_graph.setArrayData(
                ac.mRunningParam.mInclineArray,
                ac.mRunningParam.getLcCurStageNum(),
                0
        );
        bar_graph.setCurrSelevtValue(0, ac.getIncline());
        // bar_graph.setCurrSelectInx(0);
    }

    private void checkSpeed() {
        bar_graph.setCurrSelectType(1);
        bar_graph.setUnitRange(0f, getMaxSpeed(), 7, 0);
        bar_graph.setValueRange(0f, getMaxSpeed());
        bar_graph.setArrayData(
                ac.mRunningParam.mSpeedArray,
                ac.mRunningParam.getLcCurStageNum(),
                1
        );
        // bar_graph.setCurrSelectInx(1);
        bar_graph.setCurrSelevtValue(1, ac.getSpeed());
    }

    public void refreshChart() {
        if (curIsIncline()) {
            inclineRefresh();
        }

        if (curIsSpeed()) {
            speedRefresh();
        }
    }

    public void speedRefresh() {
        bar_graph.setArrayData(
                ac.mRunningParam.mSpeedArray,
                ac.mRunningParam.getLcCurStageNum(),
                1
        );
    }

    public void inclineRefresh() {
        bar_graph.setArrayData(
                ac.mRunningParam.mInclineArray,
                ac.mRunningParam.getLcCurStageNum(),
                0
        );
    }

    public boolean curIsIncline() {
        return bar_graph.getCurrSelectType() == 0;
    }

    public boolean curIsSpeed() {
        return bar_graph.getCurrSelectType() == 1;
    }
/*
    private GraphRefresh graphRefresh;

    public void setGraphRefresh(GraphRefresh graphRefresh) {
        this.graphRefresh = graphRefresh;
    }

    interface GraphRefresh {
        void inclineRefresh();

        void speedRefresh();
    }*/

    private BaseRunActivity ac;

    public RunGraph(BaseRunActivity baseRunActivity) {
        this.ac = baseRunActivity;
    }
}
