package com.run.treadmill.util;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.run.treadmill.util.thread.ThreadPoolManager;

public class ThreadUtils {
    private static volatile Handler sMainThreadHandler;

    public static void runInThread(Runnable runnable, long delay) {
        new Thread(() -> {
            SystemClock.sleep(delay);
            runnable.run();
        }).start();
    }

    public static void runInThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static void postOnMainThread(Runnable runnable) {
        getUiThreadHandler().post(runnable);
    }

    public static void postOnMainThread(Runnable runnable, long delay) {
        runInThread(() -> {
            getUiThreadHandler().post(runnable);
        }, delay);
    }

    private static Handler getUiThreadHandler() {
        if (sMainThreadHandler == null) {
            sMainThreadHandler = new Handler(Looper.getMainLooper());
        }

        return sMainThreadHandler;
    }

    public static void initThreadPool() {
        ThreadPoolManager.getInstance().createThreadPool();
    }

    public static void runInPoolThread(Runnable runnable) {
        ThreadPoolManager.getInstance().addTask(runnable);
    }
}
