package com.run.treadmill.activity.home.bg;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.run.treadmill.R;
import com.run.treadmill.reboot.MyApplication;

public class HomeAnimation {
    private ImageView iv_home_bg;

    public HomeAnimation(ImageView iv_home_bg) {
        setIv_home_bg(iv_home_bg);
    }

    public void setIv_home_bg(ImageView iv_home_bg) {
        this.iv_home_bg = iv_home_bg;
    }

    // 图片轮播图，淡入淡出
    public void initAndStart() {
        initDrawableView();
        thread = new Thread(new MyRunnable());
        thread.start();
    }

    private int change = 0;//记录下标
    private int[] ids = new int[]{
            R.drawable.bk_background_idle_mode_1,
            R.drawable.bk_background_idle_mode_2,
            R.drawable.bk_background_idle_mode_3,
            R.drawable.bk_background_idle_mode_4,
            R.drawable.bk_background_idle_mode_5

    };
    private Drawable[] drawables;//图片集合
    private Thread thread;//线程
    private boolean threadFlag = true;//线程结束标志符

    private void initDrawableView() {
        //填充图片
        drawables = new Drawable[ids.length];
        for (int i = 0; i < ids.length; i++) {
            drawables[i] = MyApplication.getContext().getResources().getDrawable(ids[i]);
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int duration = msg.arg1;
            TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{drawables[change % ids.length],
                    drawables[(change + 1) % ids.length]});
            change++;//改变标识位置
            iv_home_bg.setBackground(transitionDrawable);
            transitionDrawable.startTransition(duration);
            return false;
        }
    });

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            //这个while(true)是做死循环
            while (threadFlag) {
                int duration = 2000;//改变的间隔
                Message message = mHandler.obtainMessage();
                message.arg1 = duration;
                mHandler.sendMessage(message);
                try {
                    Thread.sleep(5000);
                    //隔duration秒发送一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void destroy() {
        threadFlag = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
}
