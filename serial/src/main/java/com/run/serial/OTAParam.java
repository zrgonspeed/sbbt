package com.run.serial;

public class OTAParam {
    public static boolean reSend = false;
    public static byte[] pkgBytes;
    public static int index;


    /**
     * 0x6D：下发更新命令 0x6D 参数 0x69 --转接板进入OTA更新
     */
    public final static int CMD_UPDATE = 0x6D;

    /**
     * 0xD0：下发更新数据命令
     */
    public final static int CMD_BIN_DATA = 0xD0;

    public static boolean isSendBinCnt = false;
    public static boolean isSendBinData = false;
    public static boolean isSendBinOneFrame = true;
    public static boolean isInBinUpdateStatus = false;

    public static byte[] readUpdateReplyPkg = {(byte) 0x70, 0x6C, 0x65, 0x61, 0x73, 0x65,
            (byte) 0x20, 0x30, 0x20, (byte) 0x0A, (byte) 0x00,};

    public static byte[] otaConnectPkg = {(byte) 0xAA, 0x00, 0x11, 0x71, 0x00,
            (byte) 0x81, 0x01, 0x01, (byte) 0xE0, (byte) 0x91,
            0x28, (byte) 0xCE, 0x47, 0x44, 0x00,
            0x24, 0x00, 0x00, 0x01, (byte) 0xF4,
            (byte) 0xF0,};

}
