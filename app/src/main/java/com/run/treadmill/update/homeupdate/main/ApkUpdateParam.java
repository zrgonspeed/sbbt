package com.run.treadmill.update.homeupdate.main;

import android.content.Context;
import android.os.Environment;

import com.run.treadmill.sp.SpManager;

public class ApkUpdateParam {
    public static final String DOWNLOAD_PATH_CN = Environment.getExternalStorageDirectory().getAbsolutePath() + "/treadmill" + "/cn";
    public static final String DOWNLOAD_PATH_OTHER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/treadmill" + "/other";

    public static final String UPDATE_THIRD_A133_END = "/restapi/apk/A133/update/treadmill?apkNames=";
    /**
     * 是否是测试服务器
     */
    private final static boolean isTestServer = false;
    /**
     * 国外服务器
     */
    public static String NOT_CN_HOST;
    /**
     * 国内服务器
     */
    public static String CN_HOST;

    static {
        if (isTestServer) {
            NOT_CN_HOST = "http://apk-test.anplus-tech.com";
            CN_HOST = "http://apkchina-test.anplus-tech.com";
        } else {
            NOT_CN_HOST = "http://apk.anplus-tech.com";
            CN_HOST = "http://apkchina.anplus-tech.com";
        }
    }

    public static String getUpdateHost(Context mContext) {
        String reqUrl;
        if (mContext.getResources().getConfiguration().locale.getCountry().equals("CN")) {
            if (SpManager.getAlterUpdatePath()) {
                reqUrl = NOT_CN_HOST;
            } else {
                reqUrl = CN_HOST;
            }
        } else {
            if (SpManager.getAlterUpdatePath()) {
                reqUrl = CN_HOST;
            } else {
                reqUrl = NOT_CN_HOST;
            }
        }
        return reqUrl;
    }

    public static String getDownloadPath(String url) {
        if (url.contains(NOT_CN_HOST)) {
            return DOWNLOAD_PATH_OTHER;
        }

        return DOWNLOAD_PATH_CN;
    }
}
