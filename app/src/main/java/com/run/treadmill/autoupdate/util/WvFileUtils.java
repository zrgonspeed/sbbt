package com.run.treadmill.autoupdate.util;

import java.io.File;

public class WvFileUtils {
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
}
