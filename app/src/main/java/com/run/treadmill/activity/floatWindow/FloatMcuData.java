package com.run.treadmill.activity.floatWindow;

import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.treadmill.activity.floatWindow.FloatWindowManager;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.control.NormalParam;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.MsgWhat;

public class FloatMcuData {
    public static void onSucceed(byte[] data, int len, FloatWindowManager fwm) {
        if (ErrorManager.getInstance().errorDelayTime > 0) {
            ErrorManager.getInstance().errorDelayTime = ErrorManager.SAFE_DELAY_TIME;
            ErrorManager.getInstance().exitError = true;
            fwm.sendNormalMsg(MsgWhat.MSG_ERROR);
            return;
        }
        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {
            int curSafeError = fwm.resolveDate(data, NormalParam.SAFE_ERROR_INX, NormalParam.SAFE_ERROR_LEN);
            int curSysError = fwm.resolveDate(data, NormalParam.SYS_ERROR_INX, NormalParam.SYS_ERROR_LEN);


            if (curSafeError != ErrorManager.ERR_NO_ERROR) {
                if (!ErrorManager.getInstance().isSafeError) {
                    ErrorManager.getInstance().lastError = ErrorManager.getInstance().errStatus;
                    ErrorManager.getInstance().errStatus = ErrorManager.ERR_SAFE_ERROR;
                    ErrorManager.getInstance().isSafeError = true;
                    ControlManager.getInstance().emergencyStop();
                    SerialUtils.getInstance().stopResend();
                }
                if (ErrorManager.getInstance().errorDelayTime != ErrorManager.SAFE_DELAY_TIME) {
                    ErrorManager.getInstance().errorDelayTime = ErrorManager.SAFE_DELAY_TIME;
                }
                ErrorManager.getInstance().exitError = true;
                fwm.myFloatHandler.sendEmptyMessage(MsgWhat.MSG_ERROR);
                return;
            }

            //非扬升错误
            if (ErrorManager.getInstance().isNoInclineError(curSysError)) {
                fwm.sendNormalMsg(MsgWhat.MSG_ERROR);
                return;
            }
            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                if (curSysError == ErrorManager.ERR_INCLINE_ADJUST) {
                    ErrorManager.getInstance().errStatus = curSysError;
                    ErrorManager.getInstance().hasInclineError = true;
                }
                if (ErrorManager.getInstance().isHasInclineError()) {
                    fwm.sendNormalMsg(MsgWhat.MSG_ERROR_INCLINE);
                }
            }

            int keyResult = SerialKeyValue.isNeedSendMsg(fwm.resolveDate(data, NormalParam.KEY_VALUE_INX, NormalParam.KEY_VALUE_LEN));
            if (keyResult != -1) {
                fwm.sendNormalMsg(MsgWhat.MSG_DATA_KEY_EVENT, keyResult);
            }
            if (fwm.mRunningParam == null) {
                return;
            }

            if (fwm.mRunningParam.isStopStatus()) {
                int[] reDate = new int[3];
                reDate[0] = fwm.resolveDate(data, NormalParam.BELT_STATE_INX, NormalParam.BELT_STATE_LEN);
                if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                    reDate[1] = fwm.resolveDate(data, NormalParam.INCLINE_STATE_INX, NormalParam.INCLINE_STATE_LEN);
                }
                // Logger.d("****float 发送****");
                fwm.sendNormalMsg(MsgWhat.MSG_DATA_BELT_AND_INCLINE, reDate);
            }

            ErrorManager.getInstance().lastSpeed = fwm.resolveDate(data, NormalParam.CURR_SPEED_INX, NormalParam.CURR_SPEED_LEN);

/*            int belt = resolveDate(data, NormalParam.BELT_STATE_INX, NormalParam.BELT_STATE_LEN);
            sendNormalMsg(MsgWhat.MSG_DATA_BELT_STATUS, belt);*/

            if (fwm.resolveDate(data, NormalParam.HR_VALUE1_INX, NormalParam.HR_VALUE1_LEN) == 0) {
                fwm.mRunningParam.setCurrPulse(fwm.resolveDate(data, NormalParam.HR_VALUE2_INX, NormalParam.HR_VALUE2_LEN));
            } else {
                fwm.mRunningParam.setCurrPulse(fwm.resolveDate(data, NormalParam.HR_VALUE1_INX, NormalParam.HR_VALUE1_LEN));
            }

            fwm.mRunningParam.setCurrAD(fwm.resolveDate(data, NormalParam.CURR_AD_INX, NormalParam.CURR_AD_LEN));
            fwm.mRunningParam.setStepNumber(fwm.resolveDate(data, NormalParam.Step_Number_VALUE_INX, NormalParam.Step_Number_VALUE_LEN));
        }
    }


    /**
     * Float回调的跑带和扬升
     */
    public static void beltAndIncline(FloatWindowManager mFwm, int[] data) {
        if (mFwm.baseRunBottomFloat != null) {
            // 每次进入暂停页面, 保持1秒禁用continue
            if (mFwm.disFlag) {
                mFwm.pauseFw.disContinue();
                return;
            }
            if (mFwm.disPauseBtn) {
                mFwm.baseRunBottomFloat.disIvPause();
                return;
            }

            int beltStatus = data[0];
            int inclineStatus = data[1];
            if (beltStatus != 0) {
                return;
            }
            if (ErrorManager.getInstance().isHasInclineError()) {
                if (mFwm.mRunningParam.isStopStatus()) {
                    mFwm.pauseFw.enContinue();
                }
                return;
            }
            if (inclineStatus == 0) {
                if (mFwm.mRunningParam.isStopStatus()) {
                    mFwm.pauseFw.enContinue();
                }
            }
        }

    }

}
