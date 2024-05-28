package com.run.treadmill.autoupdate.util;

import android.content.Context;

import androidx.annotation.IntDef;

import java.io.Serializable;
import java.util.List;

public class WvAppBean implements Serializable {
    public static final int UPDATE = 1;
    public static final int NO_UPDATE = 2;
    public static final int WAIT_DOWNLOAD = 3;
    public static final int DOWNLOADING = 4;
    public static final int WAIT_INSTALL = 5;
    public static final int INSTALLING = 6;
    public static final int DOWNLOAD_FAIL = 7;
    public static final int CHECKING = 8;

    @IntDef({UPDATE, NO_UPDATE, WAIT_DOWNLOAD, DOWNLOADING, WAIT_INSTALL, INSTALLING, DOWNLOAD_FAIL, CHECKING})
    public @interface AppStatus {
    }

    private List<AppInfo> apkInfos;

    public class AppInfo {
        private String name;
        private String version;
        private String url;
        private String sign;
        private String version_desc;
        private String created_at;
        public @AppStatus
        int isUpdate = NO_UPDATE;
        public int progress;
        public int imgId;

        public void checkUpdate(Context context, String packName) {
            isUpdate = WvVersionUtil.isNewVersion(
                    WvVersionUtil.getSpecAppVersionName(context, packName), version) ? UPDATE : NO_UPDATE;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getVersion_desc() {
            return version_desc;
        }

        public void setVersion_desc(String version_desc) {
            this.version_desc = version_desc;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public int getImgId() {
            return imgId;
        }

        public void setImgId(int imgId) {
            this.imgId = imgId;
        }

        @Override
        public String toString() {
            return "AppInfo{" +
                    "name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", url='" + url + '\'' +
                    ", sign='" + sign + '\'' +
                    ", version_desc='" + version_desc + '\'' +
                    ", created_at='" + created_at + '\'' +
                    ", isUpdate=" + isUpdate +
                    ", progress=" + progress +
                    ", imgId=" + imgId +
                    '}';
        }
    }

    public List<AppInfo> getApkInfos() {
        return apkInfos;
    }

    public void setApkInfos(List<AppInfo> apkInfos) {
        this.apkInfos = apkInfos;
    }
}