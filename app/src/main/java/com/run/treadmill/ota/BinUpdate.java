package com.run.treadmill.ota;

import android.app.Activity;
import android.app.ProgressDialog;

import com.run.android.ShellCmdUtils;
import com.run.serial.OTAParam;
import com.run.serial.SerialUtils;
import com.run.treadmill.R;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

public class BinUpdate extends BaseUpdate {

    private Activity activity;

    public void procBin(Activity act, String path) {
        Logger.d("========data======1===");
        if (OTAParam.isSendBinCnt) {
            return;
        }
        activity = act;
        OTAParam.isSendBinCnt = true;
        SerialUtils.getInstance().reMoveAllReSendPackage();

        // 0x6D  0x69
        ControlManager.getInstance().sendUpdateCmd();
        //SerialUtils.getInstance().sendOtaConnectPackage();

        byte[] data = readStream(path);
        Logger.d("========data=========" + data.length);

        new Thread(new Runnable() {
            int index = 0;
            boolean isEndSend = false;

            final int oneFrameLen = 256;

            @Override
            public void run() {
                while (!isEndSend && !OTAParam.isSendBinData) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Logger.d("========data======2===");

                while (!isEndSend) {
                    try {
                        if (OTAParam.reSend) {
                            OTAParam.isSendBinOneFrame = true;
                            index = OTAParam.index;
                        }

                        if (OTAParam.isSendBinData && OTAParam.isSendBinOneFrame) {
                            int readLen = oneFrameLen;
                            byte[] pkgBytes;
                            pkgBytes = new byte[readLen];

                            for (int i = 0; i < pkgBytes.length; i++) {
                                if (i + index >= data.length) {
                                    pkgBytes[i] = 0x00;
                                    Logger.d("========data====1=====");
                                } else {
                                    pkgBytes[i] = data[i + index];
                                    //Logger.d("========data====2=====" + (i + index));
                                }
                            }
                            Logger.d("========data====index=====" + index + " data.length " + data.length);

                            OTAParam.isSendBinOneFrame = false;

                            // 保存，用于重发
                            OTAParam.pkgBytes = pkgBytes;
                            OTAParam.index = index;
                            OTAParam.reSend = false;
                            SerialUtils.getInstance().sendOtaDataPackage(pkgBytes, index);

                            index += readLen;
                            if (index >= 0x0000CF00) {
                                Logger.d("========data====3=====");
                                isEndSend = true;
                                index = data.length;
                            }

                            activity.runOnUiThread(() -> {
                                //Logger.d("========data====pro=====" + (index * 1.0f / data.length * 100));
                                showDialogUpdate((int) (index * 1.0f / 0x0000CF00 * 100));
                            });
                        }
                        Thread.sleep(400);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (OTAParam.isSendBinOneFrame) {
                    ShellCmdUtils.getInstance().execCommand("reboot");
                }
            }
        }).start();
    }

    public byte[] readStream(String fileName) {
        try {
            InputStream inStream = new FileInputStream(fileName);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inStream.close();
            return outStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ProgressDialog dialog = null;

    private void showDialogUpdate(int pro) {
        if (dialog == null) {
            dialog = new ProgressDialog(activity);
        }
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setTitle(activity.getString(R.string.ota_update));

        dialog.show();
        dialog.setProgress(pro);
        if (pro >= 100) {
            dialog.dismiss();
        }
    }

}
