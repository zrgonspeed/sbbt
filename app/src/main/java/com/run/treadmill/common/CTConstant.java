package com.run.treadmill.common;

import android.os.Environment;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/17
 */
public class CTConstant {
    /**
     * 是否从媒体进入
     */
    public static final String IS_MEDIA = "isMedia";
    /**
     * 媒体的包名
     */
    public static final String PK_NAME = "pkName";
    /**
     * 媒体的类名
     */
    public static final String CLASS_MEDIA = "className";
    /**
     * 是否需要显示 VO2
     */
    public static final String NEED_VO2 = "vo2";
    /**
     * 影片名称
     */
    public static final String VR_PATH_INX = "vrPath";
    /**
     * 影片时长
     */
    public static final String VR_PATH_DURATION = "vrDuration";

    /**
     * 进入Setting界面下的Lock
     */
    public static final String IS_ENTER_LOCK = "isSettingLock";
    /**
     * 进入工程模式几
     */
    public static final String FACTORY_NUM = "factoryNum";
    /**
     * 工程模式不显示报错
     */
    public static final String FACTORY_NO_SHOW_ERR = "factoryNoShowErr";

    /**
     * 下载的文件存储路径
     */
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/treadmill";

    /**
     * program模式下的名称,例子:P1
     */
    public static final String PROGRAM_MODE_NAME = "programModeName";


    public static final int DEVICE_TYPE_DC = 20000;
    public static final int DEVICE_TYPE_AC = 20001;
    /**
     * 下控是积微变频器
     */
    public static final int DEVICE_TYPE_AA = 20002;

    /********************** 运动模式 ************************/
    public static final int QUICKSTART = 10000;
    public static final int GOAL = 10001;
    public static final int HILL = 10002;
    public static final int USER_PROGRAM = 10003;
    public static final int HRC = 10004;
    public static final int FITNESS_TEST = 10005;
    public static final int INTERVAL = 10006;
    public static final int VISION = 10007;
    public static final int PROGRAM = 10008;

    public static final int other = 10010;

    @IntDef({QUICKSTART, GOAL, HILL, USER_PROGRAM, HRC, FITNESS_TEST, INTERVAL, VISION, PROGRAM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RunMode {
    }

    /***********************  运动状态  **************************/
    /**
     * 正常状态
     */
    public static final int RUN_STATUS_NORMAL = 11000;
    /**
     * 准备状态
     */
    public static final int RUN_STATUS_PREPARE = 11001;
    /**
     * warm up状态
     */
    public static final int RUN_STATUS_WARM_UP = 11002;
    /**
     * running状态
     */
    public static final int RUN_STATUS_RUNNING = 11003;
    /**
     * stop状态
     */
    public static final int RUN_STATUS_STOP = 11004;
    /**
     * cool down状态
     */
    public static final int RUN_STATUS_COOL_DOWN = 11005;
    /**
     * 继续状态
     */
    public static final int RUN_STATUS_CONTINUE = 11006;

    @IntDef({RUN_STATUS_NORMAL, RUN_STATUS_PREPARE, RUN_STATUS_WARM_UP, RUN_STATUS_RUNNING, RUN_STATUS_STOP, RUN_STATUS_COOL_DOWN, RUN_STATUS_CONTINUE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RunStatus {
    }

    /***************************  数字键数值类型  ******************************/
    /**
     * 时间
     */
    public static final int TYPE_TIME = 12000;
    /**
     * 距离
     */
    public static final int TYPE_DISTANCE = 12001;
    /**
     * 卡路里
     */
    public static final int TYPE_CALORIES = 12002;
    /**
     * 年龄
     */
    public static final int TYPE_AGE = 12003;
    /**
     * 体重
     */
    public static final int TYPE_WEIGHT = 12004;
    /**
     * 速度
     */
    public static final int TYPE_SPEED = 12005;
    /**
     * 扬升
     */
    public static final int TYPE_INCLINE = 12006;
    /**
     * 最高速度
     */
    public static final int TYPE_FACTORY_HIGH_SPEED = 12007;
    /**
     * 最低速度
     */
    public static final int TYPE_FACTORY_LOW_SPEED = 12008;
    /**
     * 最大扬升
     */
    public static final int TYPE_FACTORY_MAX_INCLINE = 12009;
    /**
     * 轮径
     */
    public static final int TYPE_FACTORY_WHEEL_SIZE = 12010;
    /**
     * lube 距离
     */
    public static final int TYPE_FACTORY_LUBE = 12011;
    /**
     * 目标心率
     */
    public static final int TYPE_THR = 12012;
    /**
     * setting lock
     */
    public static final int TYPE_SETTING_LOCK = 12013;
    /**
     * reset setting lock
     */
    public static final int TYPE_SETTING_LOCK_RESET = 12014;
    /**
     * setting 时间
     */
    public static final int TYPE_SETTING_TIME = 12015;
    /**
     * setting 距离
     */
    public static final int TYPE_SETTING_DISTANCE = 12016;
    /**
     * 速比
     */
    public static final int TYPE_FACTORY_SPEED_RATE = 12017;
    /**
     * rpm
     */
    public static final int TYPE_FACTORY_RPM = 12018;

    @IntDef({TYPE_TIME, TYPE_DISTANCE, TYPE_CALORIES, TYPE_AGE, TYPE_WEIGHT, TYPE_SPEED, TYPE_INCLINE, TYPE_FACTORY_HIGH_SPEED, TYPE_FACTORY_LOW_SPEED,
            TYPE_FACTORY_MAX_INCLINE, TYPE_FACTORY_WHEEL_SIZE, TYPE_FACTORY_LUBE, TYPE_THR, TYPE_SETTING_LOCK, TYPE_SETTING_LOCK_RESET,
            TYPE_SETTING_TIME, TYPE_SETTING_DISTANCE, TYPE_FACTORY_SPEED_RATE, TYPE_FACTORY_RPM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EditType {
    }

    /*****************************  更新提示后的操作  *******************************/
    /**
     * 更新提示窗点yes
     */
    public static final int INSTALL_YES = 13000;
    /**
     * 更新提示窗点no
     */
    public static final int INSTALL_NO = 13001;
    /**
     * 更新提示窗 未决定
     */
    public static final int INSTALL_UNKNOW = 13002;

    @IntDef
    @Retention(RetentionPolicy.SOURCE)
    public @interface Ret {
    }

    /**
     * 虚拟场景跑步视频路径
     */
    public static final String[] vrVideoPath = {
            "/ANPLUS-01.mp4",
            "/ANPLUS-02.mp4",
            "/ANPLUS-03.mp4",
            "/ANPLUS-04.mp4"};

    public static final int NO_SHOW_TIPS = -1;
    public static final int SHOW_TIPS_POINT = 60001;
    /**
     * SHOW_TIPS_MACHINE_LUBE_NULL 与 SHOW_TIPS_MACHINE_LUBE 应该是 同级别
     */
    public static final int SHOW_TIPS_MACHINE_LUBE_NULL = 60002;
    /**
     * SHOW_TIPS_MACHINE_LUBE_NULL 与 SHOW_TIPS_MACHINE_LUBE 应该是 同级别
     */
    public static final int SHOW_TIPS_MACHINE_LUBE = 60003;
    public static final int SHOW_TIPS_LOCK = 60004;
    public static final int SHOW_TIPS_LUBE = 60005;
    public static final int SHOW_TIPS_OTHER_ERROR = 60006;
    public static final int SHOW_TIPS_SAFE_ERROR = 60007;
    public static final int SHOW_TIPS_COMM_ERROR = 60008;
    public static final int SHOW_TIPS_UPDATE = 60009;

    @IntDef({NO_SHOW_TIPS, SHOW_TIPS_POINT,
            SHOW_TIPS_MACHINE_LUBE, SHOW_TIPS_MACHINE_LUBE_NULL,
            SHOW_TIPS_LUBE,
            SHOW_TIPS_LOCK,
            SHOW_TIPS_OTHER_ERROR, SHOW_TIPS_SAFE_ERROR, SHOW_TIPS_COMM_ERROR,
            SHOW_TIPS_UPDATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TipPopType {
    }
}