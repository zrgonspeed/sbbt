package com.run.treadmill.autoupdate.http;

import java.io.File;

public interface WvDownloadListener {

    void onDownloadSuccess(File file);

    void onDownLoading(int progress, long lave);

    void onDownloadFailed(Exception e);
}
