package com.run.treadmill.util;

import android.os.SystemClock;

public class ButtonUtils {
    private static boolean isOk = false;

    public static boolean canResponse() {
        if (isOk) {
            return false;
        }

        isOk = true;
        return true;
    }

    static {
        ThreadUtils.runInThread(() -> {
            while (true) {
                SystemClock.sleep(200);
                isOk = false;
            }
        });
    }
}
