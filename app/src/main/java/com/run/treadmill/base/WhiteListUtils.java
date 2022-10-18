package com.run.treadmill.base;

import android.content.Context;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.R;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

public class WhiteListUtils {

    public static String setprop(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("set", String.class, String.class );
            get.invoke(c, key, value );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static void WhiteListAppFilter(Context context) {
        try {

            String[] apkNames = context.getResources().getStringArray(R.array.WhiteListAppFilter);
            String path = "/data/";
            ShellCmdUtils.getInstance().execCommand("chmod -R 777 " + path);
            path = "/data/WhiteListAppFilter.properties";
            File debug = new File(path);

            byte[] va;
            byte[] ee = "\r\n".getBytes();
            FileOutputStream fos = new FileOutputStream(debug);
            for (String s : apkNames) {
                va = s.getBytes();
                fos.write(va, 0, va.length);
                fos.write(ee, 0, ee.length);
            }
            fos.flush();
            fos.close();

            setprop("persist.sys.whiteINSTALLAPP", "1");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
