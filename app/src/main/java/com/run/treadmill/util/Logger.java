package com.run.treadmill.util;

import android.util.Log;

/**
 * @Description Log统一管理类
 * @Author GaleLiu
 * @Time 2019/01/09
 */
public class Logger {

    public static boolean isDebug = true;

    private final static String APP_TAG = "TAG";

    /**
     * 获取相关数据:类名,方法名,行号等.用来定位行<br>
     * at cn.utils.BigMenuActivity.onCreate(BigMenuActivity.java:17) 就是用來定位行的代碼<br>
     *
     * @return [ Thread:main, at
     * cn.utils.BigMenuActivity.onCreate(BigMenuActivity.java:17)]
     */
    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts != null) {
            for (StackTraceElement st : sts) {
                if (st.isNativeMethod()) {
                    continue;
                }
                if (st.getClassName().equals(Thread.class.getName())) {
                    continue;
                }
                if (st.getClassName().equals(Logger.class.getName())) {
                    continue;
                }
                return "[at " + "." + st.getMethodName()
                        + "(" + st.getFileName() + ":" + st.getLineNumber() + ")" + " ]";
            }
        }
        return null;
    }


    public static void v(String msg) {
        if (isDebug) {
            Log.v(APP_TAG, getMsgFormat(msg));
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            Log.v(tag, getMsgFormat(msg));
        }
    }


    public static void d(String msg) {
        if (isDebug) {
            Log.d(APP_TAG, getMsgFormat(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, getMsgFormat(msg));
        }
    }


    public static void i(String msg) {
        if (isDebug) {
            Log.i(APP_TAG, getMsgFormat(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, getMsgFormat(msg));
        }
    }


    public static void w(String msg) {
        if (isDebug) {
            Log.w(APP_TAG, getMsgFormat(msg));
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, getMsgFormat(msg));
        }
    }


    public static void e(String msg) {
        if (isDebug) {
            Log.e(APP_TAG, getMsgFormat(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, getMsgFormat(msg));
        }
    }

    /**
     * 输出格式定义
     */
    private static String getMsgFormat(String msg) {
        return msg + "              ;" + getFunctionName();
    }

}