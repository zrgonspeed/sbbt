package com.run.treadmill.activity.factory;

import android.os.Message;

import com.run.serial.SerialCommand;
import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.db.UserCustomDataDB;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.UserInfoManager;
import com.run.treadmill.manager.control.NormalParam;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.util.DataTypeConversion;

import org.litepal.LitePal;

public class FactoryPresenter extends BasePresenter<FactoryView> {

    private final int msg_calibration_ad = 7000;
    private final int msg_calibration_success = 7001;
    private final int msg_calibration_success_back_home = 7002;

    protected boolean isCalibrating;
    private boolean hasCalibrating;
    private int inclienStatus;
    private int beltStatus;
    public boolean isRpmStart = false;


    /**
     * 延迟判断是否校正成功
     */
    private int delayCount = 70;

    public void setParam() {
        ControlManager.getInstance().write02Normal(buildDeviceInfoData());
    }

    public void stopGetAd() {
        if (isCalibrating) {
            isCalibrating = false;
        }
    }

    private synchronized byte[] buildDeviceInfoData() {
        byte[] data = new byte[5];
        byte[] s1 = DataTypeConversion.shortToBytes((short) SpManager.getMaxAd());
        byte[] s2 = DataTypeConversion.shortToBytes((short) SpManager.getMinAd());
        byte s3 = (byte) SpManager.getMaxIncline();
        data[0] = s1[0];
        data[1] = s1[1];

        data[2] = s2[0];
        data[3] = s2[1];

        data[4] = s3;

        return data;
    }

    public int changeRPM(int curRpm, int upRrDown) {
        if (upRrDown == 1) {
            curRpm = curRpm + 1;
        } else if (upRrDown == -1) {
            curRpm = curRpm - 1;
        }

        if (curRpm >= InitParam.MAX_RPM) {
            curRpm = InitParam.MAX_RPM;
            getView().setRpmEnable(1);
        } else if (curRpm <= InitParam.MIN_RPM) {
            curRpm = InitParam.MIN_RPM;
            getView().setRpmEnable(-1);
        } else {
            getView().setRpmEnable(0);
        }
        if (isRpmStart) {
            ControlManager.getInstance().calibrateSpeedByRpm(1.0f, curRpm);
        }
        return curRpm;
    }

    @Override
    public void onSucceed(byte[] data, int len) {
        super.onSucceed(data, len);
        if (ErrorManager.getInstance().isNoInclineError()) {
            delayCount = 70;
            isCalibrating = false;
            hasCalibrating = false;
            return;
        }
        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {
            int state = (data[NormalParam.INCLINE_STATE_INX] & 0x03);

            if (state == ParamCons.CMD_INCLINE_UP) {
                //主扬升状态(上升)
                inclienStatus = 1;
            } else if (state == ParamCons.CMD_INCLINE_DOWN) {
                inclienStatus = 2;
            } else if (state == 0x00) {
                inclienStatus = 0;
            }
        } else if (data[2] == SerialCommand.TX_WR_CTR_CMD && data[3] == ParamCons.CONTROL_CMD_CALIBRATE) {
            isCalibrating = true;
            new Thread(() -> {
                try {
                    while (isCalibrating) {
                        ControlManager.getInstance().send03Normal();
                        Thread.sleep(300);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM_03) {
            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                sendMsg(msg_calibration_ad, DataTypeConversion.byteToInt(data[4]));
                int state = resolveDate(data, 9, 1);
                if (state == 2) {
                    hasCalibrating = true;
                }
            }
        }

        if (isCalibrating && hasCalibrating && beltStatus == 0 && inclienStatus == 0) {
            delayCount--;
            if (delayCount <= 0) {
                isCalibrating = false;
                hasCalibrating = false;
                mCmdHandler.sendEmptyMessage(msg_calibration_success);
//                delayCount = 70;
            }
        }
        if (!isCalibrating && !hasCalibrating && delayCount <= 0) {
            delayCount--;
            if (delayCount == -15) {
                delayCount = 70;
                mCmdHandler.sendEmptyMessage(msg_calibration_success_back_home);
            }
        }
    }

    @Override
    public void handleCmdMsg(Message msg) {
        super.handleCmdMsg(msg);
        switch (msg.what) {
            default:
                break;
            case msg_calibration_ad:
                if (!isCalibrating) {
                    return;
                }
                if (inclienStatus == 1) {
                    getView().onCalibrationAd(msg.arg1, -1);
                } else if (inclienStatus == 2) {
                    getView().onCalibrationAd(-1, msg.arg1);
                }
                break;
            case msg_calibration_success:
                LitePal.deleteAll(UserCustomDataDB.class);
                LitePal.deleteAll(UserDB.class);
                UserInfoManager.getInstance().reset();
//                LitePal.deleteAllAsync(UserCustomDataDB.class, "where 1 = 1");
//                LitePal.deleteAllAsync(UserDB.class, "where 1 = 1");
                ErrorManager.getInstance().hasInclineError = false;
                getView().onCalibrationSuccess();
                break;
            case msg_calibration_success_back_home:
                getView().onCalibrationSuccessGoBackHome();
                break;
        }
    }
}