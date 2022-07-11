package com.run.treadmill.bluetooth.activity.adapter;

import android.bluetooth.BluetoothDevice;

public interface BleAdapter {
    void addDevice(BluetoothDevice device, Short rssi);

    boolean hasConnecting();
}
