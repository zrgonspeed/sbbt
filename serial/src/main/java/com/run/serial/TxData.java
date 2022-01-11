package com.run.serial;

import androidx.annotation.NonNull;

public class TxData {
    private static TxData txData = null;

    private byte ctrl = 0x00;
    private byte param = 0x00;
    private byte[] data = {};

    private byte normalCtrl = 0x00;
    private int normalParam = 0x00;
    private byte[] normalData = {};

    private TxData() {
    }

    public static TxData getInstance() {
        if (txData == null) {
            synchronized (TxData.class) {
                if (txData == null) {
                    txData = new TxData();
                }
            }
        }
        return txData;
    }

    public synchronized void setCtrl(byte b) {
        ctrl = b;
    }

    public synchronized void setParam(byte b) {
        param = b;
    }

    /**
     * 不能为null,但是可以是长度为0的byte数组
     *
     * @param bs
     */
    public synchronized void setData(@NonNull byte[] bs) {
        data = bs;
    }

    public synchronized void setNormalCtrl(byte normalCtrl) {
        this.normalCtrl = normalCtrl;
    }

    /**
     * <br>当常态包是使用比如是0x5A的时候,这个参数位必须设置为257</br>
     * <br>如果不是使用0x5A作为常态包的时候,这应该是参数位</br>
     *
     * @param normalParam
     */
    public synchronized void setNormalParam(int normalParam) {
        this.normalParam = normalParam;
    }

    /**
     * 不能为null,但是可以是长度为0的byte数组
     *
     * @param bs
     */
    public synchronized void setNormalData(@NonNull byte[] bs) {
        this.normalData = bs;
    }

    protected synchronized byte getCtrl() {
        return ctrl;
    }

    protected synchronized byte getParam() {
        return param;
    }

    protected synchronized byte[] getData() {
        return data;
    }

    protected synchronized byte getNormalCtrl() {
        return normalCtrl;
    }

    protected synchronized int getNormalParam() {
        return normalParam;
    }

    protected synchronized byte[] getNormalData() {
        return normalData;
    }

}
