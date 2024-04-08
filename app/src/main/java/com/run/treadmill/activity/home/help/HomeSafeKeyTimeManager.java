package com.run.treadmill.activity.home.help;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.mcu.control.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.mcu.ReBootTask;

public class HomeSafeKeyTimeManager {
    public static int getDelayTime() {
        // 当前速度或者最后下发的速度
        int speed = ErrorManager.getInstance().lastSpeed;
        int delayTime = 0;
        if (!ReBootTask.isReBootFinish) {
            return 3000;
        }
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            if (speed > 0 && speed < 8) {
                delayTime = 1000;
            } else if (speed >= 8 && 20 >= speed) {
                delayTime = 2 * 1000;
            } else if (speed >= 21 && 40 >= speed) {
                delayTime = 4 * 1000;
            } else if (speed >= 41 && 60 >= speed) {
                delayTime = 6 * 1000;
            } else if (speed >= 61 && 80 >= speed) {
                delayTime = 7 * 1000;
            } else if (speed >= 81 && 100 >= speed) {
                delayTime = 8 * 1000;
            } else if (speed >= 101 && 120 >= speed) {
                delayTime = 10 * 1000;
            } else if (speed >= 121 && 140 >= speed) {
                delayTime = 12 * 1000;
            } else if (speed >= 141 && 160 >= speed) {
                delayTime = 13 * 1000;
            } else if (speed >= 161 && 180 >= speed) {
                delayTime = 14 * 1000;
            } else if (speed >= 181 && 210 >= speed) {
                delayTime = 18 * 1000;
            } else if (speed >= 211 && 250 >= speed) {
                delayTime = 20 * 1000;
            }
        }

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC && speed != 0) {
            return delayTime + 3500;
        }
        return delayTime;
    }
}
