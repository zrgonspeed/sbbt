package com.run.treadmill.activity.appStore;

import android.content.Context;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.run.treadmill.R;
import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.homeupdate.main.ApkUpdateParam;
import com.run.treadmill.http.OkHttpCallBack;
import com.run.treadmill.http.OkHttpHelper;
import com.run.treadmill.thirdapp.main.ThirdUpdateUtils;
import com.run.treadmill.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.Call;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/17
 */
public class AppStorePresenter extends BasePresenter<AppStoreView> implements OkHttpCallBack {
    private Map<String, String> mAppPacknames = new ArrayMap<>();
    private Map<String, Integer> mAppImgs = new ArrayMap<>();

    private Context mContext;

    void setContext(Context context) {
        this.mContext = context;
    }

    public StringBuilder reqUrl = null;
    public String downloadPath = null;

    void getAppList(Context context) {
        this.reqUrl = new StringBuilder(ApkUpdateParam.getUpdateHost(mContext) + ApkUpdateParam.UPDATE_THIRD_A133_END);
        this.downloadPath = ApkUpdateParam.getDownloadPath(reqUrl.toString());

        //TODO:名字可能没写好
        String[] apkNames = ThirdUpdateUtils.getNames();
        String[] apkPacknames = ThirdUpdateUtils.getPkgNames();
        int[] drawables = ThirdUpdateUtils.getUpdateDrawables();

        // Logger.i("apkNames == " + Arrays.toString(apkNames));
        if (apkNames.length <= 0 || apkPacknames.length <= 0) {
            Logger.e("apk名字或者包名为空！");
            getView().showFailure();
            return;
        }

        //组装本身apk
        reqUrl.append(InitParam.PROJECT_NAME).append(",");
        mAppPacknames.put(InitParam.PROJECT_NAME, context.getApplicationContext().getPackageName());
        mAppImgs.put(InitParam.PROJECT_NAME, R.drawable.btn_app_treadmill_1);

        for (int i = 0; i < apkNames.length; i++) {
            reqUrl.append(apkNames[i]).append(",");
            mAppPacknames.put(apkNames[i], apkPacknames[i]);
            mAppImgs.put(apkNames[i], drawables[i]);
        }
        reqUrl.deleteCharAt(reqUrl.length() - 1);

        Logger.d("请求的url：" + reqUrl.toString());
        OkHttpHelper.get(reqUrl.toString(), "AppStoreActivity", this);
        getView().showLoading();
    }

    /**
     * 删除apk文件（需要开线程？）
     *
     * @param path 文件路径
     */
    void deleteApkFile(String path) {
        if (!path.isEmpty()) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Logger.d("==== onFailure ====" + e);
        getView().showFailure();
    }

    @Override
    public void onSuccess(Call call, String response) {
        Logger.d("==== onSuccess ====" + response);
        getView().hideLoading();
        Gson gson = new Gson();
        AppBean appBean = gson.fromJson(response, AppBean.class);
        if (appBean == null) {
            getView().showFailure();
            return;
        }
        getView().initList(appBean.getApkInfos());
    }

    Map<String, String> getAppPacknames() {
        return mAppPacknames;
    }

    Map<String, Integer> getAppImgs() {
        return mAppImgs;
    }
}
