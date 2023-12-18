package com.run.treadmill.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * sharedpreferences类，用于存放一些常用的变量值
 *
 * @author chenyan
 */
public class StorageParam {
    private static String SETTINGS_TREADMILL = "StorageParam";

    private static Context context;

    public static void setContext(Context c) {
        context = c;
    }

    public static void setSpName(String name) {
        SETTINGS_TREADMILL = name;
    }

    public static String getParam(String tagName, String defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        return sp.getString(tagName, defaultValue);
    }

    public static void setParam(String tagName, String value) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(tagName, value);
        editor.commit();
    }

    public static float getParam(String tagName, float defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        return sp.getFloat(tagName, defaultValue);
    }

    public static void setParam(String tagName, float value) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putFloat(tagName, value);
        editor.commit();
    }

    public static long getParam(String tagName, long defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        return sp.getLong(tagName, defaultValue);
    }

    public static void setParam(String tagName, long value) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putLong(tagName, value);
        editor.commit();
    }

    public static int getParam(String tagName, int defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        return sp.getInt(tagName, defaultValue);
    }

    public static void setParam(String tagName, int value) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putInt(tagName, value);
        editor.commit();
    }

    public static boolean getParam(String tagName, boolean defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        return sp.getBoolean(tagName, defaultValue);
    }

    public static void setParam(String tagName, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(tagName, value);
        editor.commit();
    }


    public static void remove(String tagName) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.remove(tagName);
        editor.commit();
    }

    /**
     * 获取Editor
     *
     * @return
     */
    public static Editor getEditor() {
        return context.getSharedPreferences(SETTINGS_TREADMILL, Context.MODE_PRIVATE).edit();
    }
}
