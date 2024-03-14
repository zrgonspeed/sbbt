package com.run.treadmill.util;

import android.graphics.drawable.Drawable;

import com.run.treadmill.reboot.MyApplication;

public class ResourceUtils {
    public static Drawable getDraw(int id) {
        return MyApplication.getContext().getDrawable(id);
    }

    public static String getString(int id) {
        return MyApplication.getContext().getString(id);
    }

    public static int getColor(int id) {
        return MyApplication.getContext().getResources().getColor(id, null);
    }

    public static int getDimensionPixelSize(int id) {
        return MyApplication.getContext().getResources().getDimensionPixelSize(id);
    }
}
