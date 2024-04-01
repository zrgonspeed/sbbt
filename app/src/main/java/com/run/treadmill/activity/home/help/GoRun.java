package com.run.treadmill.activity.home.help;

import android.content.Intent;

import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.activity.runMode.quickStart.QuickStartActivity;
import com.run.treadmill.common.CTConstant;

public class GoRun {
    public static void quickStart(HomeActivity activity) {
        activity.getPresenter().setUpRunningParam();
        activity.startActivity(new Intent(activity, QuickStartActivity.class));
    }

    //进入QuickStart模式再进入第三方媒体
    public static void homeMediaToQuickStart(HomeActivity activity, String mediaPkName) {
        activity.getPresenter().setUpRunningParam();
        Intent intent = new Intent(activity, QuickStartActivity.class);
        intent.putExtra(CTConstant.IS_MEDIA, true);
        intent.putExtra(CTConstant.PK_NAME, mediaPkName);
        activity.startActivity(intent);
    }
}
