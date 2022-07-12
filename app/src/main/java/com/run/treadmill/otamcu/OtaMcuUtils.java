package com.run.treadmill.otamcu;

import android.content.Context;
import android.os.Environment;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.util.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OtaMcuUtils {

    public static void installOtaMcu(Context context) {
        new Thread(() -> {
            installOtaMcu2(context);
        }).start();
    }

    private static void installOtaMcu2(Context context) {
        // 1.从assets中复制出来  ->   storage/emulated/sdcard/otamcu/
        String targetDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/otamcu";
        String assetsDirPath = "otamcu";

        // 检测assets中的apk名称与SP中的名称是否一致
        String assetsApkName = getAssetsApkName(context, assetsDirPath);
        Logger.d("assetsApkName == " + assetsApkName);
        Logger.d("SpManager.getOtaMcuName() == " + SpManager.getOtaMcuName());
        if (SpManager.getOtaMcuName().equals(assetsApkName)) {
            return;
        }

        String fileName = copyAssetsDir2Phone(context, assetsDirPath, targetDirPath);
        if (!fileName.isEmpty()) {
            // 2.安装OtaMcu.apk
            new Thread() {
                @Override
                public void run() {
                    int res = ShellCmdUtils.getInstance().execCommand("pm install -r " + targetDirPath + File.separator + fileName);
                    if (res == 0) {
                        SpManager.setOtaMcuName(fileName);
                    }
                    Logger.d("安装" + fileName + "  res==" + res);
                }
            }.start();
        }
    }

    private static String getAssetsApkName(Context context, String assetsDirPath) {
        try {
            String[] fileList = context.getAssets().list(assetsDirPath);
            if (fileList != null) {
                if (fileList.length > 0) {//如果是目录
                    for (String fileName : fileList) {
                        return fileName;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String copyAssetsDir2Phone(Context activity, String assetsDirPath, String targetDirPath) {
        Logger.d("assetsDirPath == " + assetsDirPath);
        Logger.d("targetDirPath == " + targetDirPath);
        try {
            File file = new File(targetDirPath);
            Logger.d("file.getPath() " + file.getPath());
            Logger.d("file.exists()  " + file.exists());

            if (!file.exists()) {
                boolean mkdir = file.mkdir();
                Logger.d("mkdir == " + mkdir);
            }

            String[] fileList = activity.getAssets().list(assetsDirPath);
            if (fileList != null) {
                if (fileList.length > 0) {//如果是目录
                    for (String fileName : fileList) {
                        String targetFilePath = targetDirPath + File.separator + fileName;
                        copyAssetsFile2Phone(activity, assetsDirPath + File.separator + fileName, targetFilePath);
                        return fileName;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static void copyAssetsFile2Phone(Context context, String assetFilePath, String targetFilePath) {
        Logger.d("assetFilePath==" + assetFilePath);
        Logger.d("targetFilePath==" + targetFilePath);
        try {
            InputStream inputStream = context.getAssets().open(assetFilePath);
            File file = new File(targetFilePath);
            if (file.exists()) {
                file.delete();
            }

            if (!file.exists() || file.length() == 0) {
                FileOutputStream fos = new FileOutputStream(file);//如果文件不存在，FileOutputStream会自动创建文件
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();//刷新缓存区
                inputStream.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
