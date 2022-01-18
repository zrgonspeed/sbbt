package com.run.treadmill.interceptor;

import android.os.Message;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.MsgWhat;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.control.NormalParam;
import com.run.treadmill.util.Logger;

/**
 * @Description 错误拦截器
 * @Author GaleLiu
 * @Time 2019/09/12
 */
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

/*        if (ErrorManager.getInstance().isSafeError) {
            if (!SpManager.getGSMode() && ErrorManager.getInstance().errStatus != ErrorManager.ERR_INCLINE_CALIBRATE
                    && !ErrorManager.getInstance().hasInclineError) {
                ControlManager.getInstance().resetIncline();
            }
        }*/

        if (curSysError != ErrorManager.getInstance().errStatus) {
            //TODO:处理安全key清除错误问题
            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                if (curSysError == ErrorManager.ERR_NO_ERROR && ErrorManager.getInstance().lastError != ErrorManager.ERR_NO_ERROR) {
                    ErrorManager.getInstance().errStatus = ErrorManager.getInstance().lastError;
                } else {
                    ErrorManager.getInstance().errStatus = curSysError;
                    ErrorManager.getInstance().lastError = curSysError;
                }
            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                if (curSysError == ErrorManager.ERR_NO_ERROR && ErrorManager.getInstance().lastError != ErrorManager.ERR_NO_ERROR) {
                    ErrorManager.getInstance().errStatus = ErrorManager.getInstance().lastError;
                } else {
                    ErrorManager.getInstance().errStatus = curSysError;
                }
            } else {
                ErrorManager.getInstance().errStatus = curSysError;
            }
        }

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            int curInclineError = ((RealChain) chain).resolveDate(data, NormalParam.INCLINE_ERROR_INX, NormalParam.INCLINE_ERROR_LEN);
            //int curInclineError=ErrorManager.ERR_NO_ERROR;
            //扬升校正错误,5是扬升校正错误码
            if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_NO_ERROR && curInclineError == 5) {
                ErrorManager.getInstance().errStatus = ErrorManager.ERR_INCLINE_CALIBRATE;
                curSysError = ErrorManager.ERR_INCLINE_CALIBRATE;
            }
            //没有系统错误，并且扬升错误非0，统一认为是扬升错误(校正界面会把扬升错误改成扬升校正错误，运动界面才会改回扬升错误)
            if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_NO_ERROR && curInclineError != ErrorManager.ERR_NO_ERROR
                    && curInclineError != 5) {
                ErrorManager.getInstance().errStatus = ErrorManager.ERR_INCLINE_ADJUST;
            }

            if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_INCLINE_ADJUST && !ErrorManager.getInstance().hasInclineError) {
                ErrorManager.getInstance().hasInclineError = true;
            }
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {//暂时屏蔽不报扬升出错
            int curInclineError = ((RealChain) chain).resolveDate(data, NormalParam.INCLINE_ERROR_INX, NormalParam.INCLINE_ERROR_LEN);
            // int curInclineError=ErrorManager.ERR_NO_ERROR;
            //扬升校正错误,5是扬升校正错误码
            if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_NO_ERROR && curInclineError == 5) {
                ErrorManager.getInstance().errStatus = ErrorManager.ERR_INCLINE_CALIBRATE;
                curSysError = ErrorManager.ERR_INCLINE_CALIBRATE;
            }
            /*//没有系统错误，并且扬升错误非0，统一认为是扬升错误(校正界面会把扬升错误改成扬升校正错误，运动界面才会改回扬升错误)
            if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_NO_ERROR && curInclineError != ErrorManager.ERR_NO_ERROR
                    && curInclineError != 5) {
                ErrorManager.getInstance().errStatus = ErrorManager.ERR_INCLINE_ADJUST;
            }

            if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_INCLINE_ADJUST && !ErrorManager.getInstance().hasInclineError) {
                ErrorManager.getInstance().hasInclineError = true;
            }*/
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            if (curSysError == ErrorManager.ERR_INCLINE_ADJUST && !ErrorManager.getInstance().hasInclineError) {
                ErrorManager.getInstance().hasInclineError = true;
            }
        }
        int curKeyValue = ((RealChain) chain).resolveDate(data, NormalParam.KEY_VALUE_INX, NormalParam.KEY_VALUE_LEN);
//        if (curKeyValue != 0) {
//            Logger.d("========按键值==========" + curKeyValue);
//        }
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