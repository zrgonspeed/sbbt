/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.run.treadmill.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.lang.reflect.Method;

public class NotificationBackend {
    private static final String TAG = "NotificationBackend";

    public static boolean setNotificationsBanned(Context mContext, String pkg, boolean banned) {
        try {

            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, PackageManager.GET_ACTIVITIES);

            try {
                AppOpsManager mAppOps = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
                Method forceStopPackage = mAppOps.getClass().getDeclaredMethod("setMode", int.class, int.class, String.class, int.class);
                forceStopPackage.setAccessible(true);
                forceStopPackage.invoke(mAppOps, 11, ai.uid, pkg, AppOpsManager.MODE_IGNORED);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan " + e.getMessage());
            return false;
        }
    }
}
