package com.run.treadmill.manager.fitshow;

import android.os.Message;

import com.fitShow.treadmill.DataTypeConversion;
import com.fitShow.treadmill.FsTreadmillCommand;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowTreadmillManager;
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
        if (FitShowTreadmillManager.getInstance().isConnect && FitShowTreadmillManager.getInstance().isConnectTimer != null) {
            FitShowTreadmillManager.getInstance().isConnectTimer.setmAllTime(0L);
        }
        /*        if (isNOtConnect && runStart == FsTreadmillCommand.STATUS_NORMAL && rxData[1] != FsTreadmillCommand.CMD_SYS_INFO) {
         *//* if (rxData[2]==FsTreadmillCommand.CMD_SYS_STATUS){
                sendData(new byte[]{FsTreadmillCommand.CMD_SYS_STATUS, FsTreadmillCommand.STATUS_END}, 2);
            }*//*
            Logger.d("isNOtConnect=" + isNOtConnect + ",runStart=" + runStart + ",rxData[1]=" + rxData[1]);
            return;
        }*/
        switch (rxData[1]) {
            case FsTreadmillCommand.CMD_SYS_INFO_0x50:
                rxInfo(rxData);
                break;
            case FsTreadmillCommand.CMD_SYS_STATUS_0x51:
                rxStatus(rxData);
                break;
            case FsTreadmillCommand.CMD_SYS_DATA_0x52:
                rxData(rxData);
                break;
            case FsTreadmillCommand.CMD_SYS_CONTROL_0x53:
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
            bytes[0] = FsTreadmillCommand.PKG_HEAD;
            bytes[1] = rxData[1];
            bytes[2] = Utils.calc(new byte[]{rxData[1]}, 1);
            bytes[3] = FsTreadmillCommand.PKG_END;
            responseNothing(bytes, bytes.length);
        }
    }

    // rxData[1] == 0x50
    private static void rxInfo(byte[] rxData) throws Exception {
        if (rxData[1] == FsTreadmillCommand.CMD_SYS_INFO_0x50 && rxData[2] == FsTreadmillCommand.INFO_MODEL_0x00) {
            byte[] sendData = new byte[6];
            sendData[0] = FsTreadmillCommand.CMD_SYS_INFO_0x50;
            sendData[1] = FsTreadmillCommand.INFO_MODEL_0x00;
            sendData[2] = 0x13;
            sendData[3] = 0x00;
            sendData[4] = (byte) 0x9C;
            sendData[5] = (byte) 0x00;
            FsSend.sendData(sendData, sendData.length);
            return;
        }

        if (rxData[1] == FsTreadmillCommand.CMD_SYS_INFO_0x50 && rxData[2] == FsTreadmillCommand.INFO_SPEED_0x02) {
            float maxSpeed = SpManager.getMaxSpeed(SpManager.getIsMetric());
            byte[] spData = new byte[]{FsTreadmillCommand.CMD_SYS_INFO_0x50,
                    FsTreadmillCommand.INFO_SPEED_0x02,
                    DataTypeConversion.intLowToByte((int) ((maxSpeed > 25.5f ? 25.5f : maxSpeed) * 10))//运动秀协议缺陷，一个字节最大只能到255,速度不能大于25.5
                    , DataTypeConversion.intLowToByte((int) (SpManager.getMinSpeed(SpManager.getIsMetric()) * 10))};
            // 只构建data，不在run() 发
            FsSend.sendData2(spData, spData.length);

            // 直接发
            FitShowTreadmillManager.getInstance().fsTreadmillSerialUtils.sendData(FsSend.txData, FsSend.txSize);
            return;
        }

        if (rxData[1] == FsTreadmillCommand.CMD_SYS_INFO_0x50 && rxData[2] == FsTreadmillCommand.INFO_INCLINE_0x03) {
            byte[] inData = new byte[]{FsTreadmillCommand.CMD_SYS_INFO_0x50, FsTreadmillCommand.INFO_INCLINE_0x03, ErrorManager.getInstance().isHasInclineError()
                    ? 0 : DataTypeConversion.intLowToByte(SpManager.getMaxIncline() + InitParam.MIN_INCLINE), DataTypeConversion.intLowToByte(InitParam.MIN_INCLINE)
                    , (byte) ((SpManager.getIsMetric() ? FsTreadmillCommand.CONFIGURATION_KILOMETRE
                    : FsTreadmillCommand.CONFIGURATION_MILE) + FsTreadmillCommand.CONFIGURATION_PAUSE)};
            FsSend.sendData2(inData, inData.length);

            FitShowTreadmillManager.getInstance().fsTreadmillSerialUtils.sendData(FsSend.txData, FsSend.txSize);
            return;
        }

        //累计里程
        if (rxData[1] == FsTreadmillCommand.CMD_SYS_INFO_0x50 && rxData[2] == FsTreadmillCommand.INFO_TOTAL_0x04) {
            byte[] totleData = new byte[6];
            totleData[0] = FsTreadmillCommand.CMD_SYS_INFO_0x50;
            totleData[1] = FsTreadmillCommand.INFO_TOTAL_0x04;
            System.arraycopy(DataTypeConversion.intToBytesLitter((int) (SpManager.getRunTotalDis() * 1000)), 0, totleData, 2, 4);
            FsSend.sendData(totleData, totleData.length);
            return;
        }

        response = false;
    }

    // rxData[1] == 0x51
    private static void rxStatus(byte[] rxData) throws Exception {
        switch (rxData[2]) {
            case FsTreadmillCommand.STATUS_NORMAL:
                break;
            case FsTreadmillCommand.STATUS_END:
                break;
            case FsTreadmillCommand.STATUS_START:
                break;
            case FsTreadmillCommand.STATUS_RUNNING:
                break;
            case FsTreadmillCommand.STATUS_STOPPING:
                break;
            case FsTreadmillCommand.STATUS_ERROR:
                break;
            case FsTreadmillCommand.STATUS_DISABLE:
                break;
            case FsTreadmillCommand.STATUS_READY:
                break;
            case FsTreadmillCommand.STATUS_PAUSED:
                break;
            case FsTreadmillCommand.CMD_SYS_STATUS_0x51:
//                        Logger.d("isConnect=" + isConnect + ",isConnectTimer=" + isConnectTimer + "  fitShowTreadmillParamBuilder.build().getIncline() == " + fitShowTreadmillParamBuilder.build().getIncline());
                if (FitShowTreadmillManager.getInstance().isConnect && FitShowTreadmillManager.getInstance().isConnectTimer != null) {
                    //isConnectTimer.setmAllTime(0L);
                    if (FitShowTreadmillManager.getInstance().fitShowTreadmillParamBuilder.build().getIncline() != null) {
                        FsSend.sendRunParamToFitShow(FitShowTreadmillManager.getInstance().fitShowTreadmillParamBuilder.build());
                    } else {
                        FsSend.sendData(new byte[]{FsTreadmillCommand.CMD_SYS_STATUS_0x51, FitShowTreadmillManager.getInstance().runStart}, 2);
                    }
                } else {
                    FitShowTreadmillManager.getInstance().mHandler.sendEmptyMessage(FitShowTreadmillManager.getInstance().MSG_CONNECT);
                }
                break;
            default:
                response = false;
                break;
        }
    }

    // rxData[1] == 0x52
    private static void rxData(byte[] rxData) throws Exception {
        switch (rxData[2]) {
            case FsTreadmillCommand.DATA_SPORT:
                break;
            case FsTreadmillCommand.DATA_INFO:
                break;
            case FsTreadmillCommand.DATA_SPEED:
                break;
            case FsTreadmillCommand.DATA_INCLINE:
                break;
            default:
                response = false;
                break;
        }
    }

    // rxData[1] == 0x53
    private static void rxControl(byte[] rxData, int len) throws Exception {
        switch (rxData[2]) {
            case FsTreadmillCommand.CONTROL_READY:
                FitShowTreadmillManager.getInstance().clickStart = true;
                FitShowTreadmillManager.getInstance().runStart = FsTreadmillCommand.STATUS_START;
                FitShowTreadmillManager.getInstance().setCountDown(3);
                FitShowTreadmillManager.getInstance().mHandler.sendEmptyMessage(FsTreadmillCommand.CONTROL_READY);
                break;
            case FsTreadmillCommand.CONTROL_USER:
                byte[] totleData = new byte[]{FsTreadmillCommand.CMD_SYS_CONTROL_0x53, FsTreadmillCommand.CONTROL_USER, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00};
                FsSend.sendData(totleData, totleData.length);
                FitShowTreadmillManager.getInstance().fsTreadmillSerialUtils.sendData(
                        FsSend.txData, FsSend.txSize);
                FsThreadManager.isSendData = false;
                break;
            case FsTreadmillCommand.CONTROL_SPEED:
                break;
            case FsTreadmillCommand.CONTROL_HEIGHT:
                break;
            case FsTreadmillCommand.CONTROL_START:
                if (FitShowTreadmillManager.getInstance().runStart != FsTreadmillCommand.STATUS_START) {
                    FitShowTreadmillManager.getInstance().runStart = FsTreadmillCommand.STATUS_START;
                    FitShowTreadmillManager.getInstance().mHandler.sendEmptyMessage(FsTreadmillCommand.CONTROL_START);
                }
                break;
            case FsTreadmillCommand.CONTROL_PAUSE:
                FitShowTreadmillManager.getInstance().mHandler.sendEmptyMessage(FsTreadmillCommand.CONTROL_PAUSE);
                break;
            case FsTreadmillCommand.CONTROL_STOP:
                FitShowTreadmillManager.getInstance().runStart = FsTreadmillCommand.STATUS_PAUSED;
                // 正常发下0x0a 16长度
                        /*sendRunParamToFitShow(fitShowTreadmillParamBuilder.build());
                        Thread.sleep(80);*/

//                        this.runStart = FsTreadmillCommand.STATUS_NORMAL;
                // 可能导致APP没有退出，电子表退出了。

                //不在识别范围内的数据
                byte[] bytes = new byte[5];
                bytes[0] = FsTreadmillCommand.PKG_HEAD;
                bytes[1] = rxData[1];
                bytes[2] = FsTreadmillCommand.CONTROL_STOP;
                bytes[3] = Utils.calc(new byte[]{rxData[1]}, 2);
                bytes[4] = FsTreadmillCommand.PKG_END;
                responseNothing(bytes, bytes.length);

                FitShowTreadmillManager.getInstance().mHandler.sendEmptyMessage(FsTreadmillCommand.CONTROL_STOP);
                break;
            case FsTreadmillCommand.CONTROL_TARGET:
                // 改成倒计时时候也可以 APP设置电子表速度,用于程序模式
                if (FitShowTreadmillManager.getInstance().runStart != FsTreadmillCommand.STATUS_RUNNING && FitShowTreadmillManager.getInstance().runStart != FsTreadmillCommand.STATUS_START) {
                    break;
                }

                if (FitShowTreadmillManager.getInstance().runStart == FsTreadmillCommand.STATUS_START) {
                    // 倒计时状态
                    // 此时为程序模式
                    FitShowTreadmillManager.getInstance().isProgramMode = true;
                    FitShowTreadmillManager.getInstance().targetIncline = DataTypeConversion.byteToInt(rxData[4]);
                    FitShowTreadmillManager.getInstance().targetSpeed = DataTypeConversion.byteToInt(rxData[3]) / 10f;

                    //  0         0.5 0.8
                    if (FitShowTreadmillManager.getInstance().targetSpeed < SpManager.getMinSpeed(SpManager.getIsMetric())) {
                        FitShowTreadmillManager.getInstance().targetSpeed = SpManager.getMinSpeed(SpManager.getIsMetric());
                    }

                    Logger.e("程序模式 targetIncline == " + FitShowTreadmillManager.getInstance().targetIncline + "  targetSpeed == " + FitShowTreadmillManager.getInstance().targetSpeed);
                } else {
                    Message targetMessage = new Message();
                    targetMessage.what = FsTreadmillCommand.CONTROL_TARGET;

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

                    FitShowTreadmillManager.getInstance().mHandler.sendMessage(targetMessage);
                }
                break;
            default:
                response = false;
                break;
        }

    }

    private static void responseNothing(byte[] rxData, int len) {
        System.arraycopy(rxData, 0, FsSend.txData, 0, len);
        FsSend.txSize = len;
        FsThreadManager.isSendData = true;
    }

}
