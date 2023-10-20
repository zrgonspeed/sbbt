package com.run.treadmill.manager.fitshow.other;

public interface FitShowRunningCallBack {
    void fitShowStopRunning();

    void fitShowPausedRunning();

    void fitShowStartRunning();

    void fitShowSetSpeed(float speed);

    void fitShowSetIncline(float incline);
}
