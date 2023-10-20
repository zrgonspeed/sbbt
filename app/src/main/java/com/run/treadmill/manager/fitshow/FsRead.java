package com.run.treadmill.manager.fitshow;

import android.os.Message;

import com.fitShow.treadmill.DataTypeConversion;
import com.fitShow.treadmill.FitShowCommand;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.fitshow.other.FsThreadManager;
import com.run.treadmill.manager.fitshow.other.Utils;
import com.run.treadmill.util.Logger;

public class FsRead {
    private static boolean response = false;

    public static void parseData(byte[] rxData, int len) throws Exception {
        if (len < 4) {
            return;
        }
        response = true;
        if (FitShowManager.getInstance().isConnect && FitShowManager.getInstance().isConnectTimer != null) {
            FitShowManager.getInstance().isConnectTimer.setmAllTime(0L);
        }
        /*        if (isNOtConnect && runStart == FitShowCommand.STATUS_NORMAL && rxData[1] != FitShowCommand.CMD_SYS_INFO) {
         *//* if (rxData[2]==FitShowCommand.CMD_SYS_STATUS){
                sendData(new byte[]{FitShowCommand.CMD_SYS_STATUS, FitShowCommand.STATUS_END}, 2);
            }*//*
            Logger.d("isNOtConnect=" + isNOtConnect + ",runStart=" + runStart + ",rxData[1]=" + rxData[1]);
            return;
        }*/
        switch (rxData[1]) {
            case FitShowCommand.CMD_SYS_INFO_0x50:
                rxInfo(rxData);
                break;
            case FitShowCommand.CMD_SYS_STATUS_0x51:
                rxStatus(rxData);
                break;
            case FitShowCommand.CMD_SYS_DATA_0x52:
                rxData(rxData);
                break;
            case FitShowCommand.CMD_SYS_CONTROL_0x53:
                responseNothing(rxData, len);//收到后回复控制指令
                rxControl(rxData, len);
                break;
            default:
                response = false;
                break;
        }
        if (!response) {
            //不在识别范围内的数据
            byte[] bytes = new byte[4];
            bytes[0] = FitShowCommand.PKG_HEAD;
            bytes[1] = rxData[1];
            bytes[2] = Utils.calc(new byte[]{rxData[1]}, 1);
            bytes[3] = FitShowCommand.PKG_END;
            responseNothing(bytes, bytes.length);
        }
    }

    // rxData[1] == 0x50
    private static void rxInfo(byte[] rxData) throws Exception {
        if (rxData[1] == FitShowCommand.CMD_SYS_INFO_0x50 && rxData[2] == FitShowCommand.INFO_MODEL_0x00) {
            byte[] sendData = new byte[6];
            sendData[0] = FitShowCommand.CMD_SYS_INFO_0x50;
            sendData[1] = FitShowCommand.INFO_MODEL_0x00;
            sendData[2] = 0x00;
            sendData[3] = 0x00;
            sendData[4] = (byte) 0x00;
            sendData[5] = (byte) 0x00;
            FsSend.sendData(sendData, sendData.length);
            return;
        }

        if (rxData[1] == FitShowCommand.CMD_SYS_INFO_0x50 && rxData[2] == FitShowCommand.INFO_SPEED_0x02) {
            float maxSpeed = SpManager.getMaxSpeed(SpManager.getIsMetric());
            byte[] spData = new byte[]{FitShowCommand.CMD_SYS_INFO_0x50,
                    FitShowCommand.INFO_SPEED_0x02,
                    DataTypeConversion.intLowToByte((int) ((maxSpeed > 25.5f ? 25.5f : maxSpeed) * 10))//运动秀协议缺陷，一个字节最大只能到255,速度不能大于25.5
                    , DataTypeConversion.intLowToByte((int) (SpManager.getMinSpeed(SpManager.getIsMetric()) * 10))};
            // 只构建data，不在run() 发
            FsSend.sendData2(spData, spData.length);

            // 直接发
            FitShowManager.getInstance().fsSerialUtils.sendData(FsSend.txData, FsSend.txSize);
            return;
        }

        if (rxData[1] == FitShowCommand.CMD_SYS_INFO_0x50 && rxData[2] == FitShowCommand.INFO_INCLINE_0x03) {
            byte[] inData = new byte[]{FitShowCommand.CMD_SYS_INFO_0x50, FitShowCommand.INFO_INCLINE_0x03, ErrorManager.getInstance().isHasInclineError()
                    ? 0 : DataTypeConversion.intLowToByte(SpManager.getMaxIncline() + InitParam.MIN_INCLINE), DataTypeConversion.intLowToByte(InitParam.MIN_INCLINE)
                    , (byte) ((SpManager.getIsMetric() ? FitShowCommand.CONFIGURATION_KILOMETRE
                    : FitShowCommand.CONFIGURATION_MILE) + FitShowCommand.CONFIGURATION_PAUSE)};
            FsSend.sendData2(inData, inData.length);

            FitShowManager.getInstance().fsSerialUtils.sendData(FsSend.txData, FsSend.txSize);
            return;
        }

        //累计里程
        if (rxData[1] == FitShowCommand.CMD_SYS_INFO_0x50 && rxData[2] == FitShowCommand.INFO_TOTAL_0x04) {
            byte[] totleData = new byte[6];
            totleData[0] = FitShowCommand.CMD_SYS_INFO_0x50;
            totleData[1] = FitShowCommand.INFO_TOTAL_0x04;
            System.arraycopy(DataTypeConversion.intToBytesLitter((int) (SpManager.getRunTotalDis() * 1000)), 0, totleData, 2, 4);
            FsSend.sendData(totleData, totleData.length);
            return;
        }

        response = false;
    }

    // rxData[1] == 0x51
    private static void rxStatus(byte[] rxData) throws Exception {
        switch (rxData[2]) {
            case FitShowCommand.CMD_SYS_STATUS_0x51:
//                        Logger.d("isConnect=" + isConnect + ",isConnectTimer=" + isConnectTimer + "  fitShowTreadmillParamBuilder.build().getIncline() == " + fitShowTreadmillParamBuilder.build().getIncline());
                if (FitShowManager.getInstance().isConnect && FitShowManager.getInstance().isConnectTimer != null) {
                    //isConnectTimer.setmAllTime(0L);
                    if (FitShowManager.getInstance().paramBuilder.build().getIncline() != null) {
                        FsSend.sendRunParamToFitShow(FitShowManager.getInstance().paramBuilder.build());
                    } else {
                        FsSend.sendData(new byte[]{FitShowCommand.CMD_SYS_STATUS_0x51, FitShowManager.getInstance().runStart}, 2);
                    }
                } else {
                    FitShowManager.getInstance().mHandler.sendEmptyMessage(FitShowManager.getInstance().MSG_CONNECT);
                }
                break;
            case FitShowCommand.STATUS_NORMAL_0x00:
                break;
            case FitShowCommand.STATUS_END_0x01:
                break;
            case FitShowCommand.STATUS_START_0x02:
                break;
            case FitShowCommand.STATUS_RUNNING_0x03:
                break;
            case FitShowCommand.STATUS_STOPPING_0x04:
                break;
            case FitShowCommand.STATUS_ERROR_0x05:
                break;
            case FitShowCommand.STATUS_DISABLE_0x06:
                break;
            case FitShowCommand.STATUS_READY_0x09:
                break;
            case FitShowCommand.STATUS_PAUSED_0x0A:
                break;
            default:
                response = false;
                break;
        }
    }

    // rxData[1] == 0x52
    private static void rxData(byte[] rxData) throws Exception {
        switch (rxData[2]) {
            case FitShowCommand.DATA_SPORT_0x00:
                break;
            case FitShowCommand.DATA_INFO_0x01:
                break;
            case FitShowCommand.DATA_SPEED_0x02:
                break;
            case FitShowCommand.DATA_INCLINE_0x03:
                break;
            default:
                response = false;
                break;
        }
    }

    // rxData[1] == 0x53
    private static void rxControl(byte[] rxData, int len) throws Exception {
        if (rxData[1] == FitShowCommand.CMD_SYS_CONTROL_0x53 && rxData[2] == FitShowCommand.CONTROL_READY_0x01) {
            FitShowManager.getInstance().clickStart = true;
            FitShowManager.getInstance().runStart = FitShowCommand.STATUS_START_0x02;
            FitShowManager.getInstance().setCountDown(3);
            FitShowManager.getInstance().mHandler.sendEmptyMessage(FitShowCommand.CONTROL_READY_0x01);
            return;
        }

        if (rxData[1] == FitShowCommand.CMD_SYS_CONTROL_0x53 && rxData[2] == FitShowCommand.CONTROL_USER_0x00) {
            byte[] totleData = new byte[]{FitShowCommand.CMD_SYS_CONTROL_0x53, FitShowCommand.CONTROL_USER_0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00};
            FsSend.sendData(totleData, totleData.length);
            FitShowManager.getInstance().fsSerialUtils.sendData(FsSend.txData, FsSend.txSize);
            FsThreadManager.isSendData = false;
            return;
        }

        if (rxData[1] == FitShowCommand.CMD_SYS_CONTROL_0x53 && rxData[2] == FitShowCommand.CONTROL_START_0x09) {
            if (FitShowManager.getInstance().runStart != FitShowCommand.STATUS_START_0x02) {
                FitShowManager.getInstance().runStart = FitShowCommand.STATUS_START_0x02;
                FitShowManager.getInstance().mHandler.sendEmptyMessage(FitShowCommand.CONTROL_START_0x09);
            }
            return;
        }

        if (rxData[1] == FitShowCommand.CMD_SYS_CONTROL_0x53 && rxData[2] == FitShowCommand.CONTROL_PAUSE_0x0A) {
            FitShowManager.getInstance().mHandler.sendEmptyMessage(FitShowCommand.CONTROL_PAUSE_0x0A);
            return;
        }

        if (rxData[1] == FitShowCommand.CMD_SYS_CONTROL_0x53 && rxData[2] == FitShowCommand.CONTROL_STOP_0x03) {
            FitShowManager.getInstance().runStart = FitShowCommand.STATUS_PAUSED_0x0A;
            // 正常发下0x0a 16长度
                        /*sendRunParamToFitShow(fitShowTreadmillParamBuilder.build());
                        Thread.sleep(80);*/

//                        this.runStart = FitShowCommand.STATUS_NORMAL;
            // 可能导致APP没有退出，电子表退出了。

            //不在识别范围内的数据
            byte[] bytes = new byte[5];
            bytes[0] = FitShowCommand.PKG_HEAD;
            bytes[1] = rxData[1];
            bytes[2] = FitShowCommand.CONTROL_STOP_0x03;
            bytes[3] = Utils.calc(new byte[]{rxData[1]}, 2);
            bytes[4] = FitShowCommand.PKG_END;
            responseNothing(bytes, bytes.length);

            FitShowManager.getInstance().mHandler.sendEmptyMessage(FitShowCommand.CONTROL_STOP_0x03);
            return;
        }

        if (rxData[1] == FitShowCommand.CMD_SYS_CONTROL_0x53 && rxData[2] == FitShowCommand.CONTROL_TARGET_0x02) {
            // 改成倒计时时候也可以 APP设置电子表速度,用于程序模式
            if (FitShowManager.getInstance().runStart != FitShowCommand.STATUS_RUNNING_0x03 && FitShowManager.getInstance().runStart != FitShowCommand.STATUS_START_0x02) {
                return;
            }

            if (FitShowManager.getInstance().runStart == FitShowCommand.STATUS_START_0x02) {
                // 倒计时状态
                // 此时为程序模式
                FitShowManager.getInstance().isProgramMode = true;
                FitShowManager.getInstance().targetIncline = DataTypeConversion.byteToInt(rxData[4]);
                FitShowManager.getInstance().targetSpeed = DataTypeConversion.byteToInt(rxData[3]) / 10f;

                //  0         0.5 0.8
                if (FitShowManager.getInstance().targetSpeed < SpManager.getMinSpeed(SpManager.getIsMetric())) {
                    FitShowManager.getInstance().targetSpeed = SpManager.getMinSpeed(SpManager.getIsMetric());
                }

                Logger.e("程序模式 targetIncline == " + FitShowManager.getInstance().targetIncline + "  targetSpeed == " + FitShowManager.getInstance().targetSpeed);
            } else {
                Message targetMessage = new Message();
                targetMessage.what = FitShowCommand.CONTROL_TARGET_0x02;

                if (len == 6) {
                    targetMessage.arg1 = 0;
                    targetMessage.arg2 = DataTypeConversion.byteToInt(rxData[3]);
                } else if (len == 7) {
                    // 扬升为负数时也要处理
                    targetMessage.arg1 = DataTypeConversion.byteToInt(rxData[4]);
                    if (targetMessage.arg1 > 127) {
                        targetMessage.arg1 = targetMessage.arg1 - 256;
                        Logger.e("targetMessage.arg1 == " + targetMessage.arg1);
                    }
                    targetMessage.arg2 = DataTypeConversion.byteToInt(rxData[3]);
                }
                Logger.i("len == " + len + "  targetMessage.arg1 == " + targetMessage.arg1 + "  targetMessage.arg2 == " + targetMessage.arg2);

                FitShowManager.getInstance().mHandler.sendMessage(targetMessage);
            }
            return;
        }

        response = false;
    }

    private static void responseNothing(byte[] rxData, int len) {
        System.arraycopy(rxData, 0, FsSend.txData, 0, len);
        FsSend.txSize = len;
        FsThreadManager.isSendData = true;
    }

}
