package com.run.treadmill.thirdapp.other;

import android.os.Build;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.base.MyApplication;
import com.run.treadmill.manager.fslight.FsLight;
import com.run.treadmill.manager.musiclight.MusicLight;
import com.run.treadmill.manager.zyftms.ZyLight;
import com.run.treadmill.util.ThirdApkSupport;

/**
 * 删除第三方APP的账户数据
 */
public class DeleteAccountsUtils {
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
            "com.anplus.tft",
            "org.mozilla.firefox",
            "com.sina.weibo",
            "com.qiyi.video.pad",
            "com.yahoo.mobile.client.android.flickr",
            "com.tencent.qqmusicpad",
            "com.hpplay.happyplay.aw",
            "com.kinomap.training",

            "com.keshet.mako.VOD.intl",
            "com.disney.disneyplus",
            "in.startv.hotstar.dplus.tv",

            "com.amazon.avod.thirdpartyclient",
    };

    public static void delete() {
        new Thread(() -> {
            try {
                // android9以后 的账户数据路径不同
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ShellCmdUtils.getInstance().execCommand("rm -rf /data/system_de/0/*");
                } else {
                    ShellCmdUtils.getInstance().execCommand("rm -rf /data/system/users/0/accounts.db");
                    ShellCmdUtils.getInstance().execCommand("rm -rf /data/system/users/0/accounts.db-journal");
                }
                String[] pkNames = arr;
                for (String pkName : pkNames) {
                    ThirdApkSupport.killCommonApp(MyApplication.getContext(), pkName);
                }
                for (String pkName : pkNames) {
                    if (pkName.contains("youtube")) {
                        ShellCmdUtils.getInstance().execCommand("rm -rf /data/data/" + pkName + "/databases/*");
                    } else {
                        ShellCmdUtils.getInstance().execCommand("rm -rf /data/data/" + pkName + "/*");
                    }
                }

                MusicLight.deleteAccountCloseLight();
                ZyLight.deleteAccountCloseLight();
                FsLight.deleteAccountCloseLight();
                Thread.sleep(3000);
                ShellCmdUtils.getInstance().execCommand("reboot");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
