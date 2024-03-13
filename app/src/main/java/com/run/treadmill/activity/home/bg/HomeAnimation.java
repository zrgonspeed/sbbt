package com.run.treadmill.activity.home.bg;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.run.treadmill.R;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ResourceUtils;

// 图片轮播图，淡入淡出
public class HomeAnimation {
    private ImageView iv_home_bg;

    public HomeAnimation(ImageView iv_home_bg) {
        setIv_home_bg(iv_home_bg);
    }

    public void setIv_home_bg(ImageView iv_home_bg) {
        this.iv_home_bg = iv_home_bg;
    }

    private Drawable[] drawables = new Drawable[]{
            ResourceUtils.getDraw(R.drawable.bk_background_idle_mode_1),
            ResourceUtils.getDraw(R.drawable.bk_background_idle_mode_2),
            ResourceUtils.getDraw(R.drawable.bk_background_idle_mode_3),
            ResourceUtils.getDraw(R.drawable.bk_background_idle_mode_4),
            ResourceUtils.getDraw(R.drawable.bk_background_idle_mode_5)
    };
    private Drawable[] arr = new Drawable[2];
    private int change = 0;     //记录下标

    private Thread thread;
    private boolean threadRun = true;   //线程结束标志符
    private boolean pause = false;
    private int duration = 2000;
    private int sleep = 5000;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            arr[0] = drawables[change % drawables.length];
            arr[1] = drawables[(change + 1) % drawables.length];
            TransitionDrawable transitionDrawable = new TransitionDrawable(arr);
            iv_home_bg.setBackground(transitionDrawable);
            transitionDrawable.startTransition(duration);

            change++;
            return false;
        }
    });

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            try {
                while (threadRun) {
                    if (pause) {
                        Logger.d("当前处于暂停轮播图");
                        Thread.sleep(sleep);
                        continue;
                    }
                    Logger.d("sendHandler 轮播图");
                    Message message = mHandler.obtainMessage();
                    message.arg1 = duration;
                    mHandler.sendMessage(message);
                    Thread.sleep(sleep);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initAndStart() {
        if (thread != null) {
            return;
        }

        threadRun = true;
        thread = new Thread(new MyRunnable());
        thread.start();
    }

    public void destroy() {
        Logger.i("轮播图线程销毁");
        threadRun = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void pause() {
        pause = true;
    }

    public void resume() {
        pause = false;
    }
}
