package com.run.treadmill.homeupdate.third;

import android.content.Context;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.homeupdate.main.ApkUpdateParam;
import com.run.treadmill.thirdapp.main.ThirdUpdateUtils;

import java.util.List;
import java.util.Map;

public class ThirdUpdateCheck {
    private static final String TAG = ThirdUpdateCheck.class.getSimpleName();

    private static StringBuilder reqUrl = null;
    private static Map<String, String> mAppPacknames = new ArrayMap<>();

    public static boolean hasUpdateResponse(String response) {
        List<HomeAppBean.AppInfo> appInfos = analyzeResponse(response);

        for (HomeAppBean.AppInfo info : appInfos) {
            if (info.isUpdate == HomeAppBean.UPDATE) {
                return true;
            }
        }

        return false;
    }

    private static List<HomeAppBean.AppInfo> analyzeResponse(String response) {
        Gson gson = new Gson();
        HomeAppBean appBean = gson.fromJson(response, HomeAppBean.class);

        List<HomeAppBean.AppInfo> apkInfos = appBean.getApkInfos();
        for (HomeAppBean.AppInfo app : apkInfos) {
            app.checkUpdate(MyApplication.getContext(), mAppPacknames.get(app.getName()));
        }

        return apkInfos;
    }

    public static String buildReqUrl() {
        Context context = MyApplication.getContext();
        reqUrl = new StringBuilder(ApkUpdateParam.getUpdateHost(context) + ApkUpdateParam.UPDATE_THIRD_A133_END);
        mAppPacknames = new ArrayMap<>();

        String[] apkNames = ThirdUpdateUtils.getNames();
        String[] apkPacknames = ThirdUpdateUtils.getPkgNames();

        // mAppPacknames.put(InitParam.PROJECT_NAME, context.getApplicationContext().getPackageName());

        for (int i = 0; i < apkNames.length; i++) {
            reqUrl.append(apkNames[i]).append(",");
            mAppPacknames.put(apkNames[i], apkPacknames[i]);
        }
        reqUrl.deleteCharAt(reqUrl.length() - 1);

        return reqUrl.toString();
    }
}
