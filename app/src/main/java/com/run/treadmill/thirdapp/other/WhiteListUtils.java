package com.run.treadmill.thirdapp.other;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.util.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class WhiteListUtils {
    private final static String[] arr = {
            "com.run.treadmill",
            "com.run.dotars",
            "com.anplus.bluetooth",
            "top.cnzrg.otamcu",

            "com.anplus.tft",
            "com.hpplay.happyplay.aw",
            "com.google.android.youtube",
            "com.android.chrome",
            "com.twitter.android",
            "com.facebook.katana",
            "com.instagram.android",
            "com.spotify.music",
            "com.netflix.mediaclient",
            "com.softwinner.fireplayer",
            "com.amazon.avod.thirdpartyclient",
            "com.amazon.mShop.android.shopping",
            "com.android.music",
            "me.wcy.music",

            "org.mozilla.firefox",
            "com.sina.weibo",
            "com.qiyi.video.pad",
            "com.yahoo.mobile.client.android.flickr",
            "com.tencent.qqmusicpad",
            "com.baidu.searchbox.pad",

            "com.kinomap.training",
    };

    private static final String whiteListFilePath = "/data/WhiteListAppFilter.properties";

    public static void WhiteListAppFilter() {
        try {
            Logger.d("写入白名单列表 == " + Arrays.toString(arr));

            writePropertiesFile();
            setProp("persist.sys.whiteINSTALLAPP", "1");
        } catch (Exception e) {
            Logger.e("写入白名单异常");
            e.printStackTrace();
        }
    }

    private static void writePropertiesFile() throws IOException {
        ShellCmdUtils.getInstance().execCommand("chmod -R 777 " + "/data/");
        File debug = new File(whiteListFilePath);

        byte[] va;
        byte[] ee = "\r\n".getBytes();
        FileOutputStream fos = new FileOutputStream(debug);
        for (String s : arr) {
            va = s.getBytes();
            fos.write(va, 0, va.length);
            fos.write(ee, 0, ee.length);
        }
        fos.flush();
        fos.close();
    }

    private static String setProp(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("set", String.class, String.class);
            get.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }
}
