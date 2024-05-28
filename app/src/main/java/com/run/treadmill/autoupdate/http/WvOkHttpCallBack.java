package com.run.treadmill.autoupdate.http;

import java.io.IOException;

import okhttp3.Call;

public interface WvOkHttpCallBack {

    void onFailure(Call call, IOException e);

    void onSuccess(Call call, String response);
}