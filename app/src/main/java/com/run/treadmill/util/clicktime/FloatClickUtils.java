package com.run.treadmill.util.clicktime;

public class FloatClickUtils {
    private static long lastBuzzOnceTime;

    public static boolean canResponse() {
        long curTime = System.currentTimeMillis();
        if (Math.abs(curTime - lastBuzzOnceTime) < 300) {
            return false;
        }
        lastBuzzOnceTime = System.currentTimeMillis();
        return true;
    }
}
