package com.run.treadmill.util;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/01
 */
public class FormulaUtil {

    /**
     * 根据最大最小速度得出间隔为0.1的速度数组，得到速度的下标
     *
     * @param speed    当前速度
     * @param minSpeed 最小速度
     * @return 当前速度的该数组下标
     */
    public static int getInxBySpeed(float speed, float minSpeed) {
        return (int) (speed * 10 - minSpeed * 10);
    }

    /**
     * 根据最大最小速度得出间隔为0.1的速度数组，得到该下标的速度
     *
     * @param inx      当前下标
     * @param minSpeed 最小速度
     * @return 当前下标的速度
     */
    public static float getSpeedByInx(int inx, float minSpeed) {
        return (minSpeed * 10 + inx) / 10f;
    }

    /**
     * 计算运动距离
     *
     * @param speed 速度(kph/mph)
     * @param time  时间(hour)
     * @return 以公制速度计算则返回公制距离;以英制速度计算则返回英制距离
     */
    public static float getRunDistances(float speed, float time) {
        return Math.round(speed * time * 1000000) / 1000000f;
    }

    /**
     * 获取公制距离
     *
     * @param speed    速度
     * @param time     时间
     * @param isMetric 是否公制
     * @return 4位小数的公制距离
     */
    public static float getMetricRunDistances(float speed, float time, boolean isMetric) {
        if (!isMetric) {
            return UnitUtil.getMileToKmByFloat4(getRunDistances(speed, time));
        }
        return getRunDistances(speed, time);
    }

    /**
     * 计算运动卡路里
     *
     * @param weight  重量(kg)
     * @param speed   速度(kph)
     * @param incline 扬升
     * @return kcal
     */
    public static float getRunCalories(float weight, float speed, float incline, boolean isMetric) {
        if (isMetric) {
            weight = UnitUtil.getKgToLb1(weight);
            speed = UnitUtil.getKmToMile(speed);
        }
        float calories;
        if (speed < 3.7f) {
            calories = (1f + 0.768f * speed + 0.137f * speed * incline) * weight / 2.2f / 3600f;
        } else {
            calories = (1f + 1.532f * speed + 0.0685f * speed * incline) * weight / 2.2f / 3600f;
        }
        return calories;
    }

    /**
     * 计算met
     *
     * @param speed
     * @param incline
     * @param isMetric
     * @return
     */
    public static float getMETs(float speed, float incline, boolean isMetric) {
        speed = isMetric ? UnitUtil.getKmToMile(speed) : speed;
        float vo2;
        if (speed < 4.5f) {
            vo2 = 3.5f + (0.1f * speed) + (1.8f * speed * incline);
        } else {
            vo2 = 3.5f + 0.2f * speed + 0.9f * speed * incline;
        }
        return (Math.round((vo2 / 3.5f) * 1000) / 1000.0f);
    }

    /**
     * 获取心率值
     *
     * @param age 年龄
     * @param p
     * @return
     */
    public static int getTHR(int age, int p) {
        return Math.round((220 - age) * (p / 100f));
    }

    /**
     * 计算实际下发速度
     * 显示1-22， 实际0.9-19.8
     */
    public static float computeSpeed(float speed) {
        float resultSpeed = UnitUtil.getFloatBy1f(speed * 0.9f);
        return resultSpeed;
    }

    private float computeSpeedxxxx(float speed, boolean isMetric) {
        //        Logger.d("getMaxSpeed=" + SpManager.getMaxSpeed(isMetric) + ",isMetric=" + isMetric);
        float realSpeedMax = 20f;

        // 大于这个速度点开始按比例下发
        float changePoint = 12.0f;
        if (!isMetric) {
            changePoint = UnitUtil.getKmToMileByFloat1(changePoint);
            realSpeedMax = UnitUtil.getKmToMileByFloat1(realSpeedMax);
        }

        if (speed > changePoint) {
            float p = 0.8f;
            float a = (speed - changePoint) * p + changePoint;

            speed = a;
            // speed = UnitUtil.getFloatToPoint((speed - changePoint) * ((realSpeedMax - changePoint) / (viewSpeedMax - changePoint)), 1) + changePoint;
            if (speed > realSpeedMax) {
                speed = realSpeedMax;
            }
        }

        return speed;
    }
}