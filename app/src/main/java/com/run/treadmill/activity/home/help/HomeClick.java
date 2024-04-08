package com.run.treadmill.activity.home.help;

import android.content.Intent;
import android.view.View;

import com.run.treadmill.R;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.activity.login.LoginActivity;
import com.run.treadmill.activity.setting.SettingActivity;
import com.run.treadmill.sysbt.BtAppUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.SystemWifiUtils;
import com.run.treadmill.util.clicktime.HomeClickMediaUtils;
import com.run.treadmill.util.clicktime.HomeClickUtils;
import com.run.treadmill.util.thread.DelayUtils;
import com.run.treadmill.widget.floatWindow.WifiBackFloatWindowManager;

public class HomeClick extends BaseHomeHelp {

    public HomeClick(HomeActivity activity) {
        super(activity);
    }

    public void click(View view) {
        if (!HomeClickUtils.canResponse()) {
            Logger.i("不准点太快");
            return;
        }
        // 再加一层防止快点
        if (view.getId() == R.id.tv_home_media) {
            if (!HomeClickMediaUtils.canResponse()) {
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
                DelayUtils.post(view, 150, () -> {
                    GoRun.quickStart(activity);
                });
                break;
            case R.id.iv_float_close:
                activity.closeLeft();
                break;
            case R.id.iv_float_open:
                activity.openLeft();
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
                DelayUtils.post(view, 100, () -> {
                    activity.startActivity(new Intent(activity, LoginActivity.class));
                });
                break;

            case R.id.tv_home_setting:
                /*DelayUtils.post(view, 100, () -> {
                    activity.startActivity(new Intent(activity, SettingActivity.class));
                });*/
                break;

            case R.id.tv_home_media:
                activity.homeMedia.clickMedia();
                break;
        }
    }
}
