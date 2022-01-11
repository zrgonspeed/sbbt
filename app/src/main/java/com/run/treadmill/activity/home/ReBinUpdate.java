package com.run.treadmill.activity.home;

import android.app.Activity;
import android.app.ProgressDialog;

import com.run.android.ShellCmdUtils;
import com.run.serial.SerialUtils;
import com.run.treadmill.R;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

public class ReBinUpdate {

    private Activity activity;

    public void procBin(Activity act, String path) {
        Logger.d("========data======1===");
        if (SerialUtils.getInstance().isSendBinCnt) {
            return;
        }
        activity = act;
        SerialUtils.getInstance().isSendBinCnt = true;
        SerialUtils.getInstance().reMoveAllReSendPackage();

        //ControlManager.getInstance().sendUpdateCmd();
        //SerialUtils.getInstance().sendOtaConnectPackage();
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SerialUtils.getInstance().isSendBinData = true;
        SerialUtils.getInstance().sendOtaConnectPackage();

        byte[] data = readStream(path);
        Logger.d("========data=========" + data.length);

        new Thread(new Runnable() {
            int index = 0;
            boolean isEndSend = false;

            int oneFrameLen = 256;

            @Override
            public void run() {
                while (!isEndSend && !SerialUtils.isSendBinData) {
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
                /*SerialUtils.isSendOtaData = true;
                SerialUtils.isSendOtaOneFrame = true;*/
                //SpManager.setBinUpdate(true);
                Logger.d("========data======2===");
                while (!isEndSend) {
                    try {
                        if (SerialUtils.isSendBinData && SerialUtils.isSendBinOneFrame) {
                            int readLen = oneFrameLen;
                            byte[] pkgBytes;
                            /*if ( (index + oneFrameLen) >= data.length ) {
                                readLen = data.length - index;
                            }*/
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

                            SerialUtils.isSendBinOneFrame = false;
                            SerialUtils.getInstance().sendOtaDataPackage(pkgBytes, index);

                            index += readLen;
                            //if ( index >= data.length ) {
                            if (index >= 0x0000CF00) {
                                SpManager.setBinUpdate(false);
                                Logger.d("========data====3=====");
                                isEndSend = true;
                                index = data.length;
                            }

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Logger.d("========data====pro=====" + (index * 1.0f / data.length * 100));
                                    if (index != readLen) {
                                        showDialogUpdate((int) (index * 1.0f / 0x0000CF00 * 100));
                                    }
                                }
                            });
                            Logger.d("isEndSend1=" + isEndSend);
                        }
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Logger.d("isEndSend=" + isEndSend);
                while (isEndSend && SerialUtils.isSendBinOneFrame) {
                    ShellCmdUtils.getInstance().execCommand("reboot");
                    break;
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
        /*dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                dialog.dismiss();
            }
        });*/
        dialog.show();
        dialog.setProgress(pro);
        if (pro >= 100) {
            dialog.dismiss();
            //Toast.makeText(getContext(), "NO NETWORKS FOUND", Toast.LENGTH_LONG).show();
        }
    }

}
