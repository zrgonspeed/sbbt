package com.run.treadmill.activity;

import android.os.Handler;

import java.util.TimerTask;

/**
 * @Description 发空消息的task
 * @Author GaleLiu
 * @Time 2019/07/20
 */
public class EmptyMessageTask extends TimerTask {
    private Handler mHandler;
    private int mWhat;

    public EmptyMessageTask(Handler handler, int what) {
        if (handler == null) {
            throw new RuntimeException("你不传个handler给我，我闲着蛋疼呢？");
        }
        mHandler = handler;
        mWhat = what;
    }

    @Override
    public void run() {
        mHandler.sendEmptyMessage(mWhat);
    }
}