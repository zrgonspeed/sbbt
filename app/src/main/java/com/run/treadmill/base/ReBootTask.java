package com.run.treadmill.base;

import com.run.serial.RxDataCallBack;
import com.run.serial.SerialCommand;
import com.run.serial.SerialUtils;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.MsgWhat;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.control.NormalParam;
import com.run.treadmill.manager.control.ParamCons;
import com.run.treadmill.util.DataTypeConversion;
import com.run.treadmill.util.Logger;

public class ReBootTask implements Runnable, RxDataCallBack {

    private BasePresenter presenter;
    public static boolean isReBootFinish = false;
    private boolean isReBootNormalFinish = false;
    private boolean isReBootSafeError = false;

    private boolean isGetDeviceType = false;
    /**
     * 没有接收到常态包
     */
    private boolean notNormal = true;

    private int getInfoCount = 1;
    private boolean isTimeOut = false;

    private Thread thread;

    private static ReBootTask task;
    private boolean isReadNcuVer = false;
    private boolean isReadNcuYear = false;
    private boolean isReadNcuMonthDay = false;

    private ReBootTask() {

    }

    public static ReBootTask getInstance() {
        if (task == null) {
            synchronized (ReBootTask.class) {
                task = new ReBootTask();
            }
        }
        return task;
    }

    public void startReBootThread() {
        if (thread != null) {
            return;
        }
        ControlManager.getInstance().regRxDataCallBack(this);
        thread = new Thread(task);
        thread.start();
    }

    public void setPresenter(BasePresenter p) {
        Logger.d("reboot setPresenter");
        presenter = p;
    }

    @Override
    public void run() {
        try {
            //这里根据机台类型可能需要在开机时重新获取或者下发一定量的信息
            //这里以DC机台为例,因为AC00400-01是DC机台,当机台类型转换时 下面的需要更改
            while (!isTimeOut) {
                Logger.d("reboot task start");
                ControlManager.getInstance().writeDeviceType();
                ControlManager.getInstance().readDeviceType();

//                ControlManager.getInstance().writeNormalExpand();
                while (!isTimeOut) {
                    Thread.sleep(80);
                    if (isGetDeviceType) {
                        break;
                    }
                }
                if (isTimeOut) {
                    break;
                }
                Logger.d("reboot safeCheck begin time  = " + System.currentTimeMillis());
                int openSafeError = 0;
                isReBootNormalFinish = false;
                while (!isTimeOut) {
                    if (!notNormal) {
                        //获取到常态包
                        notNormal = true;
                        openSafeError++;
                        if (ErrorManager.getInstance().isSafeError) {
                            isReBootSafeError = true;
                            openSafeError = 0;
                            continue;
                        }
                        if (openSafeError > 25) {
                            break;
                        }
                    }
                }
                if (isTimeOut) {
                    break;
                }
                Logger.d("reboot safeCheck finish time = " + System.currentTimeMillis());

                ErrorManager.getInstance().exitError = false;
                isReBootSafeError = false;
                isReBootNormalFinish = true;

                ControlManager.getInstance().readNcuYear();
                ControlManager.getInstance().readNcuMonthDay();
                ControlManager.getInstance().readNcuVersion();

                getInfoCount = 3;

                if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
                    //AC
                    ControlManager.getInstance().write02Normal(buildDeviceInfoData());
                    getInfoCount += 1;

                } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                    //AA
                    ControlManager.getInstance().write02Normal(buildDeviceInfoData());
                    getInfoCount += 1;

                } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                    //DC
                    ControlManager.getInstance().read02Normal();
                    ControlManager.getInstance().readMaxAd();
                    ControlManager.getInstance().readMinAd();
                    getInfoCount = getInfoCount + 3;
                }

                if (!SpManager.getGSMode() && ErrorManager.getInstance().errStatus != ErrorManager.ERR_INCLINE_CALIBRATE
                        && !ErrorManager.getInstance().hasInclineError) {
                    if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                        ControlManager.getInstance().setIncline(0.0f);
                    } else {
                        ControlManager.getInstance().resetIncline();
                    }
                    getInfoCount += 1;
                }

                while (getInfoCount > 0) {
                    Logger.d("reboot task getInfoCount = " + getInfoCount);
                    Thread.sleep(100);
                }
                isReBootFinish = true;
                Logger.d("reboot task finish");
                if (presenter != null) {
                    Logger.d("reboot presenter regRxDataCallBack");
                    ControlManager.getInstance().regRxDataCallBack(presenter);
                }
                break;
            }
            if (isTimeOut) {
                Logger.d("reboot task finish onTimeOut");
                isReBootFinish = true;
                if (presenter != null) {
                    Logger.d("reboot presenter regRxDataCallBack");
                    ControlManager.getInstance().regRxDataCallBack(presenter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSucceed(byte[] data, int len) {
        if (data[2] == SerialCommand.TX_RD_ONE
                && data[3] == ParamCons.PARAM_DEVICE) {
            parseDeviceType(data);
            return;
        }
        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {
            notNormal = false;
            int curSafeError = resolveDate(data, NormalParam.SAFE_ERROR_INX, NormalParam.SAFE_ERROR_LEN);
            if (curSafeError != ErrorManager.ERR_NO_ERROR) {
                isReBootSafeError = true;
                ErrorManager.getInstance().isSafeError = true;
                ErrorManager.getInstance().errStatus = ErrorManager.ERR_SAFE_ERROR;
                ErrorManager.getInstance().exitError = true;
                if (presenter != null) {
                    presenter.sendNormalMsg(ErrorManager.ERR_SAFE_ERROR);
                }
            } else {
                if (!isReBootNormalFinish && isReBootSafeError) {
                    ErrorManager.getInstance().isSafeError = false;
                    ErrorManager.getInstance().errStatus = ErrorManager.ERR_SAFE_ERROR;
                    ErrorManager.getInstance().exitError = true;
                    if (presenter != null) {
                        presenter.sendNormalMsg(ErrorManager.ERR_SAFE_ERROR);
                    }
                    return;
                }
                int curSysError = resolveDate(data, NormalParam.SYS_ERROR_INX, NormalParam.SYS_ERROR_LEN);
                // int curSysError =ErrorManager.ERR_NO_ERROR;
                if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
                    //AC将扬升出粗额外处理
                    int cutInclineError = resolveDate(data, NormalParam.INCLINE_ERROR_INX, NormalParam.INCLINE_ERROR_LEN);
                    if (curSysError != ErrorManager.ERR_NO_ERROR) {
                        ErrorManager.getInstance().errStatus = curSysError;
                        if (presenter != null) {
                            presenter.sendNormalMsg(MsgWhat.MSG_ERROR, curSysError);
                        }
                        return;
                    }
                } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                    //AA将扬升出错,已经在下控处理了
                    //TODO: 注意--> CTConstant.DEVICE_TYPE_AA 强制屏蔽部分错误,后面是否删除,待议
//                    if (curSysError == 0x0C) {
//                        curSysError = 0;
//                    }
                    if (curSysError != ErrorManager.ERR_NO_ERROR) {
                        ErrorManager.getInstance().errStatus = curSysError;
                        if (presenter != null) {
                            presenter.sendNormalMsg(MsgWhat.MSG_ERROR, curSysError);
                        }
                        return;
                    }
                } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                    if (ErrorManager.getInstance().isNoInclineError(curSysError)) {
                        ErrorManager.getInstance().errStatus = curSysError;
                        if (presenter != null) {
                            presenter.sendNormalMsg(MsgWhat.MSG_ERROR, curSysError);
                        }
                        return;
                    }
                }
                ErrorManager.getInstance().errStatus = ErrorManager.ERR_NO_ERROR;
                int curBelt = resolveDate(data, NormalParam.BELT_STATE_INX, NormalParam.BELT_STATE_LEN);
                int curIncline = resolveDate(data, NormalParam.INCLINE_STATE_INX, NormalParam.INCLINE_STATE_LEN);
                int[] normalArray = new int[5];
                normalArray[0] = -1;
                normalArray[1] = curBelt;
                normalArray[2] = curIncline;
                normalArray[3] = resolveDate(data, NormalParam.CURR_SPEED_INX, NormalParam.CURR_SPEED_LEN);
                normalArray[4] = resolveDate(data, NormalParam.CURR_AD_INX, NormalParam.CURR_AD_LEN);
                if (presenter != null) {
                    presenter.sendNormalMsg(MsgWhat.MSG_DATA_BELT_AND_INCLINE, normalArray);
                }
            }
            return;
        }

        //转接板的版本和日期
        if (data[3] == ParamCons.READ_NCU_VER
                || data[3] == ParamCons.READ_NCU_YEAR
                || data[3] == ParamCons.READ_NCU_MONTH_DAY) {
            parseNcuVersion(data);
            getInfoCount--;
            return;
        }
        //GS扬升命令
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            if (data[2] == SerialCommand.TX_WR_ONE
                    && data[3] == ParamCons.CMD_SET_INCLINE) {
                getInfoCount--;
                return;
            }
        } else {
            if (data[2] == SerialCommand.TX_WR_CTR_CMD
                    && data[3] == ParamCons.CONTROL_CMD_INCLINE_RESET) {
                getInfoCount--;
                return;
            }
        }

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            if (data[3] == ParamCons.NORMAL_PACKAGE_PARAM_02) {
                getInfoCount--;
            }
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            if (data[3] == ParamCons.NORMAL_PACKAGE_PARAM_02) {
                getInfoCount--;
            }
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            if (data[3] == ParamCons.NORMAL_PACKAGE_PARAM_02) {
                parseDeviceInfo(data);
                getInfoCount--;
            } else if (data[3] == ParamCons.CMD_MIN_AD) {
                parseDeviceMinADC(data);
                getInfoCount--;
            } else if (data[3] == ParamCons.CMD_MAX_AD) {
                parseDeviceMaxADC(data);
                getInfoCount--;
            }
        }

    }

    @Override
    public void onFail(byte[] data, int len, int count) {
        //需要根据自己需求,可以在第40次重发出现前调用下面的方法以保证串口可以正常运作
        //而成功会默认调用
        if (data[2] == SerialCommand.TX_RD_ONE && data[3] == ParamCons.PARAM_DEVICE && count > 4) {
            Logger.d("reboot get deviceType fail, set default deviceType = MyApplication.DEFAULT_DEVICE_TYPE");
            isGetDeviceType = true;
            ControlManager.getInstance().init(MyApplication.DEFAULT_DEVICE_TYPE);
            ErrorManager.init(MyApplication.DEFAULT_DEVICE_TYPE);
            SerialUtils.getInstance().reMoveReSendPackage();
        }
    }

    @Override
    public void onTimeOut() {
        getInfoCount = 0;
        isTimeOut = true;
    }

    private int resolveDate(byte[] date, int offSet, int len) {
        int result;
        if (len == 3) {
            //3个字节 ,暂时不知道如何处理
            result = 0;
        } else if (len == 2) {
            result = DataTypeConversion.bytesToShortLiterEnd(date, offSet);
        } else if (len == 1) {
            result = DataTypeConversion.byteToInt(date[offSet]);
        } else {
            result = 0;
        }
        return result;
    }

    private synchronized void parseDeviceType(byte[] bytes) {
        if (bytes[2] == SerialCommand.TX_RD_ONE) {
            String deviceType = DataTypeConversion.byteArrayToHexStrNo0x(bytes, 4, 1).toUpperCase();
            Logger.d("reboot get deviceType success deviceType          = " + deviceType);
            isGetDeviceType = true;
            if (deviceType.equals("00")) {
                //TODO:在读取机台类型时但是返回00时,将机台设置为默认类型
                ControlManager.getInstance().init(MyApplication.DEFAULT_DEVICE_TYPE);
                ErrorManager.init(MyApplication.DEFAULT_DEVICE_TYPE);
                return;
            }
            if (deviceType.equals("AC")) {
                ControlManager.getInstance().init(CTConstant.DEVICE_TYPE_AC);
                ErrorManager.init(CTConstant.DEVICE_TYPE_AC);
            } else if (deviceType.equals("AA")) {
                ControlManager.getInstance().init(CTConstant.DEVICE_TYPE_AA);
                ErrorManager.init(CTConstant.DEVICE_TYPE_AA);
            } else if (deviceType.equals("5A")) {
                ControlManager.getInstance().init(CTConstant.DEVICE_TYPE_DC);
                ErrorManager.init(CTConstant.DEVICE_TYPE_DC);
            } else {
                ControlManager.getInstance().init(MyApplication.DEFAULT_DEVICE_TYPE);
                ErrorManager.init(MyApplication.DEFAULT_DEVICE_TYPE);
            }
        }
    }

    private void parseDeviceInfo(byte[] bytes) {
        if (bytes[2] == SerialCommand.TX_RD_SOME) {

            int unit = bytes[4] & 0xFF;
            float maxSpeed = bytes[5] & 0xFF;
            float minSpeed = bytes[6] & 0xFF;
            int maxIncline = bytes[7] & 0xFF;

            //TODO: 注意,在AC00412(DC)的项目,这里的轮径值,不是按照通信协议的低位在前,高位在后实现的,而是正常的高位在前,低位在后
            //TODO: 新的DC项目需要确认这个数据的高低位状态
            float wheelSize = DataTypeConversion.bytesToShortLiterEnd(bytes, 8);

            maxSpeed = maxSpeed / 10f;
            minSpeed = minSpeed / 10f;
            wheelSize = wheelSize / 100f;

            SpManager.setIsMetric((unit == 0));
            //会出现数值溢出问题
            SpManager.setMaxSpeed(maxSpeed, (unit == 0));
            SpManager.setMinSpeed(minSpeed, (unit == 0));
            SpManager.setMaxIncline(maxIncline);
            SpManager.setWheelSize(wheelSize);
        }
    }

    //    private synchronized void parseDeviceWheelSize(byte[] bytes) {
//        if (bytes[2] == SerialCommand.TX_RD_ONE) {
//            int wheelSize = DataTypeConversion.bytesToShortLiterEnd(bytes, 4);
//            SpManager.setWheelSize(wheelSize / 100f);
//        }
//    }
//
    private synchronized void parseDeviceMaxADC(byte[] bytes) {
        if (bytes[2] == SerialCommand.TX_RD_ONE) {
            int maxADC = DataTypeConversion.doubleBytesToIntLiterEnd(bytes, 4);
            SpManager.setMaxAd(maxADC);
        }
    }

    private synchronized void parseDeviceMinADC(byte[] bytes) {
        if (bytes[2] == SerialCommand.TX_RD_ONE) {
            int minADC = DataTypeConversion.doubleBytesToIntLiterEnd(bytes, 4);
            SpManager.setMinAd(minADC);
        }
    }

    private synchronized void parseNcuVersion(byte[] bytes) {
        if (bytes[2] != SerialCommand.TX_RD_ONE) {
            return;
        }
        String res = "";
        if (bytes[3] == ParamCons.READ_NCU_VER && !isReadNcuVer) {
            isReadNcuVer = true;
            res = DataTypeConversion.byteArrayToHexStrNo0x(bytes, 4, 1);
            SpManager.setNcuNum("V" + res);
            Logger.d("reboot parseNcuVersion   READ_NCU_VER        = " + res);
        } else if (bytes[3] == ParamCons.READ_NCU_YEAR && !isReadNcuYear) {
            isReadNcuYear = true;
            res = DataTypeConversion.byteArrayToHexStrNo0x(bytes, 4, 2);
            SpManager.setNcuYear(res);
            Logger.d("reboot parseNcuVersion   READ_NCU_YEAR       = " + res);
        } else if (bytes[3] == ParamCons.READ_NCU_MONTH_DAY && !isReadNcuMonthDay) {
            isReadNcuMonthDay = true;
            res = DataTypeConversion.byteArrayToHexStrNo0x(bytes, 4, 2);
            SpManager.setNcuMonthDay(res);
            Logger.d("reboot parseNcuVersion   READ_NCU_MONTH_DAY  = " + res);
        }
    }

    private synchronized byte[] buildDeviceInfoData() {
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
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
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
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
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            return null;
        } else {
            return null;
        }
    }
}
