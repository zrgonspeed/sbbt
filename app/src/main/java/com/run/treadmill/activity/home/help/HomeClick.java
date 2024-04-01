package com.run.treadmill.activity.home.help;

import android.content.Intent;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.run.treadmill.R;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.activity.home.help.media.HomeAppAdapter;
import com.run.treadmill.activity.login.LoginActivity;
import com.run.treadmill.activity.setting.SettingActivity;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.sysbt.BtAppUtils;
import com.run.treadmill.update.thirdapp.main.HomeAndRunAppUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.SystemWifiUtils;
import com.run.treadmill.util.WifiBackFloatWindowManager;
import com.run.treadmill.util.clicktime.HomeClickMedia;
import com.run.treadmill.util.clicktime.HomeClickUtils;

public class HomeClick extends BaseHomeHelp {

    public HomeClick(HomeActivity activity) {
        super(activity);
    }

    public void click(View view) {
        if (!HomeClickUtils.canResponse()) {
            Logger.i("不准点太快");
            return;
        }
        if (view.getId() == R.id.tv_home_media ||

                view.getId() == R.id.iv_media_x ||
                view.getId() == R.id.iv_media_app ||

                view.getId() == R.id.iv_media_app_x ||
                view.getId() == R.id.iv_media_app_back
        ) {
            if (!HomeClickMedia.canResponse()) {
                Logger.i("不准点太快Media");
                return;
            }
        }

        if (activity.getPresenter().inOnSleep) {
            activity.wakeUpSleep();
            return;
        }
        if (activity.isOnClicking) {
            return;
        }
        // 有些按钮要防止快速点击多次
        if (view.getId() == R.id.iv_bluetooth ||
                view.getId() == R.id.iv_wifi ||
                view.getId() == R.id.tv_home_quickstart ||
                view.getId() == R.id.tv_home_setting ||
                view.getId() == R.id.tv_home_media
        ) {
            activity.isOnClicking = true;
        }

        switch (view.getId()) {
            case R.id.tv_home_quickstart:
                view.postDelayed(() -> {
                    GoRun.quickStart(activity);
                }, 150);
                break;
            case R.id.iv_float_close:
                closeLeft();
                break;
            case R.id.iv_float_open:
                openLeft();
                break;

            case R.id.iv_bluetooth:
                BtAppUtils.enterBluetooth(activity);
                break;
            case R.id.iv_wifi:
                WifiBackFloatWindowManager.startWifiBackFloat();
                SystemWifiUtils.enterWifi();
                break;

            case R.id.iv_float_volume:
                activity.voiceFW.showOrHide();
                break;

            case R.id.tv_home_signin:
                view.postDelayed(() -> {
                    activity.startActivity(new Intent(activity, LoginActivity.class));
                }, 100);
                break;

            case R.id.tv_home_setting:
                view.postDelayed(() -> {
                    activity.startActivity(new Intent(activity, SettingActivity.class));
                }, 100);
                break;

            case R.id.tv_home_media:
                clickMedia();
                break;
            case R.id.iv_media_x:
                closeMedia();
                break;
            case R.id.iv_media_app:
                view.postDelayed(() -> {
                    openMediaAppList();
                    activity.isOnClicking = false;
                }, 100);
                break;
            case R.id.iv_media_app_back:
                backMediaAppList();
                break;
            case R.id.iv_media_app_x:
                closeMedia();
                break;
        }
    }

    private void backMediaAppList() {
        Logger.i("backMediaAppList()");
        activity.include_home_media.setVisibility(View.VISIBLE);
        activity.include_home_media_app.setVisibility(View.GONE);
    }

    private void openMediaAppList() {
        Logger.i("openMediaAppList()");
        activity.include_home_media_app.setVisibility(View.VISIBLE);
        activity.include_home_media.setVisibility(View.GONE);
        initAppList();
    }

    private void initAppList() {
        final String[] pkgName = HomeAndRunAppUtils.getPkgNames();
        int[] drawable = HomeAndRunAppUtils.getHomeDrawables();
        String[] apkNames = HomeAndRunAppUtils.getNames();

        RecyclerView rv_media_app = activity.findViewById(R.id.rv_media_app);

        GridLayoutManager glm1 = new GridLayoutManager(activity, 2);
        glm1.setOrientation(RecyclerView.HORIZONTAL);
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

        int[] drawable1 = new int[drawable.length];
        System.arraycopy(drawable, 0, drawable1, 0, drawable1.length);

        HomeAppAdapter adapter1 = new HomeAppAdapter(MyApplication.getContext(), drawable1);
        adapter1.setNames(apkNames);

        rv_media_app.setAdapter(adapter1);
        adapter1.setOnItemClick(position -> {
/*            if (!isCanStart && !AppDebug.debug) {
                return;
            }*/
            String mediaPkName = pkgName[position];
            Logger.i("点击了 " + mediaPkName);

            enterThirdApp(mediaPkName);
        });
    }

    private void enterThirdApp(String mediaPkName) {
        BuzzerManager.getInstance().buzzerRingOnce();
        GoRun.homeMediaToQuickStart(activity, mediaPkName);
    }

    private void clickMedia() {
        if (activity.include_home_media.getVisibility() == View.VISIBLE) {
            closeMedia();
            activity.isOnClicking = false;
            return;
        }

        activity.tv_home_media.postDelayed(() -> {
            activity.tv_home_media.setSelected(true);
            closeLeft();
            openMedia();
            activity.isOnClicking = false;
        }, 200);
    }

    private void openMedia() {
        Logger.i("openMedia()");
        activity.v_media_bg.setVisibility(View.VISIBLE);
        activity.include_home_media.setVisibility(View.VISIBLE);
    }

    private void closeMedia() {
        Logger.e("closeMedia");
        activity.v_media_bg.setVisibility(View.GONE);
        activity.include_home_media.setVisibility(View.GONE);
        activity.include_home_media_app.setVisibility(View.GONE);
        activity.tv_home_media.setSelected(false);
    }

    private void openLeft() {
        activity.findViewById(R.id.include_float_left).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.include_float_left_2).setVisibility(View.GONE);
    }

    private void closeLeft() {
        activity.findViewById(R.id.include_float_left).setVisibility(View.GONE);
        activity.findViewById(R.id.include_float_left_2).setVisibility(View.VISIBLE);
        activity.voiceFW.hide();
    }
}
