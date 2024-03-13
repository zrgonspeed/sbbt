package com.run.treadmill.activity.runMode;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Message;

import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.treadmill.Custom;
import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.manager.control.NormalParam;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.util.FormulaUtil;
import com.run.treadmill.util.UnitUtil;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/04
 */
public abstract class BaseRunPresenter<V extends BaseRunView> extends BasePresenter<V> {
    public float maxIncline, minSpeed, maxSpeed;
    public RunningParam mRunningParam;

    public abstract void calcJump();

    public abstract void setInclineValue(int isUp, float incline, boolean onlyCurr);

    public abstract void setSpeedValue(int isUp, float speed, boolean onlyCurr);

    /**
     * 设置扬升速度的边界值
     *
     * @param maxIncline
     * @param minSpeed
     * @param maxSpeed
     */
    void setInclineAndSpeed(float maxIncline, float minSpeed, float maxSpeed) {
        this.maxIncline = maxIncline;
        this.maxSpeed = maxSpeed;
        this.minSpeed = minSpeed;
        this.mRunningParam = RunningParam.getInstance();
    }

    public synchronized void setSpeedValue(int isUp, float speed) {
        int mSpeedInx = FormulaUtil.getInxBySpeed(mRunningParam.mSpeedArray[mRunningParam.getLcCurStageNum()], minSpeed);
        if (isUp == 1) {
            if (maxSpeed <= FormulaUtil.getSpeedByInx(mSpeedInx, minSpeed)) {
                mSpeedInx = FormulaUtil.getInxBySpeed(maxSpeed, minSpeed);
            } else {
                mSpeedInx++;
            }
        } else if (isUp == -1) {
            if (minSpeed >= FormulaUtil.getSpeedByInx(mSpeedInx, minSpeed)) {
                mSpeedInx = 0;
            } else {
                mSpeedInx--;
            }
        } else if (isUp == 0) {
            if (speed > maxSpeed) {
                mSpeedInx = FormulaUtil.getInxBySpeed(maxSpeed, minSpeed);
            } else {
                mSpeedInx = FormulaUtil.getInxBySpeed(speed, minSpeed);
            }
        }
        mRunningParam.currSpeedInx = mSpeedInx;
        mRunningParam.setCurrSpeed(FormulaUtil.getSpeedByInx(mSpeedInx, minSpeed));
    }

    public synchronized void setInclineValue(int isUp, float incline) {
        if (ErrorManager.getInstance().isHasInclineError()) {
            return;
        }
        float mInclien = mRunningParam.mInclineArray[mRunningParam.getLcCurStageNum()];
        if (isUp == 1) {
            if (mInclien >= maxIncline) {
                mInclien = maxIncline;
            } else {
                mInclien += 1;
            }
        } else if (isUp == -1) {
            if (mInclien <= 0) {
                mInclien = 0;
            } else {
                mInclien -= 1;
            }
        } else if (isUp == 0) {
            if (incline > maxIncline) {
                mInclien = maxIncline;
            } else {
                mInclien = incline;
            }
        }
        mRunningParam.setCurrIncline(mInclien);
    }

    /**
     * 如果速度感应线从一开始就没插好，常态包速度一直返回0，并且处于非stop状态，则取最后下发的速度
     *
     * @param isMetric 是否为公制
     */
    protected synchronized void checkLastSpeedOnRunning(boolean isMetric) {
        if (mRunningParam == null) {
            return;
        }
        if (ErrorManager.getInstance().lastSpeed == 0
                && (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING
                || mRunningParam.runStatus == CTConstant.RUN_STATUS_WARM_UP
                || mRunningParam.runStatus == CTConstant.RUN_STATUS_COOL_DOWN)) {
            ErrorManager.getInstance().lastSpeed = (int) (isMetric ? (mRunningParam.getCurrSpeed() * 10) : (UnitUtil.getMileToKmByFloat1(mRunningParam.getCurrSpeed()) * 10));
            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
                ErrorManager.getInstance().lastSpeed = (int) (ErrorManager.getInstance().lastSpeed * SpManager.getSpeedRate());
            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                ErrorManager.getInstance().lastSpeed = (int) (ErrorManager.getInstance().lastSpeed * SpManager.getRpmRate());
            }
        }
    }

    @Override
    public void onSucceed(byte[] data, int len) {
        super.onSucceed(data, len);
        if (mRunningParam == null) {
            return;
        }
        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {
            if (resolveDate(data, NormalParam.HR_VALUE1_INX, NormalParam.HR_VALUE1_LEN) == 0) {
                mRunningParam.setCurrPulse(resolveDate(data, NormalParam.HR_VALUE2_INX, NormalParam.HR_VALUE2_LEN));
            } else {
                mRunningParam.setCurrPulse(resolveDate(data, NormalParam.HR_VALUE1_INX, NormalParam.HR_VALUE1_LEN));
            }

            mRunningParam.setCurrAD(resolveDate(data, NormalParam.CURR_AD_INX, NormalParam.CURR_AD_LEN));
            mRunningParam.setStepNumber(resolveDate(data, NormalParam.Step_Number_VALUE_INX, NormalParam.Step_Number_VALUE_LEN));
        }
    }

    @Override
    public void handleCmdMsg(Message msg) {
        super.handleCmdMsg(msg);
    }

    public void enterCoolDown() {
        mRunningParam.saveHasRunData();
        mRunningParam.runningToCoolDown();
        getView().enterCoolDown();
    }

    /**
     * 如果媒体有错误返回，这样做可以防止瞬间的错误停留在运动界面问题
     */
//    void sendMyError(){
//        if(mRunningParam.errorCode == ErrorManager.ERR_SAFE_ERROR || mRunningParam.errorCode == ErrorManager.ERR_SAFE_FC_ERROR){
//            sendMsg(ErrorManager.ERR_SAFE_ERROR);
//        }else if(mRunningParam.errorCode == ErrorManager.ERR_TIME_OUT){
//            sendMsg(MsgWhat.MSG_TIME_OUT);
//        }else{
//            sendMsg(MsgWhat.MSG_ERROR,mRunningParam.errorCode);
//        }
//    }
    public String[] getThirdApk(Context context, int arrayId) {
        return context.getResources().getStringArray(arrayId);
    }

    public int[] getThirdApkDrawable(Context context, int arrayId) {
        TypedArray ar = context.getResources().obtainTypedArray(arrayId);
        int len = ar.length();
        int[] resIds = new int[len];
        for (int i = 0; i < len; i++) {
            resIds[i] = ar.getResourceId(i, 0);
        }
        return resIds;
    }

    @Override
    public void onFail(byte[] data, int len, int count) {
        super.onFail(data, len, count);

        if (Custom.DEF_DEVICE_TYPE == CTConstant.DEVICE_TYPE_DC) {
            // DC光感下控，发最小速度失败时
            if ((mRunningParam.isPrepare()
                    || mRunningParam.runStatus == CTConstant.RUN_STATUS_CONTINUE)
                    && data[3] == ParamCons.CMD_SET_SPEED) {
                SerialUtils.getInstance().reMoveReSendPackage();
            }
        }
    }
}