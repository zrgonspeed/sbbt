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
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.update.thirdapp.main.HomeAndRunAppUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.clicktime.HomeClickMedia;

public class HomeMedia extends BaseHomeHelp {
    public HomeMedia(HomeActivity activity) {
        super(activity);
    }

    public void clickMedia() {
        if (activity.include_home_media.getVisibility() == View.VISIBLE) {
            closeMedia();
            activity.isOnClicking = false;
            return;
        }

        activity.tv_home_media.postDelayed(() -> {
            activity.tv_home_media.setSelected(true);
            activity.closeLeft();
            openMedia();
            activity.isOnClicking = false;
        }, 200);
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
        String[] apkViewNames = HomeAndRunAppUtils.getViewNames();

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
        adapter1.setNames(apkViewNames);

        rv_media_app.setAdapter(adapter1);
        adapter1.setOnItemClick(position -> {
/*            if (!isCanStart && !AppDebug.debug) {
                return;
            }*/
            String mediaPkName = pkgName[position];
            Logger.i("点击了 " + apkViewNames[position] + "   " + mediaPkName);

            // enterThirdApp(mediaPkName);
        });
    }

    private void enterThirdApp(String mediaPkName) {
        BuzzerManager.getInstance().buzzerRingOnce();
        GoRun.homeMediaToQuickStart(activity, mediaPkName);
    }

    private void clickMediaInner(View view) {
        if (
                view.getId() == R.id.iv_media_x ||
                        view.getId() == R.id.iv_media_app ||

                        view.getId() == R.id.iv_media_app_x ||
                        view.getId() == R.id.iv_media_app_back
        ) {
            if (!HomeClickMedia.canResponse()) {
                Logger.i("不准点太快Media里的View");
                return;
            }
        }

        switch (view.getId()) {
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
}
