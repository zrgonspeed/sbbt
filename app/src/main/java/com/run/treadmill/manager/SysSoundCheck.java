package com.run.treadmill.manager;

import android.content.Context;
import android.content.Intent;


public class SysSoundCheck {
    public static final String TAG = "SysSoundCheck";

    private static SysSoundCheck ourInstance = null;
    private Thread mCheckThread = null;

    public static SysSoundCheck getInstance() {
        if (null == ourInstance) {
            synchronized (SysSoundCheck.class) {
                if (null == ourInstance) {
                    ourInstance = new SysSoundCheck();
                }
            }
        }
        return ourInstance;
    }

    private SysSoundCheck() {
    }

    /**
     * 播放音乐
     */
    public static void MusicPause(Context mContext) {
        String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
        Intent intent = new Intent();
        intent.setAction(PAUSE_ACTION);
        intent.putExtra("command", "pause");
        mContext.sendBroadcast(intent);
    }

}
