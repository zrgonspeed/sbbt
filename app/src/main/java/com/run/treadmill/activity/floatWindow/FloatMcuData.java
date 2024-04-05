package com.run.treadmill.activity.floatWindow;

import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.control.NormalParam;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.MsgWhat;

public class FloatMcuData {
    public static void onSucceed(byte[] data, int len, FloatWindowManager fwm) {
        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {
            NormalParam.print(data);
        }

        if (ErrorManager.getInstance().errorDelayTime > 0) {
            ErrorManager.getInstance().errorDelayTime = ErrorManager.SAFE_DELAY_TIME;
            ErrorManager.getInstance().exitError = true;
            fwm.sendNormalMsg(MsgWhat.MSG_ERROR);
            return;
        }

        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {
            int curSafeError = NormalParam.getSafeError(data);
            int curSysError = NormalParam.getSysError(data);

            // 安全Key错误
            if (curSafeError != ErrorManager.ERR_NO_ERROR) {
                safeErrorDeal(fwm);
                return;
            }

            // 非扬升错误,其他错误
            if (ErrorManager.getInstance().isNoInclineError(curSysError)) {
                fwm.sendNormalMsg(MsgWhat.MSG_ERROR);
                return;
            }

            // 扬升错误
            inclineErrorDeal(fwm, data);

            // 按键
            keyDeal(fwm, data);

            if (fwm.mRunningParam == null) {
                return;
            }

            // 回调float跑带和扬升
            if (fwm.mRunningParam.isStopStatus()) {
                beltAndInclineDeal(fwm, data);
            }

            ErrorManager.getInstance().lastSpeed = NormalParam.getSpeed(data);

            fwm.mRunningParam.setCurrAD(NormalParam.getIncline(data));
            fwm.mRunningParam.setStepNumber(NormalParam.getStep(data));

            hrDeal(fwm, data);
        }
    }

    private static void keyDeal(FloatWindowManager fwm, byte[] data) {
        int keyResult = SerialKeyValue.isNeedSendMsg(NormalParam.getKey(data));
        if (keyResult != -1) {
            fwm.sendNormalMsg(MsgWhat.MSG_DATA_KEY_EVENT, keyResult);
        }
    }

    private static void beltAndInclineDeal(FloatWindowManager fwm, byte[] data) {
        int[] reDate = new int[3];
        reDate[0] = NormalParam.getBeltState(data);
        reDate[1] = NormalParam.getInclineState(data);
        fwm.sendNormalMsg(MsgWhat.MSG_DATA_BELT_AND_INCLINE, reDate);
    }

    private static void inclineErrorDeal(FloatWindowManager fwm, byte[] data) {
        int curSysError = NormalParam.getSysError(data);
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            if (curSysError == ErrorManager.ERR_INCLINE_ADJUST) {
                ErrorManager.getInstance().errStatus = curSysError;
                ErrorManager.getInstance().hasInclineError = true;
            }
            if (ErrorManager.getInstance().isHasInclineError()) {
                fwm.sendNormalMsg(MsgWhat.MSG_ERROR_INCLINE);
            }
        }
    }

    private static void hrDeal(FloatWindowManager fwm, byte[] data) {
        if (NormalParam.getHr1(data) == 0) {
            fwm.mRunningParam.setCurrPulse(NormalParam.getHr2(data));
        } else {
            fwm.mRunningParam.setCurrPulse(NormalParam.getHr1(data));
        }
    }

    private static void safeErrorDeal(FloatWindowManager fwm) {
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
