package com.run.treadmill.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.sp.SpManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/10
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * 检测文件是否存在
     *
     * @param filepath
     * @return
     */
    public static boolean isCheckExist(String filepath) {
        try {
            if (null == filepath || filepath.isEmpty()) {
                return false;
            }
            File file = new File(filepath);
            return file.exists();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 删除apk文件
     *
     * @param path 文件路径
     */
    public static void deleteApkFile(String path) {
        if (!path.isEmpty()) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }
    }

    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static int CopySdcardFile(Context mContext, String fromFile, String toFile) {
        Logger.e(TAG, " copyShockFileToSd " + toFile + "--" + fromFile + "length = " + (new File(fromFile)).length());
        FileChannel input = null;
        FileChannel output = null;
        try {
            input = new FileInputStream(new File(fromFile)).getChannel();
            output = new FileOutputStream(new File(toFile)).getChannel();
            long length = output.transferFrom(input, 0, input.size());
            Logger.e(TAG, " copyShockFileToSd length = " + length);
        } catch (Exception e) {
            Logger.d(TAG, "error occur while CopySdcardFile: " + e);
        } finally {
            try {
                input.close();
                output.close();
                return 0;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 把logo显示在控件上
     *
     * @param mContext
     * @param btn_ergo
     */
    public static void setLogoIcon(Context mContext, ImageView btn_ergo) {
        if (!SpManager.getIsInnerLogo() &&
                isCheckExist(mContext.getFilesDir() + "/" + InitParam.LOGO_NAME)) {
            Bitmap bmpDefaultPic = BitmapFactory.decodeFile(mContext.getFilesDir() + "/" + InitParam.LOGO_NAME, null);
            btn_ergo.setImageBitmap(bmpDefaultPic);
//            bmpDefaultPic.recycle();
        }
    }

    /**
     * 根据文件路径获得bitmap
     *
     * @param imgPath 图片文件路径
     * @return bitmap
     */
    public static Bitmap imgFileTOBitmap(String imgPath) {
        if (imgPath.isEmpty() || !isCheckExist(imgPath)) {
//            throw new FileNotFoundException("没有此文件！！！");
            return null;
        }
        return BitmapFactory.decodeFile(imgPath, null);
    }

    /**
     * A133新方法
     * 安装APK文件
     *
     * @param mContext
     * @param apkfile
     */
    public static void installApk(Context mContext, File apkfile) {
        Intent apkIntent = new Intent();
        apkIntent.setAction(Intent.ACTION_VIEW);
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0+版本
            apkIntent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            apkUri = FileProvider.getUriForFile(mContext,
                    mContext.getApplicationContext().getPackageName() + ".provider", apkfile);
            apkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            apkUri = Uri.parse("file://" + apkfile.toString());
        }
        apkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        apkIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");

        mContext.startActivity(apkIntent);
        ShellCmdUtils.getInstance().execCommand("sync");
    }

    public static String getStoragePath(Context context, boolean isUsb) {
        String path = "";
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> volumeInfoClazz;
        Class<?> diskInfoClaszz;
        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            diskInfoClaszz = Class.forName("android.os.storage.DiskInfo");
            Method StorageManager_getVolumes = Class.forName("android.os.storage.StorageManager").getMethod("getVolumes");
            Method VolumeInfo_GetDisk = volumeInfoClazz.getMethod("getDisk");
            Method VolumeInfo_GetPath = volumeInfoClazz.getMethod("getPath");
            Method DiskInfo_IsUsb = diskInfoClaszz.getMethod("isUsb");
            Method DiskInfo_IsSd = diskInfoClaszz.getMethod("isSd");
            List<Object> List_VolumeInfo = (List<Object>) StorageManager_getVolumes.invoke(mStorageManager);
            assert List_VolumeInfo != null;
            for (int i = 0; i < List_VolumeInfo.size(); i++) {
                Object volumeInfo = List_VolumeInfo.get(i);
                Object diskInfo = VolumeInfo_GetDisk.invoke(volumeInfo);
                if (diskInfo == null) continue;
                boolean sd = (boolean) DiskInfo_IsSd.invoke(diskInfo);
                boolean usb = (boolean) DiskInfo_IsUsb.invoke(diskInfo);
                File file = (File) VolumeInfo_GetPath.invoke(volumeInfo);
                if (isUsb == usb) {//usb
                    assert file != null;
                    path = file.getAbsolutePath();
                } else if (!isUsb == sd) {//sd
                    assert file != null;
                    path = file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "[——————— ——————— Exception:" + e.getMessage() + "]");
            e.printStackTrace();
        }
        Log.d(TAG, " path " + path);
        return path;
    }

    /**
     * 6.0获取外置sdcard和U盘路径，并区分
     *
     * @param context
     * @param keyword SD = "内部存储"; EXT = "SD卡"; USB = "U盘"
     * @return
     */
    public static String getStoragePath(Context context, String keyword) {
        boolean isUsb = keyword.contains("usb") ? true : false;
        String path = "";
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> volumeInfoClazz;
        Class<?> diskInfoClaszz;
        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            diskInfoClaszz = Class.forName("android.os.storage.DiskInfo");
            Method StorageManager_getVolumes = Class.forName("android.os.storage.StorageManager").getMethod("getVolumes");
            Method VolumeInfo_GetDisk = volumeInfoClazz.getMethod("getDisk");
            Method VolumeInfo_GetPath = volumeInfoClazz.getMethod("getPath");
            Method DiskInfo_IsUsb = diskInfoClaszz.getMethod("isUsb");
            Method DiskInfo_IsSd = diskInfoClaszz.getMethod("isSd");
            List<Object> List_VolumeInfo = (List<Object>) StorageManager_getVolumes.invoke(mStorageManager);
            assert List_VolumeInfo != null;
            for (int i = 0; i < List_VolumeInfo.size(); i++) {
                Object volumeInfo = List_VolumeInfo.get(i);
                Object diskInfo = VolumeInfo_GetDisk.invoke(volumeInfo);
                if (diskInfo == null) continue;
                boolean sd = (boolean) DiskInfo_IsSd.invoke(diskInfo);
                boolean usb = (boolean) DiskInfo_IsUsb.invoke(diskInfo);
                File file = (File) VolumeInfo_GetPath.invoke(volumeInfo);
                //if (file !=null) {
                Log.d("assert", "USB4");
                if (isUsb == usb) {//usb
                    Log.d("assert", "USB");
                    if (file != null) {
                        Log.d("assert", "USB2");
                        assert (file != null);
                        Log.d("assert", "USB1");
                        path = file.getAbsolutePath();
                    }
                } else if (!isUsb == sd) {//sd
                    if (file != null) {
                        assert (file != null);
                        path = file.getAbsolutePath();
                    }
                }
                //}
            }
        } catch (Exception e) {
            Log.d(TAG, "[——————— ——————— Exception:" + e.getMessage() + "]");
            e.printStackTrace();
        }
        Log.d(TAG, " path " + path);
        return path + "/";
    }
}