package com.run.treadmill.activity.home.help;

import android.content.Intent;
import android.view.View;

import com.run.treadmill.R;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.activity.login.LoginActivity;
import com.run.treadmill.activity.setting.SettingActivity;
import com.run.treadmill.sysbt.BtAppUtils;
import com.run.treadmill.util.SystemWifiUtils;
import com.run.treadmill.util.WifiBackFloatWindowManager;

public class HomeClick extends BaseHomeHelp {

    public HomeClick(HomeActivity activity) {
        super(activity);
    }

    public void click(View view) {
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
                view.getId() == R.id.tv_home_quickstart
        ) {
            activity.isOnClicking = true;
        }

        switch (view.getId()) {
            case R.id.tv_home_quickstart:
                view.postDelayed(() -> {
                    GoRun.quickStart(activity);
                }, 200);
                break;
            case R.id.iv_float_close:
                activity.findViewById(R.id.inclue_float_left).setVisibility(View.GONE);
                activity.findViewById(R.id.inclue_float_left_2).setVisibility(View.VISIBLE);
                activity.voiceFW.hide();
                break;
            case R.id.iv_float_open:
                activity.findViewById(R.id.inclue_float_left).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.inclue_float_left_2).setVisibility(View.GONE);
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
                activity.startActivity(new Intent(activity, LoginActivity.class));
                break;

            case R.id.tv_home_setting:
                activity.startActivity(new Intent(activity, SettingActivity.class));
                break;
        }
    }
}
