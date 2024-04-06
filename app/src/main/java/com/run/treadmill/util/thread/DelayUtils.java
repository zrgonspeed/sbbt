package com.run.treadmill.util.thread;

import android.view.View;

public class DelayUtils {
    public static void post(View view, long delay, Runnable runnable) {
        view.postDelayed(() -> {
            runnable.run();
        }, delay);
    }
}
