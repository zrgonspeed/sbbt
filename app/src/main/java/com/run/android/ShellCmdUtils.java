package com.run.android;


import android.util.Log;

public class ShellCmdUtils {
    private final String TAG = "ShellCmdUtils";
    private static ShellCmdUtils mInstance = null;

    public ShellCmdUtils() {
        nativeShellCmdUtilsInit();
    }

    public static ShellCmdUtils getInstance() {

        if (null == mInstance) {
            synchronized (ShellCmdUtils.class) {
                if (null == mInstance) {
                    mInstance = new ShellCmdUtils();
                }
            }
        }
        return mInstance;
    }

    public int execCommand(String cmd) {
        Log.v("shell", "执行 " + cmd);
        return nativeExecShellCmd(cmd);
    }

    private static native int nativeShellCmdUtilsInit();

    private static native void nativeShellCmdUtilsExit();

    private static native int nativeExecShellCmd(String cmd);

    static {
        System.loadLibrary("ShellCmdUtils_jni");
    }

}
