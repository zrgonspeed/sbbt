package com.run.treadmill.bluetooth.adapter;

import android.bluetooth.BluetoothDevice;

public class MyBluetoothDevice {
    private BluetoothDevice mDevice;

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
