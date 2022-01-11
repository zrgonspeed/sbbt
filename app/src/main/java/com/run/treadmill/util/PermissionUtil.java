package com.run.treadmill.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.collection.SimpleArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

/**
 * Author by HEKE
 *
 * @time 2017/4/11 17:32
 * TODO: 权限申请
 */
public class PermissionUtil {

    public static final int REQUEST_SHOWCAMERA = 0;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_RECORD_AUDIO = 2;
    public static final int REQUEST_CONTACTS = 3;
    public static final int REQUEST_LOCATION = 4;
    public static final int REQUEST_ALERT_WINDOW = 5;


    private static final SimpleArrayMap<String, Integer> MIN_SDK_PERMISSIONS;

    static {
        MIN_SDK_PERMISSIONS = new SimpleArrayMap<String, Integer>(8);
        MIN_SDK_PERMISSIONS.put("com.android.voicemail.permission.ADD_VOICEMAIL", 14);
        MIN_SDK_PERMISSIONS.put("android.permission.BODY_SENSORS", 20);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_CALL_LOG", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_EXTERNAL_STORAGE", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.USE_SIP", 9);
        MIN_SDK_PERMISSIONS.put("android.permission.WRITE_CALL_LOG", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.SYSTEM_ALERT_WINDOW", 23);
        MIN_SDK_PERMISSIONS.put("android.permission.WRITE_SETTINGS", 23);
    }

    private static boolean permissionExists(String permission) {
        Integer minVersion = MIN_SDK_PERMISSIONS.get(permission);
        return minVersion == null || Build.VERSION.SDK_INT >= minVersion;
    }

    /**
     * 悬浮窗权限
     */
    public static boolean hasAlertWindowPermission(Activity activity) {
        int hasPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.SYSTEM_ALERT_WINDOW);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
                    PermissionUtil.REQUEST_ALERT_WINDOW);
            return false;
        }
        return true;
    }

    /**
     * 摄像机权限
     *
     * @param activity
     */
    public static boolean hasCameraPermission(Activity activity) {
        int hasPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA);
        int hasWritePermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (!permissionExists(Manifest.permission.CAMERA)) {
            Log.e("permission", "your system does not suppport" + Manifest.permission.CAMERA + " permission");
            return false;
        }
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    PermissionUtil.REQUEST_SHOWCAMERA);
            return false;
        }
        if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionUtil.REQUEST_SHOWCAMERA);
            return false;
        }

        return true;
    }


    /**
     * 检测是否有读写权限
     *
     * @param activity
     */
    public static boolean hasReadExternalStoragePermission(Activity activity) {
        int hasPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (!permissionExists(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.e("permission", "your system does not suppport " + Manifest.permission.READ_EXTERNAL_STORAGE + " permission");
            return false;
        }
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionUtil.REQUEST_READ_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    /**
     * 音频处理权限，录音等
     *
     * @param activity
     */
    public static boolean hasRecordAudioPermission(Activity activity) {
        int hasPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO);
        if (!permissionExists(Manifest.permission.RECORD_AUDIO)) {
            Log.e("permission", "your system does not suppport" + Manifest.permission.RECORD_AUDIO + " permission");
            return false;
        }
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PermissionUtil.REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    public static boolean hasContactsPermission(Activity activity) {
        int hasWPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_CONTACTS);
        int hasRPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_CONTACTS);

        if (hasRPermission == PackageManager.PERMISSION_GRANTED && hasWPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS},
                PermissionUtil.REQUEST_CONTACTS);
        return false;
    }

    /**
     * 定位权限
     *
     * @param activity
     */
    public static boolean hasLocationPermission(Activity activity) {
        int hasFPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFPermission == PackageManager.PERMISSION_GRANTED && hasCPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                PermissionUtil.REQUEST_LOCATION);
        return false;
    }

}
