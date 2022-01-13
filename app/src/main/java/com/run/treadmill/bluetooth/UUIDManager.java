package com.run.treadmill.bluetooth;


public class UUIDManager {
    /**
     * 服务的UUID
     */
    public static final String SERVICE_UUID = "00006a00-0000-1000-8000-00805f9b34fb";
    /**
     * 订阅通知的UUID
     */
    public static final String NOTIFY_UUID = "00006a02-0000-1000-8000-00805f9b34fb";
    /**
     * 写出数据的UUID
     */
    public static final String WRITE_UUID = "00006a02-0000-1000-8000-00805f9b34fb";

    /**
     * NOTIFY里面的Descriptor UUID
     */
    public static final String NOTIFY_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    public static final String UUID_SERVER = "6e40fd09-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_SERVER_WRITE = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_SERVER_NOTIFY = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    public static final String UUID_HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
}
