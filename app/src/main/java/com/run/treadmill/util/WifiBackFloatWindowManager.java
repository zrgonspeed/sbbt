package com.run.treadmill.util;

import com.run.treadmill.reboot.MyApplication;

public class WifiBackFloatWindowManager {
    private static WifiBackFloatWindow wifiBackFloatWindow;

    public static void startWifiBackFloat() {
        if (wifiBackFloatWindow != null) {
            wifiBackFloatWindow.stopFloat1();
            wifiBackFloatWindow = null;
        }
        wifiBackFloatWindow = new WifiBackFloatWindow(MyApplication.getContext());
        wifiBackFloatWindow.startFloat();
    }
}
