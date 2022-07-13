package com.run.treadmill.activity.factory;

import static org.litepal.LitePalApplication.getContext;

import android.content.Intent;
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

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/24
 */
public class FactoryPresenter extends BasePresenter<FactoryView> {

    private final int msg_calibration_ad = 7000;
    private final int msg_calibration_success = 7001;
    private final int msg_calibration_success_back_home = 7002;

    protected boolean isCalibrating;
    private boolean hasCalibrating;
    private int inclienStatus;
    private int beltStatus;
    protected boolean isRpmStart = false;


    /**
     * 延迟判断是否校正成功
     */
    private int delayCount = 70;

    void calibrate(boolean isMetric, float maxSpeed, float minSpeed, float wheelSize, int maxIncline) {
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            ControlManager.getInstance().setCalcMetric(isMetric);
            ControlManager.getInstance().setMaxSpeed(DataTypeConversion.shortToBytes((short) (maxSpeed * 10)));//扩大10倍下发
            ControlManager.getInstance().setMinSpeed(DataTypeConversion.shortToBytes((short) (minSpeed * 10)));//扩大10倍下发
            ControlManager.getInstance().setWheelSize(DataTypeConversion.shortToBytes((short) (wheelSize * 100)));//扩大100倍下发
            ControlManager.getInstance().setMaxIncline(DataTypeConversion.shortToBytes((short) maxIncline));//扬升段数
        }
        ControlManager.getInstance().calibrate();
    }

    void setParam() {
        ControlManager.getInstance().write02Normal(buildDeviceInfoData());
    }

    void stopGetAd() {
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

    protected int changeRPM(int curRpm, int upRrDown) {
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
            int state = 0;
            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
                state = (data[NormalParam.INCLINE_STATE_INX] & 0x03);
            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                state = (data[NormalParam.INCLINE_STATE_INX] & 0x03);
            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                state = resolveDate(data, NormalParam.INCLINE_STATE_INX, NormalParam.INCLINE_STATE_LEN);
            }
            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
                if (state == ParamCons.CMD_INCLINE_UP) {
                    //主扬升状态(上升)
                    hasCalibrating = true;
                    inclienStatus = 1;
                } else if (state == ParamCons.CMD_INCLINE_DOWN) {
                    inclienStatus = 2;
                } else if (state == 0x00) {
                    inclienStatus = 0;
                }
            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                if (state == ParamCons.CMD_INCLINE_UP) {
                    //主扬升状态(上升)
                    inclienStatus = 1;
                } else if (state == ParamCons.CMD_INCLINE_DOWN) {
                    inclienStatus = 2;
                } else if (state == 0x00) {
                    inclienStatus = 0;
                }
            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                if (state == ParamCons.CMD_INCLINE_UP) {
                    //主扬升状态(上升)
                    hasCalibrating = true;
                    inclienStatus = 1;
                } else if (state == ParamCons.CMD_INCLINE_DOWN) {
                    inclienStatus = 2;
                } else if (state == 0x00) {
                    inclienStatus = 0;
                }
            }

            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                state = resolveDate(data, NormalParam.BELT_STATE_INX, NormalParam.BELT_STATE_LEN);
                if (state == 0x04) {//跑带矫正中
                    beltStatus = 1;
                } else if (state == 0x06) {
                    beltStatus = 2;
                } else if (state == 0x00) {
                    beltStatus = 0;
                }
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
            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
                sendMsg(msg_calibration_ad, DataTypeConversion.bytesToShortLiterEnd(data, 4));

            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                sendMsg(msg_calibration_ad, DataTypeConversion.byteToInt(data[4]));
                int state = resolveDate(data, 9, 1);
                if (state == 2) {
                    hasCalibrating = true;
                }
            } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                sendMsg(msg_calibration_ad, DataTypeConversion.byteToInt(data[4]));
                if (resolveDate(data, NormalParam.CURR_SPEED_INX, NormalParam.CURR_SPEED_LEN) != 0) {
                    ErrorManager.getInstance().lastSpeed = resolveDate(data, NormalParam.CURR_SPEED_INX, NormalParam.CURR_SPEED_LEN);
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

    public void doMasterClear() {
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
        intent.putExtra("android.intent.extra.WIPE_EXTERNAL_STORAGE", false);
        intent.addFlags((int) 0x01000000);
        getContext().sendBroadcast(intent);
    }
}