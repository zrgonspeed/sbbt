package com.run.treadmill;

import com.run.serial.LogUtils;
import com.run.treadmill.util.Logger;

public class AppDebug {
    public static boolean debug = false;
    private Custom custom;

    static {
        LogUtils.printLog = false;
        Logger.i("AppDebug static  printLog == " + LogUtils.printLog);
    }
}
