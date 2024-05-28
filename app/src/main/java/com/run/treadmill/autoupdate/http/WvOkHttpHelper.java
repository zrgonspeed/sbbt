package com.run.treadmill.autoupdate.http;

import android.os.Handler;
import android.os.Looper;

import com.run.treadmill.autoupdate.util.WvLogger;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WvOkHttpHelper {
    private static OkHttpClient okHttpClient;
    private static Handler mHandler;

    private static OkHttpClient getInstance() {
        if (okHttpClient == null) {
            synchronized (WvOkHttpHelper.class) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .build();
                    mHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return okHttpClient;
    }

    /**
     * get 请求
     *
     * @param url
     * @param tag
     * @param callBack
     */
    public static void get(String url, Object tag, WvOkHttpCallBack callBack) {
        commonGet(getRequestForGet(url, tag), callBack);
    }

    /**
     * 下载文件
     *
     * @param url
     * @param destFileDir
     * @param tag
     * @param listener
     */
    public static void download(String url, String destFileDir, String fileName, Object tag, WvDownloadListener listener) {
        commonDownload(url, destFileDir, fileName, tag, listener);
    }

    /**
     * 取消某个tag的网络请求
     *
     * @param tag
     */
    public static void cancel(Object tag) {
        if (tag == null) {
            return;
        }
        for (Call call : getInstance().dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : getInstance().dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /**
     * 判断tag是否存在
     *
     * @param tag
     */
    public static boolean isTag(Object tag) {
        if (tag == null) {
            return false;
        }
        for (Call call : getInstance().dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                return true;
            }
        }
        for (Call call : getInstance().dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                return true;
            }
        }
        return false;
    }

    private static void commonGet(Request request, WvOkHttpCallBack callBack) {
        if (request == null) {
            return;
        }
        getInstance()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        if (call.isCanceled()) {
                            return;
                        }
                        mHandler.post(() -> {
                            WvLogger.i("====onFailure======= " + request.url() + "    " + e);
                            if (callBack != null) {
                                callBack.onFailure(call, e);
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        if (call.isCanceled()) {
                            return;
                        }

                        try {
                            String string = response.body().string();
                            mHandler.post(() -> {
                                if (response.code() != 200) {
                                    callBack.onFailure(call, new IOException());
                                } else {
                                    callBack.onSuccess(call, string);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            mHandler.post(() -> {
                                callBack.onFailure(call, new IOException());
                            });
                        }
                    }
                });
    }

    private static Request getRequestForGet(String url, Object tag) {
        if (url.isEmpty()) {
            WvLogger.e("WvOkHttpHelper-----getRequestForGet---> url 地址为空！！！");
            return null;
        }
        Request request;
        if (tag != null) {
            request = new Request.Builder()
                    .url(url)
                    .get()
                    .tag(tag)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
        }
        return request;
    }

    private static void commonDownload(String url, String destFileDir, String fileName, Object tag, WvDownloadListener listener) {
        Request request = new Request.Builder()
                .url(url)
                .tag(tag)
                .build();
        getInstance()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        if (call.isCanceled()) {
                            return;
                        }
                        mHandler.post(() -> listener.onDownloadFailed(e));
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.code() != 200) {
                            mHandler.post(() -> listener.onDownloadFailed(new Exception(String.valueOf(response.code()))));
                            return;
                        }

                        InputStream is = null;
                        FileOutputStream fos = null;
                        byte[] buf = new byte[1024 * 4];
                        int len;

                        File dir = new File(destFileDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File file = new File(dir, fileName);
                        try {
                            is = response.body().byteStream();
                            long total = response.body().contentLength();
                            fos = new FileOutputStream(file);
                            long sum = 0L;
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                sum += len;
                                listener.onDownLoading(Math.round(sum * 1.0f / total * 100f), total - sum);
                            }
                            fos.flush();
                            if (call.isCanceled()) {
                                return;
                            }
                            mHandler.post(() -> listener.onDownloadSuccess(file));
                        } catch (Exception e) {
                            if (call.isCanceled()) {
                                return;
                            }
                            mHandler.post(() -> listener.onDownloadFailed(e));
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();

                            }
                        }
                    }
                });
    }
}
