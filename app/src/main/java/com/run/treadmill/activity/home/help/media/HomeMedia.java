package com.run.treadmill.activity.home.help.media;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.run.treadmill.R;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.activity.home.help.BaseHomeHelp;
import com.run.treadmill.activity.home.help.GoRun;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.update.thirdapp.main.HomeAndRunAppUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThirdApkSupport;
import com.run.treadmill.util.ToastUtils;
import com.run.treadmill.util.clicktime.HomeClickMediaUtils;
import com.run.treadmill.util.thread.DelayUtils;

public class HomeMedia extends BaseHomeHelp {

    private String[] pkgName;
    private String[] apkViewNames;
    private int[] drawable;
    private RecyclerView rv_media_app;
    private HomeAndRunAppAdapter appAdapter;

    public HomeMedia(HomeActivity activity) {
        super(activity);
    }

    // 刷新列表item变为可点
    public void onResume() {
        if (appAdapter != null) {
            appAdapter.refresh();
        }
    }

    public void clickMedia() {
        if (activity.include_home_media.getVisibility() == View.VISIBLE) {
            closeMedia();
            activity.isOnClicking = false;
            return;
        }

        DelayUtils.post(activity.tv_home_media, 200, () -> {
            activity.tv_home_media.setSelected(true);
            activity.closeLeft();
            openMedia();
            activity.isOnClicking = false;
        });
    }

    private void openMedia() {
        Logger.i("openMedia()");
        activity.v_media_bg.setVisibility(View.VISIBLE);
        activity.include_home_media.setVisibility(View.VISIBLE);

        View iv_media_x = activity.findViewById(R.id.iv_media_x);
        View iv_media_app = activity.findViewById(R.id.iv_media_app);
        View iv_media_app_back = activity.findViewById(R.id.iv_media_app_back);
        View iv_media_app_x = activity.findViewById(R.id.iv_media_app_x);

        iv_media_x.setOnClickListener(this::clickMediaInner);
        iv_media_app.setOnClickListener(this::clickMediaInner);
        iv_media_app_back.setOnClickListener(this::clickMediaInner);
        iv_media_app_x.setOnClickListener(this::clickMediaInner);
    }

    private void closeMedia() {
        Logger.e("closeMedia");
        activity.v_media_bg.setVisibility(View.GONE);
        activity.include_home_media.setVisibility(View.GONE);
        activity.include_home_media_app.setVisibility(View.GONE);
        activity.tv_home_media.setSelected(false);
    }

    private void openMediaAppList() {
        Logger.i("openMediaAppList()");
        activity.include_home_media_app.setVisibility(View.VISIBLE);
        activity.include_home_media.setVisibility(View.GONE);
        initAppList();
    }

    private void backMediaAppList() {
        Logger.i("backMediaAppList()");
        activity.include_home_media.setVisibility(View.VISIBLE);
        activity.include_home_media_app.setVisibility(View.GONE);
    }

    private void initAppList() {
        if (pkgName == null) {
            initAppAdapter();
            initRecyclerView();
        }
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

        rv_media_app = activity.findViewById(R.id.rv_media_app);
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
        if (activity.isOnPause) {
            return;
        }

        if (!HomeClickMediaUtils.canResponse()) {
            Logger.e("clickItemApp 不准点快");
            return;
        }

        BuzzerManager.getInstance().buzzerRingOnce();

        String mediaPkName = pkgName[position];
        String apkViewName = apkViewNames[position];
        Logger.i("home 点击了 " + apkViewName + "   " + mediaPkName);

        if (!ThirdApkSupport.checkApkExist(activity, mediaPkName)) {
            Logger.e("没有安装 " + mediaPkName);
            ToastUtils.showShort("Not installed");
            return;
        }

        enterThirdApp(mediaPkName);
    }

    private void enterThirdApp(String mediaPkName) {
        GoRun.homeMediaToQuickStart(activity, mediaPkName);
    }

    private void clickMediaInner(View view) {
        if (
                view.getId() == R.id.iv_media_x ||
                        view.getId() == R.id.iv_media_app ||

                        view.getId() == R.id.iv_media_app_x ||
                        view.getId() == R.id.iv_media_app_back
        ) {
            if (!HomeClickMediaUtils.canResponse()) {
                Logger.i("不准点太快Media里的View");
                return;
            }
        }

        switch (view.getId()) {
            case R.id.iv_media_x:
                closeMedia();
                break;
            case R.id.iv_media_app:
                DelayUtils.post(view, 100, () -> {
                    openMediaAppList();
                    activity.isOnClicking = false;
                });
                break;
            case R.id.iv_media_app_back:
                backMediaAppList();
                break;
            case R.id.iv_media_app_x:
                closeMedia();
                break;
        }
    }
}
