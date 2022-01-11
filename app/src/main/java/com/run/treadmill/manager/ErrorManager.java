package com.run.treadmill.manager;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.interceptor.SerialInterceptor;

public class ErrorManager {


    public static final int ERR_NO_ERROR = 0x00;

    /**
     * 安全key错误
     */
    public static final int ERR_SAFE_ERROR = -2;
    /**
     * AC 变频器导致的安全key错误
     */
    public static final int ERR_SAFE_FC_ERROR = 0x25;
    /**
     * 扬升调整错误
     */
    public static int ERR_INCLINE_ADJUST = 0x10;
    /**
     * 扬升校正错误
     */
    public static int ERR_INCLINE_CALIBRATE = 0x11;

    /**
     * A33或者T3与转接板通信超时 222
     */
    public static int ERR_TIME_OUT = 0xDE;
    /**
     * 命令执行失败
     */
    public static int ERR_CMD_FAIL = 0x17;

    public static final int SAFE_DELAY_TIME = 30;

    /**
     * 当前错误
     */
    public int errStatus = ERR_NO_ERROR;

    private static ErrorManager ourInstance = null;

    public boolean isSafeError = false;
    /**
     * 是否有扬升错误，key会清掉扬升错误后一段时间返回，需要记录，
     * 只有上电和校正可清掉扬升
     */
    public boolean hasInclineError = false;
    /**
     * 记录高速移除安全key最后的速度
     */
    public int lastSpeed;

    /**
     * 根据实际情况 拉安全key之后会出现清除错误的情况,这时候该数值就会起到作用
     * {@link com.run.treadmill.interceptor.ErrorInterceptor#intercept(SerialInterceptor.Chain)}
     * 保存最后的非安全key错误，针对DC安全key会把错误清掉，一般只能被别的错误覆盖，不会变0
     */
    public int lastError;

    /**
     * 瞬间的错误进行延时，不然跳界面卡顿会出现误操作
     */
    public int errorDelayTime;

    /**
     * 离开界面的错误 必须回到home界面才会被消除
     */
    public boolean exitError = false;

    private ErrorManager() {

    }

    public static ErrorManager getInstance() {
        if (null == ourInstance) {
            synchronized (ErrorManager.class) {
                if (null == ourInstance) {
                    ourInstance = new ErrorManager();
                }
            }
        }
        return ourInstance;
    }

    public static void init(int type) {
        switch (type) {
            default:
                resetDC();
                break;
            case CTConstant.DEVICE_TYPE_AC:
                resetAC();
                break;
            case CTConstant.DEVICE_TYPE_AA:
                resetAA();
                break;
            case CTConstant.DEVICE_TYPE_DC:
                resetDC();
                break;
        }
    }

    private static void resetAC() {
        ERR_INCLINE_ADJUST = 0x85;
        ERR_INCLINE_CALIBRATE = 0x87;

        ERR_TIME_OUT = 0xDE;

        ERR_CMD_FAIL = 0x17;
    }

    private static void resetAA() {
        ERR_INCLINE_ADJUST = 0x85;
        ERR_INCLINE_CALIBRATE = 0x87;

        ERR_TIME_OUT = 0xDE;

        ERR_CMD_FAIL = 0x17;
    }

    private static void resetDC() {
        ERR_INCLINE_ADJUST = 0x10;
        ERR_INCLINE_CALIBRATE = 0x11;

        ERR_TIME_OUT = 0xDE;

        ERR_CMD_FAIL = 0x17;
    }

    /**
     * 是否为扬升错误
     *
     * @return
     */
    public boolean isInclineError() {
        return (errStatus == ERR_INCLINE_ADJUST);
    }

    public boolean isInclineError(int error) {
        return (error == ERR_INCLINE_ADJUST);
    }

    /**
     * 是否存在扬升错误
     *
     * @return
     */
    public boolean isHasInclineError() {
        return hasInclineError;
    }

    /**
     * 判断是否是非扬升错诶
     *
     * @param error
     * @return
     */
    public boolean isNoInclineError(int error) {
        return (error != ERR_NO_ERROR && error != ERR_INCLINE_ADJUST);
    }

    /**
     * 判断是否是非扬升错
     *
     * @return
     */
    public boolean isNoInclineError() {
        return (errStatus != ERR_NO_ERROR && errStatus != ERR_INCLINE_ADJUST);
    }

    public @CTConstant.TipPopType
    int getErrorTip() {
        if (isSafeError) {
            return CTConstant.SHOW_TIPS_SAFE_ERROR;
        } else if (errStatus == ERR_TIME_OUT) {
            return CTConstant.SHOW_TIPS_COMM_ERROR;
        } else if (isNoInclineError(errStatus)) {
            return CTConstant.SHOW_TIPS_OTHER_ERROR;
        } else {
            return CTConstant.NO_SHOW_TIPS;
        }
    }
}