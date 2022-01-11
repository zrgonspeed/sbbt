package com.run.treadmill.ota;

import com.run.treadmill.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class OTAUtils {
    private static final String PROJECT_NAME = "AC00553-54T-08";

    /**
     * 检测U盘下，是否有符合命名规范的bin文件
     *
     * @param filepath
     * @return
     */
    public static boolean checkOtaFileExist(String filepath) {
        try {
            if (null == filepath || filepath.isEmpty()) {
                return false;
            }

            File file = new File(filepath);
            if (!file.exists()) {
                return false;
            }

            if (file.isDirectory()) {
                // 子文件检测
                File[] files = file.listFiles();
                if (files.length == 0) {
                    // 没有子文件
                    return false;
                }

                // 文件名格式检测
                // 有一个符合命名格式的文件就返回true
                for (int i = 0; i < files.length; i++) {
                    String name = files[i].getName();
                    if (isOkName(name)) {
                        return true;
                    }
                }

            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 复制U盘下，符合命名格式的最新日期bin文件到Android  /data/user/0/com.run.treadmill/files/OTA
     *
     * @param uPath
     * @param anPath
     * @return
     */
    public static boolean copyUpanToAn(String uPath, String anPath) {
        File dir = new File(uPath);
        if (!dir.exists() || !dir.isDirectory() || dir.listFiles().length == 0) {
            return false;
        }

        // 1.获取U盘文件列表
        String[] names = dir.list();
        Logger.d("U盘文件: " + Arrays.toString(names));

        // 2.选出最新日期的文件
        String lastFileName = getLastFileByDate(names);
        Logger.d("最新的bin文件: " + lastFileName);

        if (lastFileName == null) {
            return false;
        }

        // 安卓OTA目录
        File otaDir = new File(anPath);
        if (otaDir.exists()) {
            deleteDir(otaDir);
        }
        otaDir.mkdir();

        // 3.复制该最新bin文件到安卓OTA目录
        copyFile(uPath + "/" + lastFileName, anPath + "/" + lastFileName);

        return true;
    }

    /**
     * 获取最新日期的文件名
     *
     * @param names
     * @return
     */
    private static String getLastFileByDate(String[] names) {
        if (names.length == 0) {
            return null;
        }

        // 文件名格式检测
        ArrayList<String> okNames = new ArrayList<>();
        for (String name : names) {
            if (isOkName(name)) {
                okNames.add(name);
            }
        }

        // 没有符合的
        if (okNames.size() == 0) {
            return null;
        }

        // 排序
        Collections.sort(okNames, new StringComparator());
        Logger.d("文件名排序后(旧 -> 新)：" + okNames);

        // 返回最新的文件名
        return okNames.get(okNames.size() - 1);
    }

    /**
     * 检测文件名是否符合格式: 项目名-日期_版本-MD5值.bin <BR/>
     * 如：AC00511-04-210520_V10-95c0a4daf6a567f.bin
     *
     * @param name 文件名
     * @return
     */
    private static boolean isOkName(String name) {
        if (name == null) {
            return false;
        }

        if (name.length() > 16 + PROJECT_NAME.length() + 5 && ".bin".equals(name.substring(name.length() - 4))) {
            String a1 = name.substring(0, PROJECT_NAME.length() + 1);
            char a2 = name.charAt(PROJECT_NAME.length() + 1 + 10);
            char a3 = name.charAt(PROJECT_NAME.length() + 1 + 6);
            char a4 = name.charAt(PROJECT_NAME.length() + 1 + 7);

            if (a1.equals(PROJECT_NAME + "-") && '-' == a2 && '_' == a3 && 'V' == a4) {
                Logger.d("文件名" + name + "符合格式");
                return true;
            }
        }
        Logger.d("文件名" + name + "不符合格式");

        return false;
    }

    /**
     * 复制文件，用于U盘 -> 安卓
     *
     * @param oldPath
     * @param newPath
     */
    private static void copyFile(String oldPath, String newPath) {
        File temp = new File(oldPath);
        try (
                FileInputStream fileInputStream = new FileInputStream(temp);
                FileOutputStream fileOutputStream = new FileOutputStream(newPath);
        ) {
            byte[] buffer = new byte[1024];
            int byteRead;
            // !=-1 也可以写成！=null,意思是读取的数据不为负数或者null就说明还没有读取完毕
            while ((byteRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除目录和子文件
     *
     * @param dir
     */
    private static void deleteDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
        Logger.d(dir + "整个文件夹及其中文件都被删除");
    }


    /**
     * 比较日期类<BR/>
     * 截取两个文件名中的日期和版本进行比较
     */
    private static class StringComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            // 例 AC00511-04-210520_V10-95c0a4daf6a567f.bin

            // 210520_V10
            // 210527_V10
            String dateStr1 = o1.substring(PROJECT_NAME.length() + 1, PROJECT_NAME.length() + 1 + 10);
            String dateStr2 = o2.substring(PROJECT_NAME.length() + 1, PROJECT_NAME.length() + 1 + 10);

            String[] arr1 = dateStr1.split("_V");
            String[] arr2 = dateStr2.split("_V");

            if (arr1[0].compareTo(arr2[0]) > 0) {
                return 1;
            }

            if (arr1[0].compareTo(arr2[0]) == 0) {
                if (arr1[1].compareTo(arr2[1]) > 0) {
                    return 1;
                }
            }

            return -1;
        }
    }

    private static boolean printFiles(String uPath) {
        File dir = new File(uPath);
        if (!dir.exists() || !dir.isDirectory() || dir.listFiles().length == 0) {
            return false;
        }

        // 1.获取U盘文件列表
        String[] names = dir.list();
        Logger.d("U盘文件: " + Arrays.toString(names));

        // 2.选出最新日期的文件
        String lastFileName = getLastFileByDate(names);
        Logger.d("最新的bin文件: " + lastFileName);

        if (lastFileName == null) {
            return false;
        }
        return true;
    }
}
