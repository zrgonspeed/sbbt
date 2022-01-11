package com.run.treadmill.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import android.util.Log;
import android.widget.ImageView;

import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.SpManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
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
    private static final long UNAVAILABLE = -1L;

    public static String uPath = "/storage/udiskh";
    public static String sdPath = "/storage/extsd";

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

    public static String getUdiskPath(Context context, String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getStoragePath(context, "usb");
        } else {
            return path;
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

    public static long getUDiskTotalSpace(Context mContext) {
        String path = getStoragePath(mContext, "usb") + "test";

        File dirPath = new File(path);
        dirPath.mkdirs();

        if (!dirPath.isDirectory() || !dirPath.canWrite()) {
            Log.d(TAG, dirPath.canWrite() + "=" + dirPath.isDirectory());
            return UNAVAILABLE;
        } else {
            return 1;
        }
    }

    /**
     * 安装APK文件
     *
     * @param mContext
     * @param apkfile
     */
    public static void installApk(Context mContext, File apkfile) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) { // 7.0+以上版本
            Uri apkUri = FileProvider.getUriForFile(mContext,
                    mContext.getApplicationContext().getPackageName() + ".provider", apkfile);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setDataAndType(apkUri, "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                    "application/vnd.android.package-archive");
        }
        mContext.startActivity(i);
    }

    private static String getStoragePath(Context pContext, String keyword) {
        final StorageManager storageManager = (StorageManager) pContext.getSystemService(Context.STORAGE_SERVICE);
        try {
            //得到StorageManager中的getVolumeList()方法的对象
            final Method getVolumeList = storageManager.getClass().getMethod("getVolumeList");
            //---------------------------------------------------------------------

            //得到StorageVolume类的对象
            final Class<?> storageValumeClazz = Class.forName("android.os.storage.StorageVolume");
            //---------------------------------------------------------------------
            //获得StorageVolume中的一些方法
            final Method getPath = storageValumeClazz.getMethod("getPath");
            Method isRemovable = storageValumeClazz.getMethod("isRemovable");

            Method mGetState = null;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                try {
                    mGetState = storageValumeClazz.getMethod("getState");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            Method getUserLabel = storageValumeClazz.getMethod("getUserLabel");

            //调用getVolumeList方法，参数为：“谁”中调用这个方法
            final Object invokeVolumeList = getVolumeList.invoke(storageManager);
            final int length = Array.getLength(invokeVolumeList);

            for (int i = 0; i < length; i++) {
                final Object storageValume = Array.get(invokeVolumeList, i);//得到StorageVolume对象
                final String path = (String) getPath.invoke(storageValume);
                final boolean removable = (Boolean) isRemovable.invoke(storageValume);
                String state = null;
                if (mGetState != null) {
                    state = (String) mGetState.invoke(storageValume);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        state = Environment.getStorageState(new File(path));
                    } else {
                        if (removable) {
                            state = EnvironmentCompat.getStorageState(new File(path));
                        } else {
                            //不能移除的存储介质，一直是mounted
                            state = Environment.MEDIA_MOUNTED;
                        }
                        final File externalStorageDirectory = Environment.getExternalStorageDirectory();
                    }
                }

                //
                String userLabel = (String) getUserLabel.invoke(storageValume);

                if (keyword.contains("SD")) {
                    if (path.equals(sdPath)) {
                        return path + "/";
                    }
                    if (userLabel.contains(keyword)) {
                        return path + "/";
                    }
                } else if (keyword.contains("usb")) {
                    if (path.equals(uPath)) {
                        return path + "/";
                    }
                    File videoPath = new File(path + "/" + CTConstant.vrVideoPath[0]);
                    if (!videoPath.exists()) {
                        if (!userLabel.contains("内部存储")
                                && !userLabel.contains("Intern")
                                && !userLabel.contains("SD")) {
                            return path + "/";
                        }
                    }
                }

            }
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long getTotalSize(String path) {
        try {
            final StatFs statFs = new StatFs(path);
            long blockSize = 0;
            long blockCountLong = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
                blockCountLong = statFs.getBlockCountLong();
            } else {
                blockSize = statFs.getBlockSize();
                blockCountLong = statFs.getBlockCount();
            }
            return blockSize * blockCountLong;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static long getAvailableSize(String path) {
        try {
            final StatFs statFs = new StatFs(path);
            long blockSize = 0;
            long availableBlocks = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
                availableBlocks = statFs.getAvailableBlocksLong();
            } else {
                blockSize = statFs.getBlockSize();
                availableBlocks = statFs.getAvailableBlocks();
            }
            return availableBlocks * blockSize;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static final long A_GB = 1073741824;
    private static final long A_MB = 1048576;
    private static final int A_KB = 1024;

    private static String fmtSpace(long space) {
        if (space <= 0) {
            return "0";
        }
        double gbValue = (double) space / A_GB;
        if (gbValue >= 1) {
            return String.format("%.2fGB", gbValue);
        } else {
            double mbValue = (double) space / A_MB;
            Logger.e("GB", "gbvalue=" + mbValue);
            if (mbValue >= 1) {
                return String.format("%.2fMB", mbValue);
            } else {
                final double kbValue = space / A_KB;
                return String.format("%.2fKB", kbValue);
            }
        }
    }

    @SuppressLint("PrivateApi")
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
}
