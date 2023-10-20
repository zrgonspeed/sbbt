package com.run.treadmill.manager.fitshow.other;

import android.os.SystemClock;

import com.fitShow.ConvertData;
import com.fitShow.treadmill.FitShowCommand;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.manager.fitshow.FsRead;
import com.run.treadmill.manager.fitshow.FsSend;
import com.run.treadmill.util.Logger;

public class FsThreadManager {
    private static Thread sendThread;
    private static boolean isSend = false;

    private static Thread readThread;
    private static boolean isRead = false;

    public static void startTxDataThread() {
        if (sendThread != null) {
            try {
                sendThread.interrupt();
                sendThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isSend = true;
        sendThread = new Thread(new SendRunnable());
        sendThread.start();
    }

    private void stopTxThread() {
        try {
            isSend = false;
            if (sendThread != null) {
                sendThread.interrupt();
            }
            sendThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startRxDataThread() {
        if (readThread != null) {
            return;
        }
        isRead = true;
        readThread = new Thread(new ReadRunnable());
        readThread.start();
    }

    private void stopRxThread() {
        try {
            isRead = false;
            if (readThread != null) {
                readThread.interrupt();
            }
            readThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSendData = false;

    private static class SendRunnable implements Runnable {
        @Override
        public void run() {
            try {
                while (isSend) {
                    if (isSendData) {
                        isSendData = false;
                        if (FsSend.txSize >= 4) {
                            FitShowManager.getInstance().fsSerialUtils.sendData(FsSend.txData, FsSend.txSize);
                        }

                    }
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.d("FsTreadmill SendRunnable stop ");
            }
        }
    }

    private static class ReadRunnable implements Runnable {
        @Override
        public void run() {
            try {
                while (isRead) {
                    byte[] result = new byte[FitShowCommand.PKG_LEN * 2];
                    int len = FitShowManager.getInstance().fsSerialUtils.readData(result);
                    if (len > 0) {
                        Logger.d("FsTreadmill Read", ConvertData.byteArrayToHexString(result, len));
                        FsRead.parseData(result, len);
                    }
                    SystemClock.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
