package com.run.treadmill.activity.home.help;

import android.view.View;

import com.run.treadmill.Custom;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.util.thread.ThreadUtils;

public class HomeLoadAnim extends BaseHomeHelp {
    public HomeLoadAnim(HomeActivity activity) {
        super(activity);
    }

    public void onInitLoading() {
        // https://gitcode.com/baitutang1221/AVLoadingIndicatorView/overview?utm_source=csdn_github_accelerator&isLogin=1

        activity.avv_load.setVisibility(View.VISIBLE);
        activity.v_loading.setVisibility(View.VISIBLE);
        activity.avv_load.show();
        ThreadUtils.postOnMainThread(() -> {
            closeLoading();
            ThreadUtils.postOnMainThread(() -> {
                activity.homeBgAnimation.initAndStart();
            }, 5000);
        }, Custom.HomeLoadAnimTime * 1000);
    }

    private void closeLoading() {
        activity.v_loading.setVisibility(View.GONE);
        activity.avv_load.hide();
    }
}
