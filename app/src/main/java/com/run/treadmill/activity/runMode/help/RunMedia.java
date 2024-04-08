package com.run.treadmill.activity.runMode.help;

import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.R;
import com.run.treadmill.activity.home.help.media.HomeAndRunAppAdapter;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.mcu.control.ControlManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.update.thirdapp.main.HomeAndRunAppUtils;
import com.run.treadmill.util.ActivityUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThirdApkSupport;
import com.run.treadmill.util.ToastUtils;

public class RunMedia {
    private String mediaPkgName = "";
    private RelativeLayout rl_run_media_application;
    private HomeAndRunAppAdapter appAdapter;
    private String[] pkgName;
    private int[] drawable;
    private String[] apkViewNames;

    public void onResume() {
        if (appAdapter != null) {
            appAdapter.refresh();
        }
    }

    public void clickMedia() {
        Logger.i("R.id.iv_run_media");
        activity.iv_run_media.setSelected(true);

        // 1.隐藏中间图表
        activity.runGraph.hide();

        // 2.加载和显示app列表
        initApp();

        // 3.显示布局
        showAppList();
    }

    private void showAppList() {
        if (rl_run_media_application.getVisibility() == View.GONE) {
            rl_run_media_application.setVisibility(View.VISIBLE);
        }
    }

    private void hideAppList() {
        if (rl_run_media_application.getVisibility() == View.VISIBLE) {
            rl_run_media_application.setVisibility(View.GONE);
        }
        activity.iv_run_media.setSelected(false);
    }

    private void initApp() {
        if (rl_run_media_application != null) {
            return;
        }
        Logger.i("rl_run_media_application == null  初始化媒体列表");
        rl_run_media_application = activity.findViewById(R.id.run_media_application);

        initCloseEvent();

        initAppAdapter();

        initRecyclerView();
    }

    private void initCloseEvent() {
        ImageView iv_media_app_x = activity.findViewById(R.id.iv_media_app_x);
        iv_media_app_x.setOnClickListener((v) -> {
            BuzzerManager.getInstance().buzzerRingOnce();
            hideAppList();
            activity.runGraph.show();
        });
    }

    private void initAppAdapter() {
        pkgName = HomeAndRunAppUtils.getPkgNames();
        drawable = HomeAndRunAppUtils.getHomeDrawables();
        apkViewNames = HomeAndRunAppUtils.getViewNames();

        appAdapter = new HomeAndRunAppAdapter(activity, drawable);
        appAdapter.setNames(apkViewNames);
        appAdapter.setOnItemClick(position -> {
            clickItemApp(position);
        });
    }

    private void initRecyclerView() {
        GridLayoutManager glm1 = new GridLayoutManager(activity, 2);
        glm1.setOrientation(RecyclerView.HORIZONTAL);

        RecyclerView rv_media_app = activity.findViewById(R.id.rv_media_app);
        rv_media_app.setLayoutManager(glm1);
        // 跟显示滑动条有关
        rv_media_app.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = 0;
                outRect.bottom = 0;
                outRect.top = 0;
                outRect.right = 0;
            }
        });
        rv_media_app.setAdapter(appAdapter);
    }

    private void clickItemApp(int position) {
        if (activity.isGoMedia) {
            Logger.i("isGoMedia true 不给再打开第三方");
            return;
        }
        BuzzerManager.getInstance().buzzerRingOnce();

        String mediaPkName = pkgName[position];
        String apkViewName = apkViewNames[position];
        Logger.i("run 点击了 " + apkViewName + "   " + mediaPkName);

        if (!ThirdApkSupport.checkApkExist(activity, mediaPkName)) {
            Logger.e("没有安装 " + mediaPkName);
            ToastUtils.showShort("Not installed");
            return;
        }
        activity.isGoMedia = true;

        activity.rl_main.setVisibility(View.GONE);
        enterThirdApp(CTConstant.QUICKSTART, pkgName[position]);
    }

    public synchronized void enterThirdApp(@CTConstant.RunMode int runMode, String pkgName) {
        RunningParam mRunningParam = activity.mRunningParam;
        mRunningParam.isFloat = true;
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

    private BaseRunActivity activity;

    public RunMedia(BaseRunActivity baseRunActivity) {
        this.activity = baseRunActivity;
    }
}
