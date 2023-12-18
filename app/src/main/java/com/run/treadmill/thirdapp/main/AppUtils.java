package com.run.treadmill.thirdapp.main;

import com.run.treadmill.sp.StorageParam;

public class AppUtils {
    // sp存储状态
    public static boolean isHasTwo() {
        return getHasTwo();
    }

    private static final String SET_HAS_TWO = "set_has_two_1212";

    public static void setHasTwo(boolean value) {
        HomeAndRunAppUtils.changeLanguage = true;
        ThirdUpdateUtils.changeLanguage = true;
        StorageParam.setParam(SET_HAS_TWO, value);
    }

    private static boolean getHasTwo() {
        return StorageParam.getParam(SET_HAS_TWO, false);
    }
}
