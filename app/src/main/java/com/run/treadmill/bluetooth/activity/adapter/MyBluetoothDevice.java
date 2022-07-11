package com.run.treadmill.bluetooth.activity.adapter;

import android.bluetooth.BluetoothDevice;

public class MyBluetoothDevice {
    private final BluetoothDevice mDevice;

    public MyBluetoothDevice(BluetoothDevice device) {
        this.mDevice = device;
    }

    private int btStatus;

    public void setBTStatus(int status) {
        this.btStatus = status;
    }

    public int getBtStatus() {
        return btStatus;
    }

}
