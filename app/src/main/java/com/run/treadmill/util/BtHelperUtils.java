package com.run.treadmill.util;

import android.content.Context;

import com.chuhui.btcontrol.BtHelper;
import com.chuhui.btcontrol.bean.InitialBean;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.manager.zyftms.ZyLight;


public class BtHelperUtils {
    public static void initBtHelper(Context context) {
        BtHelper.getInstance().openPort(context, "/dev/ttyS3");
        BtHelper.getInstance().setInitData(initAndGetBean());

        ZyLight.startThread();
    }

    public static InitialBean initAndGetBean() {
        InitialBean bean = new InitialBean();
        bean.setMachineType(BtHelper.MACHINE_TYPE_TREADMILL);
        bean.setRangeIncline(0, InitParam.DEFAULT_MAX_INCLINE, 0.5f);
        bean.setRangeSpeed(0.5f, 20f, 0.1f);
        bean.totalHours = (int) (SpManager.getRunTotalTime() / 3600);
        bean.totalDistance = (int) SpManager.getRunTotalDisByMetric(true);
        return bean;
    }

    public static void onRequestConnect() {
        InitialBean bean = BtHelper.getInstance().getInitBean();
        if (bean != null) {
            bean.totalHours = (int) (SpManager.getRunTotalTime() / 3600);
            bean.totalDistance = (int) SpManager.getRunTotalDisByMetric(true);
            // bean.setErrCodes(ErrorManager.getInstance().getErrCodes(BtHelper.errLogCount, String.valueOf(SpManager.getLastBtErrLogId())), SpManager.getMachineType());
        }
    }
}
