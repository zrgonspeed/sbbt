package com.run.treadmill.autoupdate.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WvVersionUtil {
    public static String getSpecAppVersionName(Context context, String name) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(name, 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            WvLogger.e("VersionInfo", "Exception" + e);
            return "0.0";
        }
        return versionName;
    }

    /**
     * 判断是否是新版本
     *
     * @param curVersion 当前版本号
     * @param newVersion 新版本号
     * @return
     */
    public static boolean isNewVersion(String curVersion, String newVersion) {
        if (curVersion.isEmpty()) {
            return true;
        }
        if (curVersion.equals(newVersion)) {
            return false;
        }
        curVersion = replaceAllUnNum(curVersion, ".");
        newVersion = replaceAllUnNum(newVersion, ".");

        String curDataStr = curVersion.split(" ")[0];
        String newDataStr = newVersion.split(" ")[0];
        String[] curData = curDataStr.split("\\.");
        String[] newData = newDataStr.split("\\.");
        for (int i = 0; i < Math.min(curData.length, newData.length); i++) {
            if (Integer.parseInt(newData[i].trim()) > Integer.parseInt(curData[i].trim())) {
                return true;
            }
            if (Integer.parseInt(newData[i].trim()) < Integer.parseInt(curData[i].trim())) {
                return false;
            }
        }
        return (curData.length < newData.length);
    }

    /**
     * 字串中把非数字替换成某个字串
     *
     * @param str
     * @param replacement
     * @return
     */
    public static String replaceAllUnNum(String str, String replacement) {
        Pattern pattern = Pattern.compile("[^0-9]+");
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll(replacement).trim();
    }
}