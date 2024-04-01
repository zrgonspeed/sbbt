package com.run.treadmill.util.clicktime;

public class HomeClickUtils {
    private static long lastBuzzOnceTime;

    public static boolean canResponse() {
        long curTime = System.currentTimeMillis();
        if (Math.abs(curTime - lastBuzzOnceTime) < 250) {
            return false;
        }
        lastBuzzOnceTime = System.currentTimeMillis();
        return true;
    }
}
