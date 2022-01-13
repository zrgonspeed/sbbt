package com.run.treadmill.manager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.run.treadmill.util.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/04/30
 */
public class PermissionManager {

    private static int mRequestCode;
    private static String[] mPerms;

    private static OnPermissionsCallback mCallback;

    /**
     * 判断是否有权限
     *
     * @param context
     * @param perms
     * @return
     */
    public static boolean hasPermissions(Context context, String... perms) {
        for (String perm : perms) {
            if (ActivityCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        Logger.d("sss", ">>>>>>>>>>>>>hasPermissions>>>>>>>>>>>>");
        return true;
    }

    /**
     * 申请权限
     *
     * @param activity
     * @param str
     * @param requestCode
     * @param perms
     */
    public static void requestPermissions(Activity activity, String str, int requestCode, String... perms) {
        mRequestCode = requestCode;
        mPerms = perms;
        Logger.d("sss", ">>>>>>>>>>>>>requestPermissions>>>>>>>>>>>>");
        ActivityCompat.requestPermissions(activity, perms, requestCode);
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, Activity activity) {
        if (mCallback == null) {
            return;
        }
        if (requestCode == mRequestCode) {
            List<String> permList = new ArrayList<>();
            boolean isSuccess = true;
            if (permissions.length == mPerms.length) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        permList.add(permissions[i]);
                        isSuccess = false;
                        break;
                    }
                }
                if (isSuccess) {
                    mCallback.onPermissionsGranted(requestCode);
                } else {
                    mCallback.onPermissionsDenied(requestCode, permList);
                }
            }
        }
    }

    public static void setPermissionCallback(OnPermissionsCallback cb) {
        mCallback = cb;
    }

    public interface OnPermissionsCallback {
        void onPermissionsGranted(int requestCode);

        void onPermissionsDenied(int requestCode, @NonNull List<String> perms);
    }

    /**
     * 检查GPS是否打开
     *
     * @return
     */
    public static boolean checkGPSIsOpen(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 控制GPS开关
     *
     * @param context
     * @param openGps 开或者关
     */
    public static void openOrCloseGPS(Context context, boolean openGps) {
        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, openGps ? 1 : 0);
    }

    public static boolean grantPermission(Context context, String packageName, String permission) {
        try {
            Method method = context.getPackageManager().getClass().getDeclaredMethod("grantRuntimePermission",
                    String.class, String.class, UserHandle.class);
            method.invoke(context.getPackageManager(), packageName, permission, android.os.Process.myUserHandle());
            Logger.d("sss", "grantPermission succeed---(" + permission + ")---");
        } catch (Exception e) {
            Logger.d("sss", "grantPermission fail------(" + permission + ")---");
            return false;
        }
        return true;
    }
}