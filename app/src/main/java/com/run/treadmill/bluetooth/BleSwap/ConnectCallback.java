package com.run.treadmill.bluetooth.BleSwap;

public interface ConnectCallback {
    /**
     * 获得通知之后
     */

    void onConnSuccess();

    /**
     * 断开或连接失败
     */
    void onConnFailed();
}
