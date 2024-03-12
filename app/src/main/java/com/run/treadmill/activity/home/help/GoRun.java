package com.run.treadmill.activity.home.help;

import android.content.Intent;

import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.activity.runMode.quickStart.QuickStartActivity;

public class GoRun {
    public static void quickStart(HomeActivity activity) {
        activity.getPresenter().setUpRunningParam();
        activity.startActivity(new Intent(activity, QuickStartActivity.class));
    }
}
