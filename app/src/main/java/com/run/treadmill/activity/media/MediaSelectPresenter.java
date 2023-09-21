package com.run.treadmill.activity.media;

import android.content.Context;
import android.content.res.TypedArray;

import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.manager.SpManager;

import java.util.Arrays;

public class MediaSelectPresenter extends BasePresenter<MediaSelectView> {

    /**
     * 获取第三方的包名,启动类名的称字符串数组
     *
     * @param context
     * @param arrayId
     * @return
     */
    public String[] getThirdApk(Context context, int arrayId) {
        return context.getResources().getStringArray(arrayId);
    }

    public int[] getThirdApkDrawable(Context context, int arrayId) {
        TypedArray ar = context.getResources().obtainTypedArray(arrayId);
        int len = ar.length();
        int[] resIds = new int[len];
        for (int i = 0; i < len; i++) {
            resIds[i] = ar.getResourceId(i, 0);
        }
        return resIds;
    }

    public void setUpRunningParam(boolean isMetric) {
        RunningParam.reset();
        Arrays.fill(RunningParam.getInstance().mInclineArray, 5.0f);
        Arrays.fill(RunningParam.getInstance().mSpeedArray, SpManager.getMinSpeed(isMetric));
    }
}
