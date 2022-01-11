package com.run.treadmill.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.List;
import java.util.Locale;

/**
 * @Description 参考 https://github.com/Blankj/AndroidUtilCode/blob/master/utilcode/lib/src/main/java/com/blankj/utilcode/util/LanguageUtils.java
 * @Author GaleLiu
 * @Time 2019/07/15
 */
public class LanguageUtil {
    private LanguageUtil() {
        throw new UnsupportedOperationException("休想实例化我！！！");
    }

    public static void applyLanguage(Context context, @NonNull final Locale locale, final Class<? extends Activity> activity) {
        applyLanguage(context, locale, activity, false);
    }

    private static void applyLanguage(Context context, Locale locale, Class<? extends Activity> activity, boolean isFollowSystem) {
        if (activity == null) {
            applyLanguage(context, locale, "", isFollowSystem);
            return;
        }
        applyLanguage(context, locale, activity.getName(), isFollowSystem);
    }

    private static void applyLanguage(Context context, Locale locale, String activityClassName, boolean isFollowSystem) {
        if (isFollowSystem) {

        } else {
            String language = locale.getLanguage();
            String country = locale.getCountry();

        }

        updateLanguage(context, locale);

        Intent intent = new Intent();
        String realActivityClassName
                = TextUtils.isEmpty(activityClassName) ? getLauncherActivity(context) : activityClassName;
        intent.setComponent(new ComponentName(context, realActivityClassName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.startActivity(intent);
    }

    private static void updateLanguage(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        Locale contextLocale = config.locale;
        if (equals(contextLocale.getLanguage(), locale.getLanguage()) && equals(contextLocale.getCountry(), locale.getCountry())) {
            return;
        }
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            context.createConfigurationContext(config);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, dm);
    }

    private static boolean equals(final CharSequence s1, final CharSequence s2) {
        if (s1 == s2) return true;
        int length;
        if (s1 != null && s2 != null && (length = s1.length()) == s2.length()) {
            if (s1 instanceof String && s2 instanceof String) {
                return s1.equals(s2);
            } else {
                for (int i = 0; i < length; i++) {
                    if (s1.charAt(i) != s2.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }

    private static String getLauncherActivity(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(context.getPackageName());
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        ResolveInfo next = info.iterator().next();
        if (next != null) {
            return next.activityInfo.name;
        }
        return "no launcher activity";
    }
}