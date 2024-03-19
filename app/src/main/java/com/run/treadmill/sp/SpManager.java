package com.run.treadmill.sp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.run.treadmill.common.InitParam;
import com.run.treadmill.util.UnitUtil;

/**
 * sharedpreferences管理类
 *
 * @author chenyan
 */
public class SpManager {
    private static final String ERROR = "set_error";
    /**
     * xml文件名
     */
    public static final String SETTINGS_TREADMILL = "StorageParam_treadmill";
    /**
     * 设置custom pass
     */
    private static final String SET_CUSTOM_PASS = "set_custom_pass";
    /**
     * 设置srs pass
     */
    private static final String SET_SRS_PASS = "set_srs_pass";

    /**
     * 设置已经跑步时间
     */
    private static final String SET_RUN_TOTAL_TIME = "set_run_total_time";

    /**
     * 设置已经跑步距离
     */
    private static final String SET_RUN_TOTAL_DIS = "set_run_total_dis";
    private static final String SET_RUN_TOTAL_DIS_IMPERIAL = "set_run_total_dis_imperial";

    /**
     * 备份最大的时间
     */
    private static final String SET_BAK_MAX_TIME = "set_bak_max_time";
    /**
     * 备份已经运动的时间
     */
    private static final String SET_BAK_RUN_TIME = "set_bak_run_time";

    /**
     * 备份最大的里程
     */
    private static final String SET_BAK_MAX_DIS = "set_bak_max_dis";
    private static final String SET_BAK_MAX_DIS_IMPERIAL = "set_bak_max_dis_imperial";
    /**
     * 备份已经跑过的里程
     */
    private static final String SET_BAK_RUN_DIS = "set_bak_run_dis";
    private static final String SET_BAK_RUN_DIS_IMPERIAL = "set_bak_run_dis_imperial";

    /**
     * 设置最大加油Lube
     */
    private static final String SET_MAX_LUBE = "set_max_lube";
    private static final String SET_MAX_LUBE_IMPERIAL = "set_max_lube_imperial";
    /**
     * 设置已经运行的lube
     */
    private static final String SET_RUN_LUBE = "set_run_lube";
    private static final String SET_RUN_LUBE_IMPERIAL = "set_run_lube_imperial";

    /**
     * 设置logo类型 是否有更新过 有则使用更新后的logo,没有则使用原始logo
     */
    private static final String SET_LOGO_TYPE = "set_logo_type";

    /**
     * 设置Unit 单位
     */
    private static final String SET_UNIT = "set_unit";

    /**
     * 设置ncu版本信息 年
     */
    private static final String SET_NCU_YEAR = "set_ncu_year";
    /**
     * 设置ncu版本信息 月日
     */
    private static final String SET_NCU_MONTH_DAY = "set_ncu_month_day";
    /**
     * 设置ncu版本信息 版本号
     */
    private static final String SET_NCU_NUM = "set_ncu_num";

    /**
     * 设置buzzer开关
     */
    private static final String SET_BUZZER = "set_buzzer";

    /**
     * 设置公制最大速度
     */
    private static final String SET_MAX_SPEED_METRIC = "set_max_speed_metric";
    /**
     * 设置公制最小速度
     */
    private static final String SET_MIN_SPEED_METRIC = "set_min_speed_metric";

    /**
     * 设置英制最大速度
     */
    private static final String SET_MAX_SPEED_IMPERIAL = "set_max_speed_imperial";

    /**
     * 设置英制最小速度
     */
    private static final String SET_MIN_SPEED_IMPERIAL = "set_min_speed_imperial";

    /**
     * 设置轮径尺寸
     */
    private static final String SET_WHEEL_SIZE = "set_wheel_size";

    /**
     * 设置速比
     */
    private static final String SET_SPEED_RATE = "set_speed_rate";

    /**
     * 设置速比
     */
    private static final String SET_RPM_RATE = "set_rpm_rate";

    /**
     * 设置最大扬升
     */
    private static final String SET_MAX_INCLINE = "set_max_incline";

    /**
     * 设置最大AD值
     */
    private static final String SET_MAX_AD = "set_max_ad";

    /**
     * 设置最小AD值
     */
    private static final String SET_MIN_AD = "set_min_ad";
    /**
     * 睡眠
     */
    private static final String SET_SLEEP = "set_sleep";
    /**
     * 常亮
     */
    private static final String SET_DISPLAY = "set_display";
    /**
     * 是否停止扬升
     */
    private static final String SET_GS_INCLINE = "set_gs_incline";
    /**
     * 登录
     */
    private static final String SET_LOGIN_CTRL = "set_login_ctrl";

    /**
     * 用户设置的运动参数
     */
    private static final String SET_USER_RUN_INFO = "set_user_run_info";
    /**
     * 最后一次的app版本
     */
    private static final String SET_LAST_APP_VERSION_NAME = "set_last_app_version_name";
    /**
     * 是否安装app后启动
     */
    private static final String SET_INSTALL_OPEN = "set_install_open";
    /**
     * 记录初始化语言、亮度、声音
     */
    private static final String INIT_LANGUAGE_SOUND_BRIGHTNESS = "init_language_sound_brightness";
    /**
     * 是否已经安装下载的apk
     */
    private static final String SET_UPDATE_IS_NETWORK = "set_update_is_network";

    /**
     * 是否切换更新路径
     */
    private static final String SET_ALTER_UPDATE_PATH = "set_alter_update_path";

    /**
     * 是否切换语言
     */
    private static final String SET_CHANGED_LANGUAGE = "set_changed_language";

    /**
     * 语言切换
     */
    private static final String SET_LANGUAGE = "set_language";

    public static void setError(String error) {
        StorageParam.setParam(ERROR, error);
    }

    public static String getError() {
        return StorageParam.getParam(ERROR, "no error");
    }

    public static void init(@NonNull Context c) {
        StorageParam.setContext(c);
        StorageParam.setSpName(SETTINGS_TREADMILL);
    }

    /**
     * 重设总运动时间(hr)
     *
     * @param time
     */
    public static void resetRunTotalTime(long time) {
        StorageParam.setParam(SET_RUN_TOTAL_TIME, time * 60L * 60L);
        StorageParam.setParam(SET_BAK_RUN_TIME, 0L);
    }

    /**
     * 当前这一秒运动时间 秒
     *
     * @param time
     */
    public static void setRunTime(long time) {
        StorageParam.setParam(SET_RUN_TOTAL_TIME, getRunTotalTime() + time);

        if (getBackUpRunTime() >= getBackUpRunTotalTime()) {
            return;
        }
        if ((getBackUpRunTime() + time) > getBackUpRunTotalTime()) {
            StorageParam.setParam(SET_BAK_RUN_TIME, getBackUpRunTotalTime());
        } else {
            StorageParam.setParam(SET_BAK_RUN_TIME, getBackUpRunTime() + time);
        }
    }

    /**
     * 统一保存运动数据（每一秒）
     *
     * @param runLube 当前这一秒跑的距离
     * @param dis     距离
     */
    public static void setRunData(float runLube, float dis) {
        SharedPreferences.Editor ed = StorageParam.getEditor();

        if (getRunLubeDis() < getMaxLubeDis()) {
            if ((getRunLubeDis() + runLube) > getMaxLubeDis()) {

                ed.putFloat(SET_RUN_LUBE, getIsMetric() ? (float) getMaxLubeDis() : UnitUtil.getMileToKm((float) getMaxLubeDis()));
                ed.putFloat(SET_RUN_LUBE_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile((float) getMaxLubeDis()) : (float) getMaxLubeDis());
            } else {
                ed.putFloat(SET_RUN_LUBE, getIsMetric() ? (getRunLubeDis() + runLube) : UnitUtil.getMileToKm(getRunLubeDis() + runLube));
                ed.putFloat(SET_RUN_LUBE_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile(getRunLubeDis() + runLube) : (getRunLubeDis() + runLube));
            }
        }
        ed.putFloat(SET_RUN_TOTAL_DIS, getRunTotalDis() + (getIsMetric() ? dis : UnitUtil.getMileToKm(dis)));
        ed.putFloat(SET_RUN_TOTAL_DIS_IMPERIAL, getRunTotalDis() + (getIsMetric() ? UnitUtil.getMileToKm(dis) : dis));

        if (getBackUpRunDis() < getBackUpTotalRunDis()) {
            if ((getBackUpRunDis() + dis) > getBackUpTotalRunDis()) {
                ed.putFloat(SET_BAK_RUN_DIS, getIsMetric() ? getBackUpTotalRunDis() : UnitUtil.getMileToKm(getBackUpTotalRunDis()));
                ed.putFloat(SET_BAK_RUN_DIS_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile(getBackUpTotalRunDis()) : getBackUpTotalRunDis());
            } else {
                ed.putFloat(SET_BAK_RUN_DIS, getIsMetric() ? (getBackUpRunDis() + dis) : UnitUtil.getMileToKm(getBackUpRunDis() + dis));
                ed.putFloat(SET_BAK_RUN_DIS_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile(getBackUpRunDis() + dis) : (getBackUpRunDis() + dis));
            }
        }
        ed.putLong(SET_RUN_TOTAL_TIME, getRunTotalTime() + 1L);
        if (getBackUpRunTime() < getBackUpRunTotalTime()) {
            if ((getBackUpRunTime() + 1L) > getBackUpRunTotalTime()) {
                ed.putLong(SET_BAK_RUN_TIME, getBackUpRunTotalTime());
            } else {
                ed.putLong(SET_BAK_RUN_TIME, getBackUpRunTime() + 1L);
            }
        }

        ed.commit();
    }

    /**
     * @param dis  某一时间段内记录距离
     * @param time 时长(单位:秒)
     */
    public static void setRunData(float dis, long time) {
        SharedPreferences.Editor ed = StorageParam.getEditor();

        if (getRunLubeDis() < getMaxLubeDis()) {
            if ((getRunLubeDis() + dis) > getMaxLubeDis()) {

                ed.putFloat(SET_RUN_LUBE, getIsMetric() ?
                        (float) getMaxLubeDis() : UnitUtil.getMileToKm((float) getMaxLubeDis()));
                ed.putFloat(SET_RUN_LUBE_IMPERIAL, getIsMetric() ?
                        UnitUtil.getKmToMile((float) getMaxLubeDis()) : (float) getMaxLubeDis());

            } else {

                ed.putFloat(SET_RUN_LUBE, getIsMetric() ?
                        (getRunLubeDis() + dis) : UnitUtil.getMileToKm(getRunLubeDis() + dis));
                ed.putFloat(SET_RUN_LUBE_IMPERIAL, getIsMetric() ?
                        UnitUtil.getKmToMile(getRunLubeDis() + dis) : (getRunLubeDis() + dis));

            }
        }
        ed.putFloat(SET_RUN_TOTAL_DIS, getRunTotalDis() + (getIsMetric() ? dis : UnitUtil.getMileToKm(dis)));
        ed.putFloat(SET_RUN_TOTAL_DIS_IMPERIAL, getRunTotalDis() + (getIsMetric() ? UnitUtil.getMileToKm(dis) : dis));

        if (getBackUpRunDis() < getBackUpTotalRunDis()) {

            if ((getBackUpRunDis() + dis) > getBackUpTotalRunDis()) {

                ed.putFloat(SET_BAK_RUN_DIS, getIsMetric() ?
                        getBackUpTotalRunDis() : UnitUtil.getMileToKm(getBackUpTotalRunDis()));
                ed.putFloat(SET_BAK_RUN_DIS_IMPERIAL, getIsMetric() ?
                        UnitUtil.getKmToMile(getBackUpTotalRunDis()) : getBackUpTotalRunDis());

            } else {

                ed.putFloat(SET_BAK_RUN_DIS, getIsMetric() ?
                        (getBackUpRunDis() + dis) : UnitUtil.getMileToKm(getBackUpRunDis() + dis));
                ed.putFloat(SET_BAK_RUN_DIS_IMPERIAL, getIsMetric() ?
                        UnitUtil.getKmToMile(getBackUpRunDis() + dis) : (getBackUpRunDis() + dis));

            }
        }

        ed.putLong(SET_RUN_TOTAL_TIME, getRunTotalTime() + time);

        if (getBackUpRunTime() < getBackUpRunTotalTime()) {
            if ((getBackUpRunTime() + time) > getBackUpRunTotalTime()) {
                ed.putLong(SET_BAK_RUN_TIME, getBackUpRunTotalTime());
            } else {
                ed.putLong(SET_BAK_RUN_TIME, getBackUpRunTime() + time);
            }
        }

        ed.commit();
    }

    public static long getRunTotalTime() {
        return StorageParam.getParam(SET_RUN_TOTAL_TIME, 0L);
    }

    /**
     * 重设总运动距离
     *
     * @param dis
     */
    public static void resetRunTotalDis(float dis) {
        StorageParam.setParam(SET_RUN_TOTAL_DIS, getIsMetric() ? dis : UnitUtil.getMileToKm(dis));
        StorageParam.setParam(SET_RUN_TOTAL_DIS_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile(dis) : dis);

        StorageParam.setParam(SET_BAK_RUN_DIS, 0f);
        StorageParam.setParam(SET_BAK_RUN_DIS_IMPERIAL, 0f);
    }

    /**
     * 当前这一秒跑的距离
     *
     * @param dis 距离
     */
    public static void setRunDis(float dis) {
        StorageParam.setParam(SET_RUN_TOTAL_DIS, getRunTotalDis() + (getIsMetric() ? dis : UnitUtil.getMileToKm(dis)));
        StorageParam.setParam(SET_RUN_TOTAL_DIS_IMPERIAL, getRunTotalDis() + (getIsMetric() ? UnitUtil.getMileToKm(dis) : dis));

        if (getBackUpRunDis() >= getBackUpTotalRunDis()) {
            return;
        }
        if ((getBackUpRunDis() + dis) > getBackUpTotalRunDis()) {
            StorageParam.setParam(SET_BAK_RUN_DIS, getIsMetric() ? getBackUpTotalRunDis() : UnitUtil.getMileToKm(getBackUpTotalRunDis()));
            StorageParam.setParam(SET_BAK_RUN_DIS_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile(getBackUpTotalRunDis()) : getBackUpTotalRunDis());
        } else {
            StorageParam.setParam(SET_BAK_RUN_DIS, getIsMetric() ? (getBackUpRunDis() + dis) : UnitUtil.getMileToKm(getBackUpRunDis() + dis));
            StorageParam.setParam(SET_BAK_RUN_DIS_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile(getBackUpRunDis() + dis) : (getBackUpRunDis() + dis));
        }
    }

    public static float getRunTotalDis() {
        return StorageParam.getParam(getIsMetric() ? SET_RUN_TOTAL_DIS : SET_RUN_TOTAL_DIS_IMPERIAL, 0f);
    }

    /**
     * 根据公英制获取总运动距离
     *
     * @param isMetric
     * @return
     */
    public static float getRunTotalDisByMetric(boolean isMetric) {
        return StorageParam.getParam(isMetric ? SET_RUN_TOTAL_DIS : SET_RUN_TOTAL_DIS_IMPERIAL, 0f);
    }

    /**
     * 重设 设置界面最大运动时间（hr）
     *
     * @param time
     */
    public static void resetBackUpRunTotalTime(long time) {
        StorageParam.setParam(SET_BAK_MAX_TIME, time * 60L * 60L);
        StorageParam.setParam(SET_BAK_RUN_TIME, 0L);
    }

    public static long getBackUpRunTotalTime() {
        return StorageParam.getParam(SET_BAK_MAX_TIME, 0L);
    }

    public static long getBackUpRunTime() {
        return StorageParam.getParam(SET_BAK_RUN_TIME, 0L);
    }

    /**
     * 重设 设置界面最大运动距离
     *
     * @param dis
     */
    public static void resetBackUpRunTotalDis(float dis) {
        StorageParam.setParam(SET_BAK_MAX_DIS, getIsMetric() ? dis : UnitUtil.getMileToKm(dis));
        StorageParam.setParam(SET_BAK_MAX_DIS_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile(dis) : dis);

        StorageParam.setParam(SET_BAK_RUN_DIS, 0f);
        StorageParam.setParam(SET_BAK_RUN_DIS_IMPERIAL, 0f);
    }

    public static float getBackUpTotalRunDis() {
        return StorageParam.getParam(getIsMetric() ? SET_BAK_MAX_DIS : SET_BAK_MAX_DIS_IMPERIAL, 0f);
    }

    public static float getBackUpRunDis() {
        return StorageParam.getParam(getIsMetric() ? SET_BAK_RUN_DIS : SET_BAK_RUN_DIS_IMPERIAL, 0f);
    }

    public static int getMaxLubeDis() {
        return StorageParam.getParam(getIsMetric() ? SET_MAX_LUBE : SET_MAX_LUBE_IMPERIAL, getIsMetric() ? InitParam.DEFAULT_LUBE_DISTANCE : InitParam.DEFAULT_LUBE_DISTANCE_IMPERIAL);
    }

    /**
     * 设置最大加油里程
     *
     * @param maxLube （公制）
     */
    public static void setMaxLubeDis(int maxLube) {
        StorageParam.setParam(SET_MAX_LUBE, getIsMetric() ? maxLube : UnitUtil.getFloatToIntClear(UnitUtil.getMileToKm(maxLube)));
        StorageParam.setParam(SET_MAX_LUBE_IMPERIAL, getIsMetric() ? UnitUtil.getFloatToIntUp(UnitUtil.getKmToMile(maxLube)) : maxLube);
    }

    public static float getRunLubeDis() {
        return StorageParam.getParam(getIsMetric() ? SET_RUN_LUBE : SET_RUN_LUBE_IMPERIAL, 0f);
    }

    /**
     * 重置已经运行的lube
     *
     * @param runLube 当前这一秒跑的距离
     */
    public static void reSetRunLubeDis(float runLube) {
        StorageParam.setParam(SET_RUN_LUBE, getIsMetric() ? runLube : UnitUtil.getMileToKm(runLube));
        StorageParam.setParam(SET_RUN_LUBE_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile(runLube) : runLube);
    }

    /**
     * 设置已经运行的lube
     *
     * @param runLube 当前这一秒跑的距离
     */
    public static void setRunLubeDis(float runLube) {
        if (getRunLubeDis() >= getMaxLubeDis()) {
            return;
        }
        if ((getRunLubeDis() + runLube) > getMaxLubeDis()) {
            StorageParam.setParam(SET_RUN_LUBE, getIsMetric() ? (float) getMaxLubeDis() : UnitUtil.getMileToKm((float) getMaxLubeDis()));
            StorageParam.setParam(SET_RUN_LUBE_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile((float) getMaxLubeDis()) : (float) getMaxLubeDis());
        } else {
            StorageParam.setParam(SET_RUN_LUBE, getIsMetric() ? (getRunLubeDis() + runLube) : UnitUtil.getMileToKm(getRunLubeDis() + runLube));
            StorageParam.setParam(SET_RUN_LUBE_IMPERIAL, getIsMetric() ? UnitUtil.getKmToMile(getRunLubeDis() + runLube) : (getRunLubeDis() + runLube));
        }
    }

    /**
     * 是否为内部logo
     *
     * @return 结果
     */
    public static boolean getIsInnerLogo() {
        return StorageParam.getParam(SET_LOGO_TYPE, true);
    }

    public static void setIsInnerLogo(boolean inner) {
        StorageParam.setParam(SET_LOGO_TYPE, inner);
    }


    public static boolean getIsMetric() {
        return StorageParam.getParam(SET_UNIT, true);
    }

    /**
     * 设置公英制
     *
     * @param isMetric 是否为公制
     */
    public static void setIsMetric(boolean isMetric) {
        StorageParam.setParam(SET_UNIT, isMetric);
    }

    public static String getNcuVer() {
        return getNcuYear() + getNcuMonthDay() + getNcuNum();
    }

    public static void setNcuYear(String year) {
        StorageParam.setParam(SET_NCU_YEAR, year);
    }

    public static String getNcuYear() {
        return StorageParam.getParam(SET_NCU_YEAR, "2020");
    }

    public static void setNcuMonthDay(String month_day) {
        StorageParam.setParam(SET_NCU_MONTH_DAY, month_day);
    }

    public static String getNcuMonthDay() {
        return StorageParam.getParam(SET_NCU_MONTH_DAY, "1102");
    }

    public static void setNcuNum(String num) {
        StorageParam.setParam(SET_NCU_NUM, num);
    }

    public static String getNcuNum() {
        return StorageParam.getParam(SET_NCU_NUM, "V10");
    }

    public static boolean getBuzzer() {
        return StorageParam.getParam(SET_BUZZER, true);
    }

    public static void setBuzzer(boolean buzzer) {
        StorageParam.setParam(SET_BUZZER, buzzer);
    }

    public static void setSleep(boolean sleep) {
        StorageParam.setParam(SET_SLEEP, sleep);
    }

    public static boolean getSleep() {
        return StorageParam.getParam(SET_SLEEP, true);
    }

    public static void setDisplay(boolean display) {
        StorageParam.setParam(SET_DISPLAY, display);
    }

    public static boolean getDisplay() {
        return StorageParam.getParam(SET_DISPLAY, false);
    }

    public static void setGSMode(boolean incline) {
        StorageParam.setParam(SET_GS_INCLINE, incline);
    }

    public static boolean getGSMode() {
        return StorageParam.getParam(SET_GS_INCLINE, true);
    }

    /**
     * 获取最大速度
     *
     * @param isMetric 是否获取公制速度
     * @return isMetric?公制:英制
     */
    public static float getMaxSpeed(boolean isMetric) {
        if (isMetric) {
            return StorageParam.getParam(SET_MAX_SPEED_METRIC, InitParam.DEFAULT_MAX_SPEED_METRIC);
        } else {
            return StorageParam.getParam(SET_MAX_SPEED_IMPERIAL, InitParam.DEFAULT_MAX_SPEED_IMPERIAL);
        }
    }

    public static float getMaxSpeed() {
        return getMaxSpeed(getIsMetric());
    }

    /**
     * 设置最大速度
     *
     * @param isMetric 是否获取公制速度
     */
    public static void setMaxSpeed(float speed, boolean isMetric) {
        if (isMetric) {
            StorageParam.setParam(SET_MAX_SPEED_METRIC, speed);
        } else {
            StorageParam.setParam(SET_MAX_SPEED_IMPERIAL, speed);
        }
    }

    /**
     * 设置最小速度
     *
     * @param speed    速度
     * @param isMetric speed是否为公制速度
     */
    public static void setMinSpeed(float speed, boolean isMetric) {
        if (isMetric) {
            StorageParam.setParam(SET_MIN_SPEED_METRIC, speed);
        } else {
            StorageParam.setParam(SET_MIN_SPEED_IMPERIAL, speed);
        }
    }

    /**
     * 获取最小速度
     *
     * @param isMetric 是否获取公制速度
     * @return isMetric?公制:英制
     */
    public static float getMinSpeed(boolean isMetric) {
        if (isMetric) {
            return StorageParam.getParam(SET_MIN_SPEED_METRIC, InitParam.DEFAULT_MIN_SPEED_METRIC);
        } else {
            return StorageParam.getParam(SET_MIN_SPEED_IMPERIAL, InitParam.DEFAULT_MIN_SPEED_IMPERIAL);
        }
    }

    public static float getMinSpeed() {
        return getMinSpeed(getIsMetric());
    }

    // 设置轮径尺寸
    public static float getWheelSize() {
        return StorageParam.getParam(SET_WHEEL_SIZE, InitParam.DEFAULT_WHEEL_SIZE);
    }

    public static void setWheelSize(float wheelSize) {
        StorageParam.setParam(SET_WHEEL_SIZE, wheelSize);
    }

    public static float getSpeedRate() {
        return StorageParam.getParam(SET_SPEED_RATE, InitParam.DEFAULT_SPEED_RATE);
    }

    public static void setSpeedRate(float speedRate) {
        StorageParam.setParam(SET_SPEED_RATE, speedRate);
    }

    public static int getRpmRate() {
        return StorageParam.getParam(SET_RPM_RATE, InitParam.DEFAULT_RPM_RATE);
    }

    public static void setRpmRate(int speedRate) {
        StorageParam.setParam(SET_RPM_RATE, speedRate);
    }

    public static int getMaxIncline() {
        return StorageParam.getParam(SET_MAX_INCLINE, InitParam.DEFAULT_MAX_INCLINE);
    }

    public static void setMaxIncline(int incline) {
        StorageParam.setParam(SET_MAX_INCLINE, incline);
    }

    public static int getMaxAd() {
        return StorageParam.getParam(SET_MAX_AD, InitParam.MAX_AD);
    }

    public static void setMaxAd(int maxAd) {
        StorageParam.setParam(SET_MAX_AD, maxAd);
    }

    public static int getMinAd() {
        return StorageParam.getParam(SET_MIN_AD, InitParam.MIN_AD);
    }

    public static void setMinAd(int minAd) {
        StorageParam.setParam(SET_MIN_AD, minAd);
    }

    public static String getCustomPass() {
        return StorageParam.getParam(SET_CUSTOM_PASS, InitParam.CUSTOM_PASS);
    }

    public static void setCustomPass(String customPass) {
        StorageParam.setParam(SET_CUSTOM_PASS, customPass);
    }

    public static String getSrsPass() {
        return StorageParam.getParam(SET_SRS_PASS, InitParam.SRS_PASS);
    }

    public static void setUserRunInfo(String userInfo) {
        StorageParam.setParam(SET_USER_RUN_INFO, userInfo);
    }

    public static String getUserRunInfo() {
        return StorageParam.getParam(SET_USER_RUN_INFO, null);
    }

    /**
     * 删除用户运动设置信息
     */
    public static void removeUserRunInfo() {
        StorageParam.remove(SET_USER_RUN_INFO);
    }

    /**
     * 设置app最后一次的版本
     *
     * @param versionName
     */
    public static void setLastAppVersionName(String versionName) {
        StorageParam.setParam(SET_LAST_APP_VERSION_NAME, versionName);
    }

    /**
     * 获取app最后一次的版本
     *
     * @return
     */
    public static String getLastAppVersionName() {
        return StorageParam.getParam(SET_LAST_APP_VERSION_NAME, "");
    }

    public static void setInstallOpen(boolean isInstall) {
        StorageParam.setParam(SET_INSTALL_OPEN, isInstall);
    }

    public static boolean getInstallOpen() {
        return StorageParam.getParam(SET_INSTALL_OPEN, false);
    }

    public static void setInitLanguageSoundBrightness(boolean isInstall) {
        StorageParam.setParam(INIT_LANGUAGE_SOUND_BRIGHTNESS, isInstall);
    }

    public static boolean getInitLanguageSoundBrightness() {
        return StorageParam.getParam(INIT_LANGUAGE_SOUND_BRIGHTNESS, false);
    }

    public static void setUpdateIsNetwork(boolean isNetwork) {
        StorageParam.setParam(SET_UPDATE_IS_NETWORK, isNetwork);
    }

    public static boolean getUpdateIsNetwork() {
        return StorageParam.getParam(SET_UPDATE_IS_NETWORK, false);
    }

    public static void setAlterUpdatePath(boolean isAlter) {
        StorageParam.setParam(SET_ALTER_UPDATE_PATH, isAlter);
    }

    public static boolean getAlterUpdatePath() {
        return StorageParam.getParam(SET_ALTER_UPDATE_PATH, false);
    }

    public static void setChangedServer(boolean isAlter) {
        StorageParam.setParam(SET_CHANGED_LANGUAGE, isAlter);
    }

    public static boolean getChangedServer() {
        return StorageParam.getParam(SET_CHANGED_LANGUAGE, false);
    }

    public static void setLanguage(String language) {
        StorageParam.setParam(SET_LANGUAGE, language);
    }

    /**
     * zh de en fr es pt
     */
    public static String getLanguage() {
        return StorageParam.getParam(SET_LANGUAGE, "en");
    }

}