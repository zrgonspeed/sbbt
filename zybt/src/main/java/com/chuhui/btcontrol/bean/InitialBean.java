package com.chuhui.btcontrol.bean;

import android.util.Log;


import com.chuhui.btcontrol.BtHelper;

import java.util.Arrays;
import java.util.List;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2022/7/26
 */
public class InitialBean {
    /**
     * 设备类型
     * @see com.chuhui.btcontrol.BtHelper.BtHandler#setMachineType(int)
     * */
    private int machineType;
    /** 扬升范围*/
    private float minIncline, maxIncline, inclineSchg;
    /** 速度范围*/
    private float minSpeed, maxSpeed, speedSchg;

    /** 总时间*/
    public int totalHours;
    /** 总距离*/
    public int totalDistance;
    /** 总步数*/
    public int totalSteps;
    /** 错误集合*/
    public byte[] errs = new byte[BtHelper.errLogCount];

    public int getMachineType() {
        return machineType;
    }

    public void setMachineType(int machineType) {
        this.machineType = machineType;
    }

    public float getMinIncline() {
        return minIncline;
    }

    public void setMinIncline(float minIncline) {
        this.minIncline = minIncline;
    }

    public float getMaxIncline() {
        return maxIncline;
    }

    public void setMaxIncline(float maxIncline) {
        this.maxIncline = maxIncline;
    }

    public float getMinSpeed() {
        return minSpeed;
    }

    public void setMinSpeed(float minSpeed) {
        this.minSpeed = minSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getInclineSchg() {
        return inclineSchg;
    }

    public void setInclineSchg(float inclineSchg) {
        this.inclineSchg = inclineSchg;
    }

    public float getSpeedSchg() {
        return speedSchg;
    }

    public void setSpeedSchg(float speedSchg) {
        this.speedSchg = speedSchg;
    }

    public void setRangeIncline(float minIncline, float maxIncline, float inclineSchg){
        this.minIncline = minIncline;
        this.maxIncline = maxIncline;
        this.inclineSchg = inclineSchg;
    }

    public void setRangeSpeed(float minSpeed, float maxSpeed, float SpeedSchg){
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.speedSchg = SpeedSchg;
    }

    public static final int DEVICE_TYPE_AC = 0x57;
    public static final int DEVICE_TYPE_DC = 0x5A;
    public void setErrCodes(List<String> codes, int deviceType){
        Arrays.fill(errs, (byte) 0x00);
        if(codes == null || codes.size() <= 0){
            return;
        }
        byte curErrCode;

        if (deviceType == DEVICE_TYPE_AC) {
            Log.i("sss","codes.size() == " + codes.size() + "   errs.length == " + errs.length);

            // AC
            for (int i = 0; i < Math.min(codes.size(), errs.length); i++) {
                // Log.i("sss", "i==" + i);
                switch (codes.get(i)) {
                    case "01":
                        curErrCode = 0x03;
                        break;
                    case "02":
                        curErrCode = 0x02;
                        break;
                    case "04":
                        curErrCode = 0x41;
                        break;
                    case "06":
                        curErrCode = 0x04;
                        break;
                    case "07":
                        curErrCode = 0x05;
                        break;
                    case "08":
                        curErrCode = 0x06;
                        break;
                    case "09":
                        curErrCode = 0x07;
                        break;
                    case "0A":
                        curErrCode = 0x08;
                        break;
                    case "0B":
                        curErrCode = 0x09;
                        break;
                    case "0C":
                        curErrCode = 0x0A;
                        break;
                    case "21":
                        curErrCode = 0x0B;
                        break;
                    case "22":
                        curErrCode = 0x0C;
                        break;
                    case "23":
                        curErrCode = 0x0D;
                        break;
                    case "25":
                        curErrCode = 0x0E;
                        break;
                    case "26":
                        curErrCode = 0x0F;
                        break;
                    case "27":
                        curErrCode = 0x10;
                        break;
                    case "28":
                        curErrCode = 0x11;
                        break;
                    case "29":
                        curErrCode = 0x12;
                        break;
                    case "50":
                    case "51":
                        curErrCode = 0x31;
                        break;
                    case "52":
                    case "16":
                        curErrCode = 0x31;
                        break;
                    default:
                        curErrCode = 0x00;
                        break;
                }
                Log.d("sssAC", "zy err =>  原err: " + codes.get(i) + "  ==  转换后err: >> " + curErrCode);
                errs[i] = curErrCode;
            }
        }

        if (deviceType == DEVICE_TYPE_DC) {
            // DC
            for (int i = 0; i < Math.min(codes.size(), errs.length); i++) {
                switch (codes.get(i)) {
                    case "1":
                        curErrCode = 0x42;
                        break;
                    case "2":
                        curErrCode = 0x41;
                        break;
                    case "3":
                        curErrCode = 0x20;
                        break;
                    case "4":
                        curErrCode = 0x44;
                        break;
                    case "5":
                        curErrCode = 0x31;
                        break;
                    case "6":
                        curErrCode = 0x31;
                        break;
                    case "7":
                        curErrCode = 0x44;
                        break;
                    case "8":
                        curErrCode = 0x01;
                        break;
                    default:
                        curErrCode = 0x00;
                        break;
                }
                Log.d("sssDC", "zy err =>  原err: " + codes.get(i) + "  ==  转换后err: >> " + curErrCode);
                errs[i] = curErrCode;
            }
        }

    }
}