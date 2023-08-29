package com.run.treadmill.util;

public class VolumeUtils {
    private static long lastBuzzOnceTime;

    public static boolean canResponse() {
        long curTime = System.currentTimeMillis();
        if (Math.abs(curTime - lastBuzzOnceTime) < 200) {
            return false;
        }
        lastBuzzOnceTime = System.currentTimeMillis();
        return true;
    }
}
