package com.run.treadmill.autoupdate;


import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;
import com.run.android.ShellCmdUtils;
import com.run.treadmill.autoupdate.http.WvDownloadListener;
import com.run.treadmill.autoupdate.http.WvOkHttpCallBack;
import com.run.treadmill.autoupdate.http.WvOkHttpHelper;
import com.run.treadmill.autoupdate.util.WvAppBean;
import com.run.treadmill.autoupdate.util.WvCustomTimer;
import com.run.treadmill.autoupdate.util.WvFileUtils;
import com.run.treadmill.autoupdate.util.WvLogger;
import com.run.treadmill.autoupdate.util.WvMd5Manager;
import com.run.treadmill.autoupdate.util.WvNetworkUtils;
import com.run.treadmill.autoupdate.util.WvVersionUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;

public class WebViewUpdate implements WvCustomTimer.TimerCallBack {
    private static final String APK_URL = "http://apk.anplus-tech.com/restapi/apk/A133/update/treadmill?apkNames=";
    private static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/treadmill";

    private int timer_count = 2;
    private static long delay = 2 * 1000;
    private static long reCheckDelay = 5 * 1000;
    private static long period = 10 * 1000;

    private final String webView = "webview";
    private final String webView_apk = webView + ".apk";
    private final String webView_pkg = "com.android.webview";

    private Context mContext;

    private final String tag_http_get_version = "tag_http_get_version";
    private final String tag_http_download_file = "tag_http_download_file";
    private WvCustomTimer checkMainTimer;
    private final String tag_timer_check_main = "tag_timer_check_main";
    private String webView_url_dl;
    private String webView_url_md5;
    private int downLoad_webView = -1;

    public void startCheckUpdate(Context c) {
        mContext = c;
        if (checkMainTimer == null) {
            checkMainTimer = new WvCustomTimer();
            checkMainTimer.setTag(tag_timer_check_main);
            checkMainTimer.startTimer(
                    delay, period, this);
            return;
        }
        checkMainTimer.closeTimer();
        checkMainTimer.setTag(tag_timer_check_main);
        checkMainTimer.startTimer(
                delay, period, this);
    }

    public void reCheckUpdate() {
        if (mContext == null
                || checkMainTimer == null) {
            return;
        }
        WvLogger.d(TAG, "重新检测 reStart timer");
        checkMainTimer.closeTimer();
        checkMainTimer.setTag(tag_timer_check_main);
        checkMainTimer.startTimer(
                reCheckDelay, period, this);
    }

    @Override
    public void timerComply(long lastTime, String tag) {
        WvLogger.d(TAG, "lastTime == " + lastTime + "   timer_count == " + timer_count);
        if (tag.equals(tag_timer_check_main)
                && (lastTime % timer_count == 0)) {

            boolean hasNet = WvNetworkUtils.isOnline();
            if (hasNet) {
                WvLogger.d(TAG, "有网络.(" + timer_count + ")");
                checkMainTimer.closeTimer();
                WvOkHttpHelper.cancel(tag_http_get_version);
                WvOkHttpHelper.get(getUpdateUrl(),
                        tag_http_get_version, new MyHttpCallback());

            }
        }
    }

    private String getUpdateUrl() {
        StringBuilder reqUrl = new StringBuilder(APK_URL);
        reqUrl.append(webView);

        WvLogger.d(TAG, "getUpdateUrl = " + reqUrl);
        return reqUrl.toString();
    }

    private class MyHttpCallback implements WvOkHttpCallBack {

        @Override
        public void onFailure(Call call, IOException e) {
            WvLogger.d(TAG, "getUpdateUrl request fail.");
            reCheckUpdate();
        }

        @Override
        public void onSuccess(Call call, String response) {
            WvLogger.d(TAG, "getUpdateUrl onSuccess");
            Gson mGson = new Gson();
            List<WvAppBean.AppInfo> appInfoList;

            try {
                appInfoList = mGson.fromJson(response, WvAppBean.class).getApkInfos();
            } catch (Exception ignore) {
                return;
            }
            if (appInfoList == null || appInfoList.isEmpty()) {
                return;
            }
            check_webViewUpdate(appInfoList);
        }

        private void check_webViewUpdate(List<WvAppBean.AppInfo> appInfoList) {
            WvLogger.d(TAG, "check_webViewUpdate " + appInfoList);

            if (downLoad_webView != -1) {
                if (downLoad_webView == 3) {
                    // WvLogger.d(TAG, "WebView down load or install fail,try again!!");
                    webView_down(webView_url_dl, webView_url_md5);
                } else {
                    WvLogger.i(TAG, "不需要更新");
                }
                return;
            }
            WvAppBean.AppInfo appInfo = null;
            for (int i = 0; i < appInfoList.size(); i++) {
                appInfo = appInfoList.get(i);
                if (appInfo.getName().equals(webView)) {
                    break;
                }
            }
            if (appInfo == null) {
                return;
            }
            appInfo.checkUpdate(mContext, webView_pkg);
            String ver = WvVersionUtil.getSpecAppVersionName(mContext, webView_pkg);
            boolean update = appInfo.isUpdate == WvAppBean.UPDATE;
            WvLogger.d(TAG, "本地WebView(" + ver + ") 需要更新？ = " + update);
            final String path_1 = DOWNLOAD_PATH + "/" + webView_apk;
            final String path_2 = DOWNLOAD_PATH + "/" + webView;
            if (WvFileUtils.isCheckExist(path_1)) {
                WvFileUtils.deleteApkFile(path_1);
            }
            if (WvFileUtils.isCheckExist(path_2)) {
                WvFileUtils.deleteApkFile(path_2);
            }

            if (!update) {
                downLoad_webView = 0;
                return;
            }
            webView_url_dl = appInfo.getUrl();
            webView_url_md5 = appInfo.getSign();
            WvLogger.d(TAG, "WebView 需要更新，准备下载和安装");
            webView_down(webView_url_dl, webView_url_md5);
        }

        private void webView_down(final String url_dl, final String urlMD5) {
            WvLogger.d(TAG, "webView_down");

            downLoad_webView = 1;
            final String path_1 = DOWNLOAD_PATH + "/" + webView_apk;
            final String path_2 = DOWNLOAD_PATH + "/" + webView;

            if (WvFileUtils.isCheckExist(path_1)) {
                WvFileUtils.deleteApkFile(path_1);
            }
            if (WvFileUtils.isCheckExist(path_2)) {
                WvFileUtils.deleteApkFile(path_2);
            }
            WvOkHttpHelper.cancel(tag_http_download_file);
            WvOkHttpHelper.download(url_dl, DOWNLOAD_PATH,
                    webView_apk, tag_http_download_file, new WvDownloadListener() {

                        @Override
                        public void onDownloadSuccess(File file) {
                            new Thread(() -> {
                                String apkMD5 = WvMd5Manager.fileToMD5(path_1);
                                WvLogger.d(TAG, "WebView path = " + path_1);
                                WvLogger.d(TAG, "WebView MD5 local = " + apkMD5);
                                WvLogger.d(TAG, "WebView MD5 net   = " + urlMD5);

                                if (urlMD5.equals(apkMD5)) {
                                    ShellCmdUtils.getInstance().execCommand("pm install -r " + path_1);
                                    ShellCmdUtils.getInstance().execCommand("sync");
                                    String ver_webView_curr =
                                            WvVersionUtil.getSpecAppVersionName(mContext, "com.android.webview");
                                    WvLogger.d(TAG, "WebView 安装完成,current ver = " + ver_webView_curr);
                                    // Toaster.showLong("Webview installed");
                                    downLoad_webView = 2;
                                    return;
                                }
                                downLoad_webView = 3;
                            }).start();
                        }

                        private long time;

                        @Override
                        public void onDownLoading(int progress, long lave) {
                            if (lave == 0) {
                                WvLogger.d(webView + " 下载完成");
                                // Toaster.showLong("Installing webview");
                                return;
                            }

                            // 间隔3秒打印。
                            long cur = System.currentTimeMillis();
                            if (cur - time > 3000) {
                                time = cur;
                                WvLogger.d(webView + " 下载进度: %" + progress, "剩余: " + lave + " 字节");
                                // Toaster.showLong("Update webview " + progress + "%");
                            }
                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            downLoad_webView = 3;
                            WvLogger.d(TAG, "WebView 下载失败");
                            reCheckUpdate();
                        }
                    });
        }
    }

    private static final String TAG = WebViewUpdate.class.getSimpleName();
    private static volatile WebViewUpdate instance;

    private WebViewUpdate() {
    }

    public static WebViewUpdate getInstance() {
        if (instance == null) {
            synchronized (WebViewUpdate.class) {
                if (instance == null) {
                    instance = new WebViewUpdate();
                }
            }
        }
        return instance;
    }
}