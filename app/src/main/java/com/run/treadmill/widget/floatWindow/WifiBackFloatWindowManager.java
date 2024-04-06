package com.run.treadmill.widget.floatWindow;

import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.widget.floatWindow.WifiBackFloatWindow;

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
