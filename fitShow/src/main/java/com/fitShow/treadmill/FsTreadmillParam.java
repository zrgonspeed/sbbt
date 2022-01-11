package com.fitShow.treadmill;

import android.util.Log;

/**
 * @Description
 * @Author YeYueHong
 * @Date 2020/8/31
 */
public class FsTreadmillParam {

    /**
     * 运动时间
     */
    private Integer workTime;
    /**
     * 速度
     */
    private Integer speed;
    /**
     * 扬升
     */
    private Integer incline;
    /**
     * 心率
     */
    private Integer hr;
    /**
     * 步数
     */
    private Integer stepNumber;
    /**
     * 距离
     */
    private Integer distance;
    /**
     * 卡路里
     */
    private Integer calorie;
    /**
     * 段数
     */
    private Integer stageNum;
    /**
     * 错误码
     */
    private Integer error;
    /**
     * 运动状态
     */
    private Integer runStatus;

    public static class Builder {
        private FsTreadmillParam fsTreadmillParam = new FsTreadmillParam();

        /**
         * 把RunParam数据置空
         *
         * @return
         */
        public Builder clean() {
            fsTreadmillParam.workTime = 0;
            fsTreadmillParam.speed = 0;
            fsTreadmillParam.incline = 0;
            fsTreadmillParam.hr = 0;
            fsTreadmillParam.stepNumber = 0;
            fsTreadmillParam.distance = 0;
            fsTreadmillParam.calorie = 0;
            fsTreadmillParam.stageNum = 0;
            fsTreadmillParam.error = 0;
            fsTreadmillParam.runStatus = 0;
            return this;
        }

        /**
         * 单位s
         *
         * @param workTime
         * @return
         */
        public Builder workTime(float workTime) {
            fsTreadmillParam.workTime = (int) workTime;
            return this;
        }

        /**
         * 速度内部扩大10倍
         *
         * @param speed
         * @return
         */
        public Builder speed(float speed) {
            fsTreadmillParam.speed = UnitUtil.getFloatToInt(speed * 10);
            return this;
        }

        /**
         * @param incline
         * @return
         */
        public Builder incline(float incline) {
            fsTreadmillParam.incline = UnitUtil.getFloatToInt(incline);
            return this;
        }

        public Builder hr(int hr) {
            fsTreadmillParam.hr = hr;
            return this;
        }

        public Builder stepNumber(int stepNumber) {
            fsTreadmillParam.stepNumber = stepNumber;
            return this;
        }

        /**
         * 距离内部扩大1000倍
         *
         * @param distance
         * @return
         */
        public Builder distance(float distance) {
            fsTreadmillParam.distance = UnitUtil.getFloatToInt(distance * 1000);
            return this;
        }

        /**
         * 卡路里内部扩大10倍
         *
         * @param calorie
         * @return
         */
        public Builder calorie(float calorie) {
            fsTreadmillParam.calorie = UnitUtil.getFloatToInt(calorie * 10);
            return this;
        }

        public Builder stageNum(int stageNum) {
            fsTreadmillParam.stageNum = stageNum + 1;
            return this;
        }

        public Builder error(int error) {
            fsTreadmillParam.error = error;
            return this;
        }

        public Builder runStatus(int runStatus) {
            fsTreadmillParam.runStatus = runStatus;
            return this;
        }


        public FsTreadmillParam build() {
            return fsTreadmillParam;
        }
    }

    public Integer getWorkTime() {
        return workTime;
    }

    public Integer getSpeed() {
        if (speed > 255) {//运动秀协议缺陷，一个字节最大只能到255,速度不能大于25.5
            return 255;
        }
        return speed;
    }

    public Integer getIncline() {
        return incline;
    }

    public Integer getHr() {
        return hr;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public Integer getDistance() {
        return distance;
    }

    public Integer getCalorie() {
        return calorie;
    }

    public Integer getStageNum() {
        return stageNum;
    }


    public Integer getError() {
        return error;
    }

    public Integer getRunStatus() {
        return runStatus;
    }
}
