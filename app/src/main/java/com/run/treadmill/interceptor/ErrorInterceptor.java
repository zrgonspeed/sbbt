package com.run.treadmill.interceptor;

import android.os.Message;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.MsgWhat;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.control.NormalParam;

public class ErrorInterceptor implements SerialInterceptor {

    @Override
    public Message intercept(Chain chain) {
        byte[] data = ((RealChain) chain).getmData();
        int curSysError = ((RealChain) chain).resolveDate(data, NormalParam.SYS_ERROR_INX, NormalParam.SYS_ERROR_LEN);
        //curSysError = 0;
        //TODO: 注意--> CTConstant.DEVICE_TYPE_AA 强制屏蔽部分错误,后面是否删除,待议
//        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
//            if (curSysError == 0x0C) {
//                curSysError = 0;
//            }
//        }

        if (curSysError != ErrorManager.getInstance().errStatus) {
            //TODO:处理安全key清除错误问题
            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                if (curSysError == ErrorManager.ERR_NO_ERROR && ErrorManager.getInstance().lastError != ErrorManager.ERR_NO_ERROR) {
                    ErrorManager.getInstance().errStatus = ErrorManager.getInstance().lastError;
                } else {
                    ErrorManager.getInstance().errStatus = curSysError;
                    ErrorManager.getInstance().lastError = curSysError;
                }
            }
        }

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {//暂时屏蔽不报扬升出错
            int curInclineError = ((RealChain) chain).resolveDate(data, NormalParam.INCLINE_ERROR_INX, NormalParam.INCLINE_ERROR_LEN);
            if (curInclineError == 0) {
                InclineError.hasInError = false;
            } else {
                InclineError.hasInError = true;
            }
            //扬升校正错误,5是扬升校正错误码
            if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_NO_ERROR && curInclineError == 5) {
                ErrorManager.getInstance().errStatus = ErrorManager.ERR_INCLINE_CALIBRATE;
                curSysError = ErrorManager.ERR_INCLINE_CALIBRATE;
            }
        }
        int curKeyValue = ((RealChain) chain).resolveDate(data, NormalParam.KEY_VALUE_INX, NormalParam.KEY_VALUE_LEN);

        //非扬升错误
        if (ErrorManager.getInstance().isNoInclineError(curSysError)) {
            Message msg = new Message();
            msg.what = MsgWhat.MSG_ERROR;
            msg.arg1 = curSysError;
            if (((RealChain) chain).isInOnSleep()
                    && curKeyValue != 0) {
                msg.arg2 = curKeyValue;
            }
            return msg;
        }
        return chain.procced(data, ((RealChain) chain).isInOnSleep());
    }
}