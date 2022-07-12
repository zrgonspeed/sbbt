package com.run.treadmill.otamcu;

interface OtaMcuAidlInterface {
    String getProjectName();
    String getPlatform();
    String getPlatformPort();

    void stopSerial();
}

