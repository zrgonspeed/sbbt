package com.chuhui.btcontrol.bean;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/03/26
 */
public class RunParam {
    /**
     * 速度,单位-->km/h (1位小数)
     */
    private float speed;

    /**
     * 距离,单位-->m(米) （2位小数）
     */
    private float distance;

    /**
     * 扬升
     */
    private float incline;

    /**
     * 卡路里,单位-->kCal
     */
    private float kCal;

    /**
     * 心率
     */
    private int hr;

    /**
     * 时间,单位-->sec(秒)
     */
    private long time;
    /** 剩余时间，单位--> sec*/
    private long remainingTime;

    /**
     * 停止状态（结束）
     */
    public static final int RUN_STOP_STATUS = 1;
    /**
     * 开始状态
     */
    public static final int RUN_START_STATUS = 2;
    /**
     * 暂停状态
     */
    public static final int RUN_PAUSE_STATUS = 3;
    /**
     * 运动状态
     */
    private int runStatus;

    /**
     * 无效，预留
     */
    public static final int RUN_NONE_STAGE = 0;
    /**
     * 倒数 阶段
     */
    public static final int RUN_COUNTDOWN_STAGE = 1;
    /**
     * warmup 阶段
     */
    public static final int RUN_WARM_UP_STAGE = 2;
    /**
     * 正常运动 阶段
     */
    public static final int RUN_GENERAL_STAGE = 3;
    /**
     * 运动阶段
     */
    private int runStage;

    /**
     * 是否完全静止  1:静止，2：非静止
     */
    private int atRest;

    private int stat = -1;

    public void goSummary(){
        speed = 0f;
        incline = 0f;
    }

    public void reset() {
        speed = 0f;
        distance = 0.0f;
        incline = 0f;
        kCal = 0;
        hr = 0;
        time = 0;
        remainingTime = 0;
        runStatus = RUN_STOP_STATUS;
        runStage = RUN_NONE_STAGE;
        atRest = 1;
    }

    public static class Builder {
        private RunParam mRunParam = new RunParam();

        /**
         * 设置速度，公制
         *
         * @param speed
         * @return
         */
        public Builder speed(float speed) {
            mRunParam.speed = speed;
            return this;
        }

        /**
         * 设置距离，公制
         *
         * @param distance
         * @return
         */
        public Builder distance(float distance) {
            mRunParam.distance = distance;
            return this;
        }

        /**
         * 设置扬升
         *
         * @param incline
         * @return
         */
        public Builder incline(float incline) {
            mRunParam.incline = incline;
            return this;
        }

        /**
         * 设置卡路里
         *
         * @param kCal
         * @return
         */
        public Builder kCal(float kCal) {
            mRunParam.kCal = kCal;
            return this;
        }

        /**
         * 设置心率
         *
         * @param hr
         * @return
         */
        public Builder hr(int hr) {
            mRunParam.hr = hr;
            return this;
        }

        /**
         * 设置时间
         *
         * @param time
         * @return
         */
        public Builder time(long time) {
            mRunParam.time = time;
            return this;
        }

        /**
         * 设置剩余时间
         * @param remainingTime
         * @return
         */
        public Builder remainingTime(long remainingTime){
            mRunParam.remainingTime = remainingTime;
            return this;
        }

        /**
         * @param runStatus
         * @return
         */
        public Builder runStatus(int runStatus) {
            if (runStatus == RUN_PAUSE_STATUS) {
                speed(0.0f);
                mRunParam.stat = mRunParam.runStatus;
            }
            mRunParam.runStatus = runStatus;
            return this;
        }

        public Builder runStage(int runStage) {
            mRunParam.runStage = runStage;
            return this;
        }

        public Builder atRest(boolean atRest) {
            mRunParam.atRest = atRest ? 1 : 2;
            return this;
        }

        public Builder runFinish() {
            mRunParam.reset();
            return this;
        }

        public RunParam bulid() {
            return mRunParam;
        }
    }

    public float getSpeed() {
        return speed;
    }

    public float getDistance() {
        return distance;
    }

    public float getIncline() {
        return incline;
    }

    public float getkCal() {
        return kCal;
    }

    public int getHr() {
        return hr;
    }

    public long getTime() {
        return time;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public int getRunStatus() {
        if (stat != -1) {
            stat = -1;
            return RUN_START_STATUS;
        }
        return runStatus;
    }

    public int getRunStage() {
        return runStage;
    }

    public int getAtRest() {
        return atRest;
    }
}