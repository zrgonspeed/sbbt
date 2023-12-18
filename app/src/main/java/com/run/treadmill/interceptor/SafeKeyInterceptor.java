package com.run.treadmill.interceptor;

import android.os.Message;

import com.run.serial.SerialUtils;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.manager.control.NormalParam;

/**
 * @Description 安全key的错误拦截
 * @Author GaleLiu
 * @Time 2019/09/12
 */
public class SafeKeyInterceptor implements SerialInterceptor {
    private Message msg;

    @Override
    public Message intercept(Chain chain) {
        byte[] data = ((RealChain) chain).getmData();
        int safeError = ((RealChain) chain).resolveDate(data, NormalParam.SAFE_ERROR_INX, NormalParam.SAFE_ERROR_LEN);
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            //AC 机种 25错误也归为安全key 错误
            //这个行为 后续如果被发现 需要修改
            int otherError = ((RealChain) chain).resolveDate(data, NormalParam.SYS_ERROR_INX, NormalParam.SYS_ERROR_LEN);
            if (otherError == ErrorManager.ERR_SAFE_FC_ERROR) {
                safeError = ErrorManager.ERR_SAFE_FC_ERROR;
            }
        }
        if (safeError != ErrorManager.ERR_NO_ERROR) {
            if (!ErrorManager.getInstance().isSafeError) {
                ErrorManager.getInstance().lastError = ErrorManager.getInstance().errStatus;
                ErrorManager.getInstance().errStatus = ErrorManager.ERR_SAFE_ERROR;
                ErrorManager.getInstance().isSafeError = true;

                ControlManager.getInstance().emergencyStop();
                SerialUtils.getInstance().stopResend();

                if (!SpManager.getGSMode()) {
                    ControlManager.getInstance().resetIncline();
                }
            }

            if (ErrorManager.getInstance().errorDelayTime != ErrorManager.SAFE_DELAY_TIME) {
                ErrorManager.getInstance().errorDelayTime = ErrorManager.SAFE_DELAY_TIME;
            }
            return getMsg(((RealChain) chain).isInOnSleep(), ((RealChain) chain).resolveDate(data, NormalParam.KEY_VALUE_INX, NormalParam.KEY_VALUE_LEN));
        }
        if (ErrorManager.getInstance().isSafeError) {
            ErrorManager.getInstance().errorDelayTime--;
        }
        if (ErrorManager.getInstance().errorDelayTime > 0) {
            return getMsg(((RealChain) chain).isInOnSleep(), ((RealChain) chain).resolveDate(data, NormalParam.KEY_VALUE_INX, NormalParam.KEY_VALUE_LEN));
        }
        SerialUtils.getInstance().resetSend();
        return chain.procced(data, ((RealChain) chain).isInOnSleep());
    }

    private Message getMsg(boolean isInOnSleep, int curKeyValue) {
        msg = new Message();
        msg.what = ErrorManager.ERR_SAFE_ERROR;
        //如果项目的休眠模式为假休眠(只是单纯关闭屏幕),需要额外处理按键唤醒问题
//        if (curKeyValue != 0) {
//            Logger.d("========按键值==========" + curKeyValue);
//        }
        if (isInOnSleep && curKeyValue != 0) {
            msg.arg1 = curKeyValue;
        }
        return msg;
    }
}