package com.run.treadmill.util;

import android.app.backup.BackupManager;
import android.content.res.Configuration;

import java.lang.reflect.Method;
import java.util.Locale;


public class LanguageUtil {
    private LanguageUtil() {
        throw new UnsupportedOperationException("休想实例化我！！！");
    }

    public static synchronized void changeSystemLanguage60(final Locale locale) {
        try {
            if (locale != null) {
                Class classActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
                Method getDefault = classActivityManagerNative.getDeclaredMethod("getDefault");
                Object objIActivityManager = getDefault.invoke(classActivityManagerNative);
                Class classIActivityManager = Class.forName("android.app.IActivityManager");
                Method getConfiguration = classIActivityManager.getDeclaredMethod("getConfiguration");
                Configuration config = (Configuration) getConfiguration.invoke(objIActivityManager);
                config.setLocale(locale);
                Class clzConfig = Class.forName("android.content.res.Configuration");
                java.lang.reflect.Field userSetLocale = clzConfig.getField("userSetLocale");
                userSetLocale.set(config, true);
                Class[] clzParams = {Configuration.class};
                Method updateConfiguration = classIActivityManager.getDeclaredMethod("updateConfiguration", clzParams);
                updateConfiguration.invoke(objIActivityManager, config);
                BackupManager.dataChanged("com.android.providers.settings");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}