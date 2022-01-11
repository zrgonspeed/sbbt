package com.run.treadmill.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/07
 */
public class CustomTimer {

    private TimerCallBack mTimerCallBack;
    private Timer mTimer;
    private TimerTask mTimerTask;

    /**
     * 定时器的标志
     */
    private String mTag;
    /**
     * 标志缓存
     */
    private static List<String> cacheTag;
    /**
     * 总共执行了多少时间（s）
     */
    private long mAllTime;

    public CustomTimer() {
        if (cacheTag == null) {
            cacheTag = new ArrayList<>();
        }
    }

    public interface TimerCallBack {

        void timerComply(long lastTime, String tag);
    }

    public void setCallback(TimerCallBack callback) {
        this.mTimerCallBack = callback;
    }

    public void setTag(String tag) {
        if (cacheTag.contains(tag)) {
            throw new RuntimeException("已存在同种定时器！");
        }
        this.mTag = tag;
        cacheTag.add(mTag);
    }

    /**
     * 设置统计时间
     *
     * @param allTime
     */
    public void setmAllTime(long allTime) {
        this.mAllTime = allTime;
    }

    /**
     * 开始计时
     *
     * @param delay    延时
     * @param callBack 回调
     */
    public void startTimer(long delay, TimerCallBack callBack) {
        startTimer(delay, null, callBack);
    }

    /**
     * 开始计时
     *
     * @param delay     延时
     * @param timerTask 自定义任务
     * @param callBack  回调
     */
    public void startTimer(long delay, TimerTask timerTask, TimerCallBack callBack) {
        if (mTimer == null) {
            mTimer = new Timer(true);
        }
        if (mTimerTask == null) {
            mTimerTask = timerTask == null ? new LoopTask() : timerTask;
        }
        this.mTimerCallBack = callBack;
        mTimer.schedule(mTimerTask, delay);
    }

    /**
     * 开始计时
     *
     * @param delay    延时
     * @param period   重复时间
     * @param callBack 回调
     */
    public void startTimer(long delay, long period, TimerCallBack callBack) {
        if (mTimer == null) {
            mTimer = new Timer(true);
        }
        if (mTimerTask == null) {
            mTimerTask = new LoopTask();
        }
        this.mTimerCallBack = callBack;
        mTimer.schedule(mTimerTask, delay, period);
    }

    /**
     * 关闭计时
     */
    public void closeTimer() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        cacheTag.remove(mTag);
        mAllTime = 0;
    }

    class LoopTask extends TimerTask {
        @Override
        public void run() {
            mAllTime++;
            if (mTimerCallBack != null) {
                mTimerCallBack.timerComply(mAllTime, mTag);
            }
        }
    }
}