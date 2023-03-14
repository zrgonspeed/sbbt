package com.run.treadmill.http;

import android.os.Handler;
import android.os.Looper;

import com.run.treadmill.util.Logger;

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

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/19
 */
public class OkHttpHelper {
    private static OkHttpClient okHttpClient;
    private static Handler mHandler;

    private static OkHttpClient getInstance() {
        if (okHttpClient == null) {
            synchronized (OkHttpHelper.class) {
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
    public static void get(String url, Object tag, OkHttpCallBack callBack) {
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
    public static void download(String url, String destFileDir, String fileName, Object tag, DownloadListener listener) {
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

    private static void commonGet(Request request, OkHttpCallBack callBack) {
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
                            Logger.i("====onFailure======= " + request.url().toString() + "    " + e);
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
                            int code = response.code();
                            if (code != 200) {
                                mHandler.post(() -> {
                                    callBack.onFailure(call, new IOException());
                                });
                            } else {
                                String body = response.body().string();
                                mHandler.post(() -> {
                                    callBack.onSuccess(call, body);
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            callBack.onFailure(call, new IOException());
                        }
                    }
                });
    }

    private static Request getRequestForGet(String url, Object tag) {
        if (url.isEmpty()) {
            Logger.e("OkHttpHelper-----getRequestForGet---> url 地址为空！！！");
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

    private static void commonDownload(String url, String destFileDir, String fileName, Object tag, DownloadListener listener) {
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
