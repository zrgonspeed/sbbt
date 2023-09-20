package com.run.treadmill.otamcu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.run.serial.SerialUtils;
import com.run.treadmill.common.InitParam;

public class OtaMcuService extends Service {
    public static final String PROJECT_NAME = "AC00551-55T-01";
    public static final String PLATFORM = "A133";
    public static final String PLATFORM_PORT = "/dev/ttyS2";

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    class MyBinder extends OtaMcuAidlInterface.Stub {
        @Override
        public String getProjectName() {
            return PROJECT_NAME;
        }

        @Override
        public String getPlatform() {
            return PLATFORM;
        }

        @Override
        public String getPlatformPort() {
            return PLATFORM_PORT;
        }

        @Override
        public void stopSerial() {
            SerialUtils.getInstance().stopSerial();
        }
    }
}
