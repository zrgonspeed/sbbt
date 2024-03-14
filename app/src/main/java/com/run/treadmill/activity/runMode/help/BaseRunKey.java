package com.run.treadmill.activity.runMode.help;

import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.runMode.BaseRunPresenter;
import com.run.treadmill.serial.SerialKeyValue;

public class BaseRunKey {
    private BaseRunActivity activity;

    public BaseRunKey(BaseRunActivity baseRunActivity) {
        this.activity = baseRunActivity;
    }

    public BaseRunPresenter getPresenter() {
        return (BaseRunPresenter) activity.getPresenter();
    }

    public void cmdKeyValue(int keyValue) {
        if (activity.mRunningParam.isPrepare() || activity.mRunningParam.isContinue()) {
            return;
        }
        if (!activity.mRunningParam.isRunning()
                && keyValue != SerialKeyValue.START_CLICK
                && keyValue != SerialKeyValue.STOP_CLICK
                && keyValue != SerialKeyValue.HAND_START_CLICK
                && keyValue != SerialKeyValue.HAND_STOP_CLICK
        ) {
            // 不在运动状态的时候，只能接收Start和Stop按键
            return;
        }
        if (activity.isGoMedia) {
            return;
        }

        activity.runCmdKeyValue(keyValue);
    }
}
