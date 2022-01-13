package com.run.treadmill.interceptor;

import android.os.Message;

import com.run.serial.SerialUtils;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.MsgWhat;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.control.NormalParam;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.Logger;

/**
 * @Description 常态包拦截器
 * @Author GaleLiu
 * @Time 2019/09/12
 */
public class NormalInterceptor implements SerialInterceptor {

    @Override
    public Message intercept(Chain chain) {
        byte[] data = ((RealChain) chain).getmData();
        Message msg = new Message();
        int[] normalArray = new int[5];
        int curKeyValue = ((RealChain) chain).resolveDate(data, NormalParam.KEY_VALUE_INX, NormalParam.KEY_VALUE_LEN);
//        if (curKeyValue != 0) {
//            Logger.d("========按键值==========" + curKeyValue);
//        }
        int keyResult = SerialKeyValue.isNeedSendMsg(curKeyValue);
        normalArray[0] = keyResult;
        SerialUtils.keyValue = keyResult;
        normalArray[1] = ((RealChain) chain).resolveDate(data, NormalParam.BELT_STATE_INX, NormalParam.BELT_STATE_LEN);
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            //如果无其他错误只有扬升错误，不管扬升处于什么状态都认为可以开始运动
            normalArray[2] = (data[NormalParam.INCLINE_STATE_INX] & 0x03);
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            //如果无其他错误只有扬升错误，不管扬升处于什么状态都认为可以开始运动
            normalArray[2] = (data[NormalParam.INCLINE_STATE_INX] & 0x03);
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            normalArray[2] = ((RealChain) chain).resolveDate(data, NormalParam.INCLINE_STATE_INX, NormalParam.INCLINE_STATE_LEN);
        }
        normalArray[3] = ((RealChain) chain).resolveDate(data, NormalParam.CURR_SPEED_INX, NormalParam.CURR_SPEED_LEN);
        normalArray[4] = ((RealChain) chain).resolveDate(data, NormalParam.CURR_AD_INX, NormalParam.CURR_AD_LEN);
        msg.what = MsgWhat.MSG_NOMAL_DATA;
        msg.obj = normalArray;
        return msg;
    }
}