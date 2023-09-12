package com.run.treadmill.thirdapp.other;

import android.os.SystemClock;

import com.run.treadmill.base.MyApplication;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.NotificationBackend;
import com.run.treadmill.util.ThirdApkSupport;

import java.util.Arrays;

/**
 * 屏蔽第三方通知
 */
public class IgnoreSendMessageUtils {
    private final static String[] arr = {
            "com.google.android.youtube",
            "com.android.chrome",
            "com.twitter.android",
            "com.facebook.katana",
            "com.instagram.android",
            "com.spotify.music",
            "com.netflix.mediaclient",
            "com.android.music",
            "com.softwinner.fireplayer",
            "org.mozilla.firefox",
            "com.sina.weibo",
            "com.qiyi.video.pad",
            "com.yahoo.mobile.client.android.flickr",
            "com.kinomap.training",

            "com.keshet.mako.VOD.intl",
            "com.disney.disneyplus",
            "in.startv.hotstar.dplus.tv",
    };

    public static void onCreateMission() {
        new Thread(() -> {
            SystemClock.sleep(1000);
            String[] pkNames = arr;
            // Logger.i("屏蔽第三方app的通知 " + Arrays.toString(pkNames));
            for (String pkName : pkNames) {
                NotificationBackend.setNotificationsBanned(MyApplication.getContext(), pkName, false);
                ThirdApkSupport.killCommonApp(MyApplication.getContext(), pkName);
            }
        }).start();
    }
}
