package com.run.treadmill.activity.runMode.help;

import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.runMode.MediaRunAppAdapter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.update.thirdapp.main.HomeAndRunAppUtils;
import com.run.treadmill.util.ActivityUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ResourceUtils;
import com.run.treadmill.util.ThirdApkSupport;

import java.util.ArrayList;
import java.util.List;

public class RunMedia {
    private BaseRunActivity activity;

    public RunMedia(BaseRunActivity baseRunActivity) {
        this.activity = baseRunActivity;
    }

    private MediaRunAppAdapter mMediaRunAppAdapter;
    /*** 媒体列表*/
    private PopupWindow mediaPopWin;
    /*** 媒体icon*/
    private List<Integer> iconList;
    private String[] pkgName;
    private String mediaPkgName = "";

    public void onCreate() {
        iconList = new ArrayList<>();
        pkgName = HomeAndRunAppUtils.getPkgNames();

        int[] drawable = HomeAndRunAppUtils.getRunDrawables();
        for (int id : drawable) {
            iconList.add(id);
        }
    }

    public void showMediaPopWin(@CTConstant.RunMode int runMode) {
        if (mMediaRunAppAdapter == null) {
            mMediaRunAppAdapter = new MediaRunAppAdapter(iconList);
            mMediaRunAppAdapter.setOnItemClick(position -> {
                BuzzerManager.getInstance().buzzerRingOnce();
                activity.isGoMedia = true;
                enterThirdApk(runMode, pkgName[position]);
                hideMediaPopWin();
                activity.rl_main.setVisibility(View.GONE);
            });
        }
        if (mediaPopWin == null) {
            View mediaView = activity.getLayoutInflater().inflate(R.layout.pop_window_media, null);
            RecyclerView rv_media = mediaView.findViewById(R.id.rv_media);
            rv_media.setLayoutManager(new GridLayoutManager(activity, 2));
            rv_media.setAdapter(mMediaRunAppAdapter);
            mediaPopWin = new PopupWindow(mediaView,
                    ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_300_x),
                    ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_715_y));
        }

        if (mediaPopWin.isShowing()) {
            hideMediaPopWin();
        } else if (!activity.mRunningParam.isStopStatus() && !activity.mRunningParam.isCoolDownStatus()) {
            mediaPopWin.showAtLocation(activity.btn_media,
                    Gravity.NO_GRAVITY,
                    (ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_0_x)),
                    ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_147_y));
            activity.btn_media.setSelected(true);
        }
    }

    public synchronized void enterThirdApk(@CTConstant.RunMode int runMode, String pkgName) {
        RunningParam mRunningParam = activity.mRunningParam;

        mRunningParam.isFloat = true;
        Logger.i("isFloat " + true);

        ControlManager.getInstance().setSendWaiteTime(70);
        mRunningParam.waiteTime = 955;
        mRunningParam.waiteNanosTime = 14000;
        Logger.d("enterThirdApk pkgName =" + pkgName);
        mediaPkgName = pkgName;
        mRunningParam.mediaPkgName = pkgName;
        if (pkgName.contains("com.facebook.katana")) {
            ThirdApkSupport.stopKillLoginAppTimer();
            activity.startActivity(MyApplication.getContext().getPackageManager().getLaunchIntentForPackage("com.facebook.katana"));
        } else {
            ThirdApkSupport.doStartApplicationWithPackageName(activity, pkgName);
        }
        activity.mFloatWindowManager.runningActivityStartMedia(runMode);
    }

    /**
     * 离开关闭第三方apk
     */
    public void shortDownThirtyApk() {
        SystemSoundManager.MusicPause(activity);
        ThirdApkSupport.killInputmethodPid(activity, "com.google.android.inputmethod.pinyin");
        Logger.d("shortDownThirtyApk pkgName =" + mediaPkgName);
        if (mediaPkgName.contains("com.facebook.katana")) {
            int facebookId = ThirdApkSupport.findPid(activity, "com.facebook.katana");
            Logger.d("kill facebookId = " + facebookId);
            ShellCmdUtils.getInstance().execCommand("kill " + facebookId);
            return;
        }
        if (!mediaPkgName.equals("")) {
            ThirdApkSupport.killCommonApp(activity, mediaPkgName);
        }
    }

    public void hideMediaPopWin() {
        if (mediaPopWin != null && mediaPopWin.isShowing()) {
            mediaPopWin.dismiss();
        }
        if (activity.isCalcDialogShowing()) {
            activity.mCalcBuilder.stopPopWin();
        }
        if (activity.btn_media != null) {
            activity.btn_media.setSelected(false);
        }
    }

    public void dismissPopWin() {
        if (mediaPopWin != null && mediaPopWin.isShowing()) {
            mediaPopWin.dismiss();
        }
    }

    public boolean isShowing() {
        return mediaPopWin != null && mediaPopWin.isShowing();
    }

    public void checkMediaBack() {
        //媒体的mp4（或者其他媒体） 自己退出回来
        if (!activity.quickToMedia && activity.rl_main.getVisibility() == View.GONE) {
            //关闭悬浮窗
            if (activity.mFloatWindowManager != null) {
                if (!ActivityUtils.isContainsMy()) {
                    Logger.d("!ActivityUtils.isContainsMy() 顶部不是主apk的界面  -> 不关闭悬浮窗");
                } else {
                    Logger.d("ActivityUtils.isContainsMy() 顶部是主apk的界面  -> 关闭悬浮窗");
                    activity.mFloatWindowManager.stopFloatWindow();
                }
                activity.isGoMedia = false;
            }
            activity.mRunningParam.setCallback(activity);
            activity.rl_main.setVisibility(View.VISIBLE);
        }
    }
}
