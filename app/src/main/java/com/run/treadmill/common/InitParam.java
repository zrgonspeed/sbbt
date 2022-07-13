package com.run.treadmill.common;

import android.content.Context;

import com.run.treadmill.manager.SpManager;

public class InitParam {
    public static final String APK = "553-54T-08";
    public static final String PROJECT_NAME = "AC00" + APK;
    public static final String APK_NAME = "AC00" + APK + ".apk";
    public static final String LOGO_NAME = "logo.png";

    /**
     * 隐藏音量条时间  单位：ms
     */
    public static final int HIDE_VOICE_TIME = 3000;
    /**
     * warm up 时间
     */
    public static final long WARM_UP_TIME = 180;
    /**
     * cool down 时间
     */
    public static final long COOL_DOWN_TIME = 180;

    /**
     * 最小目标心率
     */
    public static final int MIN_TARGET_HEART_RATE = 80;
    /**
     * 最大目标心率
     */
    public static final int MAX_TARGET_HEART_RATE = 180;

    /**
     * 公制最大速度最大值
     */
    public static final float MAX_SPEED_MAX_METRIC = 24.0f;
    /**
     * 公制最大速度最小值
     */
    public static final float MAX_SPEED_MIN_METRIC = 18.0f;
    /**
     * 公制最大速度预设值
     */
    public static final float DEFAULT_MAX_SPEED_METRIC = 20.0f;//24.0f;

    /**
     * 公制最小速度最大值
     */
    public static final float MIN_SPEED_MAX_METRIC = 2.0f;
    /**
     * 公制最小速度最小值
     */
    public static final float MIN_SPEED_MIN_METRIC = 0.5f;
    /**
     * 公制最小速度预设值
     */
    public static final float DEFAULT_MIN_SPEED_METRIC = 1.0f;//0.5f;

    /**
     * 英制最大速度最大值
     */
    public static final float MAX_SPEED_MAX_IMPERIAL = 15.0f;
    /**
     * 英制最大速度最小值
     */
    public static final float MAX_SPEED_MIN_IMPERIAL = 11.0f;
    /**
     * 英制最大速度预设值
     */
    public static final float DEFAULT_MAX_SPEED_IMPERIAL = 12.5f;//15.0f;

    /**
     * 英制最小速度最大值
     */
    public static final float MIN_SPEED_MAX_IMPERIAL = 1.2f;
    /**
     * 英制最小速度最小值
     */
    public static final float MIN_SPEED_MIN_IMPERIAL = 0.3f;
    /**
     * 英制最小速度预设值
     */
    public static final float DEFAULT_MIN_SPEED_IMPERIAL = 0.6f;//0.3f;

    /**
     * 最大扬升最大值
     */
    public static final int MAX_INCLINE_MAX = 20;
    /**
     * 最大扬升最小值
     */
    public static final int MAX_INCLINE_MIN = 10;
    /**
     * 最大扬升预设值
     */
    public static final int DEFAULT_MAX_INCLINE = 15;//20;

    /**
     * 最大轮径
     */
    public static final float MAX_WHEEL_SIZE = 4.00f;
    /**
     * 最小轮径
     */
    public static final float MIN_WHEEL_SIZE = 1.50f;
    /**
     * 最大速比
     */
    public static final float MAX_SPEED_RATE = 200.0f;
    /**
     * 最小速比
     */
    public static final float MIN_SPEED_RATE = 10.0f;


    /**
     * 最大rpm
     */
    public static final int MAX_RPM = 500;
    /**
     * 最小rpm
     */
    public static final int MIN_RPM = 5;


    public static final int MAX_AGE = 99;
    public static final int MIN_AGE = 10;

    public static final int MAX_WEIGHT_METRIC = 227;
    public static final int MIN_WEIGHT_METRIC = 20;

    public static final int MAX_WEIGHT_IMPERIAL = 500;
    public static final int MIN_WEIGHT_IMPERIAL = 44;

    public static final int MAX_AD = 230;
    public static final int MIN_AD = 16;

    /**
     * AC最小AD值得绝对差距范围
     */
    public static final int ABS_AC_AD = 15;
    /**
     * AA最小AD值得绝对差距范围
     */
    public static final int ABS_AA_AD = 15;

    /**
     * DC最小AD值得绝对差距范围
     */
    public static final int ABS_DC_AD = 5;

    public static final int SLEEP_TIME = 60 * 10;
    public static final int MACHINE_LUBE_TIME = 20;

    /**
     * 最小加油里程
     */
    public static final int MIN_LUBE_DISTANCE = 0;
    /**
     * 最大加油里程
     */
    public static final int MAX_LUBE_DISTANCE = 6000;

    /**
     * 默认轮径（英制）
     */
    public static final float DEFAULT_WHEEL_SIZE = 2.21f;
    /**
     * 默认速率
     */
    public static final float DEFAULT_SPEED_RATE = 67.0f;

    /**
     * 默认rpm
     */
    public static final int DEFAULT_RPM_RATE = 150;


    /**
     * 默认加油里程(公制)
     */
    public static final int DEFAULT_LUBE_DISTANCE = 300;
    /**
     * 默认加油里程(英制)
     */
    public static final int DEFAULT_LUBE_DISTANCE_IMPERIAL = 187;

    /**
     * 最大运动段数
     */
    public static final int TOTAL_RUN_STAGE_NUM = 30;
    /**
     * 默认扬升
     */
    public static final int DEFAULT_INCLINE = 0;
    /**
     * 默认速度
     */
    public static final float DEFAULT_SPEED = 0.8f;
    /**
     * warm up 扬升
     */
    public static final float WARM_UP_INCLIEN = 0f;
    /**
     * warm up 速度（公制）
     */
    public static final float WARM_UP_SPEED_METRIC = 4.8f;
    /**
     * warm up 速度（英制）
     */
    public static final float WARM_UP_SPEED_IMPERIAL = 3f;
    /**
     * cool down 扬升
     */
    public static final float COOL_DOWN_INCLIEN = 0f;

    /**
     * 最大时间（分钟）
     */
    public static final int MAX_TIME_MIN = 99;
    /**
     * 最小时间（分钟）
     */
    public static final int MIN_TIME_MIN = 20;
    /**
     * 最大运动距离
     */
    public static final int MAX_DISTANCE = 99;
    /**
     * 最小运动卡路里
     */
    public static final int MIN_CALORIES = 10;
    /**
     * 最大运动卡路里
     */
    public static final int MAX_CALORIES = 9999;
    /**
     * 默认公制体重
     */
    public static final float DEFAULT_WEIGHT_METRIC = 70;
    /**
     * 默认英制体重
     */
    public static final float DEFAULT_WEIGHT_IMPERIAL = 154;
    /**
     * 默认年龄
     */
    public static final int DEFAULT_AGE = 30;
    /**
     * 默认时间（分钟）
     */
    public static final int DEFAULT_TIME = 20;
    /**
     * 默认目标心率
     */
    public static final int DEFAULT_THR = 80;
    /**
     * 默认性别
     */
    public static final int DEFAULT_GENDER_MALE = 0;
    public static final int DEFAULT_GENDER_FEMALE = 1;

    public static final String SRS_PASS = "1234";
    public static final String CUSTOM_PASS = "0000";

    public static final int MIN_INCLINE = 0;

    /**
     * 是否是测试服务器
     */
    private final static boolean isTestServer = false;
    /**
     * 国外服务器
     */
    public static String NOT_CN_HOST;
    /**
     * 国内服务器
     */
    public static String CN_HOST;

    public static final String UPDATE_THIRD_A133_END = "/restapi/apk/A133/update/treadmill?apkNames=";
    public static final String UPDATE_TREADMILL_END = "/restapi/apk/A133/update/treadmill?apkNames=";

    static {
        if (isTestServer) {
            NOT_CN_HOST = "http://apk-test.anplus-tech.com";
            CN_HOST = "http://apkchina-test.anplus-tech.com";
        } else {
            NOT_CN_HOST = "http://apk.anplus-tech.com";
            CN_HOST = "http://apkchina.anplus-tech.com";
        }
    }

    /*
     *  if (A && B || !A && !B) {
     *       test
     *   } else {
     *       official
     *   }
     *
     * */
    public static String getUpdateHost(Context mContext) {
        String reqUrl;
        if (mContext.getResources().getConfiguration().locale.getCountry().equals("CN")) {
            if (SpManager.getAlterUpdatePath()) {
                reqUrl = NOT_CN_HOST;
            } else {
                reqUrl = CN_HOST;
            }
        } else {
            if (SpManager.getAlterUpdatePath()) {
                reqUrl = CN_HOST;
            } else {
                reqUrl = NOT_CN_HOST;
            }
        }
        return reqUrl;
    }

    public static String getDownloadPath(String url) {
        if (url.contains(InitParam.NOT_CN_HOST)) {
            return CTConstant.DOWNLOAD_PATH_OTHER;
        }

        return CTConstant.DOWNLOAD_PATH_CN;
    }
}
