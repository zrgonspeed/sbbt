package com.run.treadmill.util;

/**
 * 时间转换为字符串
 */
public class TimeStringUtil {

    private TimeStringUtil() {

    }

    /**
     * 将毫秒转换为00:00(分钟:秒);
     * 分钟数会突破99
     *
     * @param value 毫秒
     * @return
     */
    public static String getMsToMinSecValue(float value) {
        long time = Math.round(value / 1000.0);
        long minute = time / 60;
        long hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        minute += hour * 60;
        return String.format("%02d:%02d", minute, second);
    }

    /**
     * 将毫秒转换为00:00(分钟:秒);
     * 分钟数会突破99
     *
     * @param value 毫秒
     * @return
     */
    public static String getMsToMinSecValueOnSummary(float value) {
        long time = Math.round(value / 1000.0);
        long minute = time / 60;
        long hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        minute += hour * 60;
        if (minute >= 99999) {
            minute = 99999;
        }
        return String.format("%02d:%02d", minute, second);
    }

    /**
     * 将毫秒转换为00:00(分钟:秒);
     * 分钟数超过100会重置
     *
     * @param value 毫秒
     * @return
     */
    public static String getMsToMinSecValueHasUp(float value) {
        long time = Math.round(value / 1000.0);
        time = time % (100 * 60);
        long minute = time / 60;
        long hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        minute += hour * 60;
        return String.format("%02d:%02d", minute, second);
    }

    /**
     * 将毫秒转换为00:00(小时:分钟);
     * 小时数会突破99
     *
     * @param value 毫秒
     * @return
     */
    public static String getMsToHourMinValue(float value) {
        long time = Math.round(value / 1000.0);
        long minute = time / 60;
        long hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * 毫秒转换为时:分:秒;
     * 小时数会突破99
     *
     * @param value
     * @return
     */
    public static String getMsToTimeValueOnRunning(float value) {
        long time = Math.round(value / 1000.0);
        long minute = time / 60;
        long hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        return String.format("%01d:%02d:%02d", hour, minute, second);
    }

    /**
     * 毫秒转换为时:分:秒;
     * 小时数会突破99
     *
     * @param value
     * @return
     */
    public static String getMsToTimeValue(float value) {
        long time = Math.round(value / 1000.0);
        long minute = time / 60;
        long hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            return "00:" + String.format("%02d:%02d", minute, second);
        }
    }

    public static String getHourToTimeValue(float value) {
        long time = Math.round(value * 60 * 60);
        long minute = time / 60;
        long hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            return String.format("%02d:%02d", minute, second);
        }
    }

    /**
     * 秒数转换为 hh HR:mm MIN(带单位)
     *
     * @param value
     * @return
     */
    public static String getSecToRemainHourMin(long value) {
        long time = Math.round(value);
        long minute = time / 60;
        long hour = minute / 60;
//        long second = time % 60;
        minute %= 60;
        return String.format("%d HR:%02d MIN", hour, minute);
//        if (hour > 0) {
//            return String.format("%d HR:%02d MIN", hour, minute);
//
//        } else {
//            return String.format("%d MIN:%02d SEC", minute, second);
//        }
    }

    /**
     * 秒转小时分钟 （xx hr xx min）
     *
     * @param sec
     * @return
     */
    public static String getSecToHrMin(long sec) {
        long hr = sec / 3600;
        long min = (sec - hr * 3600) / 60;
        return hr + " hr " + min + " min";
    }

    /**
     * 秒数转换为 hh HR :mm MIN :ss SEC
     *
     * @param value
     * @return
     */
    public static String getSecToRemainTime(long value) {
        long time = Math.round(value);
        long minute = time / 60;
        long hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        if (hour > 0) {
            return String.format("%d HR:%02d MIN:%d SEC", hour, minute, second);

        } else {
            return String.format("%d MIN:%02d SEC", minute, second);
        }
    }

    public static String getSecToHour(long value) {
        long time = Math.round(value);
        long minute = time / 60;
        long hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        return hour + "";
    }

    /**
     * 将秒转为小时分钟或者分钟秒
     *
     * @param value   秒
     * @param format1 时间格式(小时  分钟)
     * @param format2 时间格式（分钟  秒）
     * @return
     */
    public static String getsecToHrMinOrMinSec(long value, String format1, String format2) {
        long time = Math.round(value);
        long hour = time / 60 / 60;
        String timeValue;
        if (hour > 0) {
            timeValue = String.format(format1, hour, (value - hour * 60 * 60) / 60);//"%02d:%02d"
        } else {
            timeValue = String.format(format2, value / 60, value % 60);//"%02d:%02d"
        }
        return timeValue;
    }
}
