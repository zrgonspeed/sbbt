package com.chuhui.btcontrol.zybt;

import android.util.Log;

import androidx.annotation.NonNull;

import com.chuhui.btcontrol.BaseBtControl;
import com.chuhui.btcontrol.BtHelper;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/03/26
 */
public class ZyTxDataTask implements Runnable {
    private ZyBt mZyBt;

    ZyTxDataTask(@NonNull ZyBt zybt) {
        this.mZyBt = zybt;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if(!mZyBt.isSendData){
                    Thread.sleep(100);
                    continue;
                }

                // 上电操作
                if(mZyBt.msgWhats.size() > 0){
                    mZyBt.currMsg = mZyBt.msgWhats.getFirst();
                    switch (mZyBt.currMsg){
                        case BaseBtControl.ACTION_REBOOT:
                            if(!BtHelper.getInstance().connected()){
                                mZyBt.sendReboot();
                            }
                            break;
                        case BaseBtControl.ACTION_WAKE:
                            if(!BtHelper.getInstance().connected()){
                                mZyBt.sendModeSetting();
                            }
                            break;
                        case BaseBtControl.ACTION_GET_NAME:
                            mZyBt.sendGetDeviceName();
                            break;
                        case BaseBtControl.ACTION_GET_VER:
                            mZyBt.sendGetDeviceVer();
                            break;
                        case BaseBtControl.ACTION_GET_MAC:
                            mZyBt.sendGetDeviceMac();
                            break;
                        case ZyBt.ACTION_SET_MACHINE_TYPE:
                            mZyBt.setMachineType(mZyBt.mInitialBean.getMachineType());
                            mZyBt.pollFirstAndNext();
                            break;
                        case ZyBt.ACTION_SET_RANGE_INCLINE:
                            mZyBt.setInclineRange(
                                    (int)(mZyBt.mInitialBean.getMinIncline() * 10),
                                    (int)(mZyBt.mInitialBean.getMaxIncline() * 10),
                                    (int)(mZyBt.mInitialBean.getInclineSchg() * 10));
                            mZyBt.pollFirstAndNext();
                            break;
                        case ZyBt.ACTION_SET_RANGE_SPEED:
                            mZyBt.setSpeedRange(
                                    (int)(mZyBt.mInitialBean.getMinSpeed() * 100),
                                    (int) (mZyBt.mInitialBean.getMaxSpeed() * 100),
                                    (int) (mZyBt.mInitialBean.getSpeedSchg() * 100));
                            mZyBt.pollFirstAndNext();
                            break;
                        default:
                            break;
                    }
                    Thread.sleep(400);
                }

                Thread.sleep(100);

                while (BtHelper.getInstance().connected()) {
                    mZyBt.sendRunParamToBT();
                    Thread.sleep(500);
                }
            }
        }catch (Exception e){
            Log.e("ZyTxDataTask","数据发送出现异常！");
        }
    }
}