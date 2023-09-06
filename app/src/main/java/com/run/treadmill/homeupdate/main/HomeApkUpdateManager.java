package com.run.treadmill.homeupdate.main;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.google.gson.Gson;
import com.run.android.ShellCmdUtils;
import com.run.treadmill.activity.appStore.AppBean;
import com.run.treadmill.base.MyApplication;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.http.DownloadListener;
import com.run.treadmill.http.OkHttpCallBack;
import com.run.treadmill.http.OkHttpHelper;
import com.run.treadmill.manager.Md5Manager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.WifiBTStateManager;
import com.run.treadmill.util.DownloadTimeUtils;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.VersionUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.HttpUrl;

public class HomeApkUpdateManager implements DownloadListener, OkHttpCallBack {
    private static final String TAG = HomeApkUpdateManager.class.getSimpleName();
    private Context mContext;

    private int isPermitUpdateApk = CTConstant.INSTALL_UNKNOW;
    /**
     * 是否成功请求过检测新版apk
     */
    public boolean isHasRequestNewApk = false;
    /**
     * 是否为切换路径后成功请求过检测新版apk
     */
    private boolean isAlterUpdatePath = false;
    /**
     * 是否已经下载apk
     */
    private boolean isDownloadApk = false;

    private Thread mThread;

    /**
     * 是否有网络
     */
    private boolean isHasNetword;
    /**
     * 当前是否是新版本
     */
    public boolean isNewVersion;

    private AppBean.AppInfo appInfo;
    private String reqUrl = "";
    private long time;

    private UpdateViewCallBack mUpdateViewCallBack;

    public interface UpdateViewCallBack {
        void showTipsPoint();

        void showUpdateApk();
    }

    /**
     * 对比版本信息
     */
    private void comparedAppVersion() {
        String appVersionName = VersionUtil.getAppVersionName(mContext);
        String lastAppVersionName = SpManager.getLastAppVersionName();
        isNewVersion = VersionUtil.isNewVersion(lastAppVersionName, appVersionName, "V");
        Logger.i(TAG, "appVersionName == " + appVersionName);
        Logger.i(TAG, "lastAppVersionName == " + lastAppVersionName);
        Logger.i(TAG, "isNewVersion == " + isNewVersion);

        if (isNewVersion && SpManager.getUpdateIsNetwork()) {
            if (mUpdateViewCallBack != null) {
                mUpdateViewCallBack.showTipsPoint();
            }
        }
        if (!appVersionName.equals(lastAppVersionName)) {
            SpManager.setLastAppVersionName(appVersionName);
        }
        SpManager.setUpdateIsNetwork(false);
    }


    @Override
    public void onSuccess(Call call, String response) {
        HttpUrl url = call.request().url();
        Logger.i(TAG, "==== onSuccess ==== url ==" + url);
        Logger.i(TAG, "==== onSuccess ====" + response);

        Gson mGson = new Gson();
        List<AppBean.AppInfo> apkInfos = mGson.fromJson(response, AppBean.class).getApkInfos();
        if (apkInfos.isEmpty()) {
            return;
        }

        appInfo = apkInfos.get(0);
        if (appInfo == null) {
            return;
        }

        String currentVersion = VersionUtil.getSpecAppVersionName(mContext, mContext.getApplicationContext().getPackageName());
        String serverVersion = appInfo.getVersion();
        boolean isNewVersion = VersionUtil.isNewVersion(currentVersion, serverVersion);
        Logger.i(TAG, "currentVersion: " + currentVersion);
        Logger.i(TAG, "serverVersion: " + serverVersion);
        Logger.i(TAG, "isNewVersion(): " + isNewVersion);

        //已经是最新版了
        if (!isNewVersion) {
            Logger.i(TAG, "当前电子表是最新版");
            //检测更新后是新版，并且跟文件版本不同，则是新版
            comparedAppVersion();
            //如果没有更新则删除已存在的apk
            if (FileUtil.isCheckExist(ApkUpdateParam.getDownloadPath(this.reqUrl) + "/" + InitParam.APK_NAME)) {
                //TODO:需要开线程?
                FileUtil.deleteApkFile(ApkUpdateParam.getDownloadPath(this.reqUrl) + "/" + InitParam.APK_NAME);
                FileUtil.deleteApkFile(ApkUpdateParam.getDownloadPath(this.reqUrl) + "/" + InitParam.PROJECT_NAME);
            }
            return;
        } else {
            Logger.i(TAG, "当前电子表不是最新版，服务器有最新版");
        }

        File apkFile = new File(ApkUpdateParam.getDownloadPath(this.reqUrl) + "/" + InitParam.APK_NAME);
        String apkMD5 = Md5Manager.fileToMD5(apkFile.getPath());

        if (apkFile.exists() && apkMD5 != null && apkMD5.compareTo(appInfo.getSign()) == 0) {
            Logger.i(TAG, "apk文件完整");
            Logger.i(TAG, "apk文件路径: " + apkFile.getPath());

            isDownloadApk = true;
            if (isPermitUpdateApk == CTConstant.INSTALL_YES) {
                installApk();
            } else {
                if (mUpdateViewCallBack != null) {
                    mUpdateViewCallBack.showUpdateApk();
                }
            }
        } else {
            Logger.i(TAG, "apk文件不完整或不存在，删除重新下载");
            apkFile.delete();
            FileUtil.deleteApkFile(ApkUpdateParam.getDownloadPath(this.reqUrl) + "/" + InitParam.PROJECT_NAME);

            Logger.i(TAG, "download  " + appInfo.getUrl());
            OkHttpHelper.download(appInfo.getUrl(), ApkUpdateParam.getDownloadPath(this.reqUrl), InitParam.APK_NAME, "HomeActivity", this);
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onDownloadSuccess(File file) {
        if (file.getPath().equals(ApkUpdateParam.getDownloadPath(this.reqUrl) + "/" + InitParam.APK_NAME)) {
            String apkMD5 = Md5Manager.fileToMD5(file.getPath());
            if (apkMD5 != null && apkMD5.compareTo(appInfo.getSign()) == 0) {
                isDownloadApk = true;
                Logger.i(TAG, "apk文件下载成功，路径: " + file.getPath());

                //如果更新框点了yes则安装apk
                if (isPermitUpdateApk == CTConstant.INSTALL_YES) {
                    installApk();
                } else {
                    if (mUpdateViewCallBack != null) {
                        // 防止进入工厂模式，apk下载完成，切换服务器，返回Home界面，弹出切换前下载的apk。
                        if (!SpManager.getChangedServer()) {
                            mUpdateViewCallBack.showUpdateApk();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDownLoading(int progress, long lave) {
        if (lave == 0) {
            Logger.d(InitParam.APK_NAME + " 下载进度: %" + progress, "剩余: " + lave + " 字节");
            Logger.d(InitParam.APK_NAME + " 下载完成");
            return;
        }
        // 间隔3秒打印。
        long cur = System.currentTimeMillis();
        if (cur - time > 3000) {
            time = cur;
            Logger.e(InitParam.APK_NAME + " 下载进度: %" + progress, "剩余: " + lave + " 字节");
        }

        if (DownloadTimeUtils.canResponse()) {
            // 间隔1秒执行
            String topActivity = getTopActivity();
            if (!topActivity.contains("com.run.treadmill")) {
                // 此时点进了wifi界面，连接其它wifi时会停止下载
                isHasRequestNewApk = false;
                SpManager.setChangedServer(true);
            }
        }
    }

    private static String getTopActivity() {
        ActivityManager am = (ActivityManager) MyApplication.getContext().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }

    @Override
    public void onDownloadFailed(Exception e) {
        //Logger.d("下载失败" + e.getMessage());
    }

    public void installApk() {
        isPermitUpdateApk = CTConstant.INSTALL_YES;
        if (isDownloadApk) {
            SpManager.setInstallOpen(true);
            SpManager.setUpdateIsNetwork(true);
            new Thread(() -> ShellCmdUtils.getInstance().execCommand("pm install -r " + ApkUpdateParam.getDownloadPath(this.reqUrl) + "/" + InitParam.APK_NAME)).start();
            ShellCmdUtils.getInstance().execCommand("sync");
        }
    }

    public boolean isUpDateYes() {
        return (isPermitUpdateApk == CTConstant.INSTALL_YES);
    }

    public void obtainUpdate(Context context, UpdateViewCallBack updateViewCallBack) {
        mContext = context;
        mUpdateViewCallBack = updateViewCallBack;
        if (SpManager.getInstallOpen()) {
            SpManager.setInstallOpen(false);
            //关闭十字线
            new Thread() {
                @Override
                public void run() {
                    ShellCmdUtils.getInstance().execCommand("settings put system pointer_location 0");
                    SpManager.setDisplay(false);
                }
            }.start();
        }

        Logger.d(TAG, "isHasRequestNewApk == " + isHasRequestNewApk);
        Logger.d(TAG, "isAlterUpdatePath=" + isAlterUpdatePath + ",SpManager.getAlterUpdatePath " + SpManager.getAlterUpdatePath());

        if (isHasRequestNewApk) {
            if (isAlterUpdatePath || (!SpManager.getAlterUpdatePath())) {
                Logger.i(TAG, "obtainUpdate return");
                return;
            }
        }

        if (SpManager.getChangedServer()) {
            mThread.interrupt();
            mThread = null;
            Logger.d(TAG, "mThread = null;");

            isHasNetword = false;
            isAlterUpdatePath = false;
            SpManager.setChangedServer(false);
        }

        if (mThread == null) {
            mThread = new Thread(() -> {
                try {
                    Logger.d(TAG, "isHasNetword == " + isHasNetword);
                    while (!isHasNetword) {
                        isHasNetword = WifiBTStateManager.isNetworkConnected(mContext);
                        if (isHasNetword) {
                            this.reqUrl = ApkUpdateParam.getUpdateHost(mContext) + ApkUpdateParam.UPDATE_THIRD_A133_END + InitParam.PROJECT_NAME;
                            Logger.i(TAG, "请求的url：" + this.reqUrl);

                            OkHttpHelper.get(this.reqUrl, "HomeActivity", this);
                            isAlterUpdatePath = SpManager.getAlterUpdatePath();
                            isHasRequestNewApk = true;
                        }
                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            mThread.start();
        }
    }

    private static volatile HomeApkUpdateManager instance;

    private HomeApkUpdateManager() {
    }

    public static HomeApkUpdateManager getInstance() {
        if (instance == null) {
            synchronized (HomeApkUpdateManager.class) {
                if (instance == null) {
                    instance = new HomeApkUpdateManager();
                }
            }
        }
        return instance;
    }
}
