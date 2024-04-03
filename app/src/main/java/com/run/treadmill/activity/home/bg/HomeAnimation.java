package com.run.treadmill.activity.home.bg;

import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import com.run.treadmill.R;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ResourceUtils;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.widget.BlurringView;

// 图片轮播图，淡入淡出
public class HomeAnimation {
    private ImageView iv_home_bg;
    private HomeActivity activity;

    private BlurringView blur_sign;
    private BlurringView blur_media;
    private BlurringView blur_quickstart;
    private BlurringView blur_program;
    private BlurringView blur_setting;

    public HomeAnimation(ImageView iv_home_bg, HomeActivity activity) {
        setIv_home_bg(iv_home_bg);
        this.activity = activity;
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
    private Thread thread2;
    private boolean threadRun = true;   //线程结束标志符
    private boolean pause = false;
    private int duration = 2000;
    private int sleep = 7000;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            arr[0] = drawables[change % drawables.length];
            arr[1] = drawables[(change + 1) % drawables.length];
            TransitionDrawable transitionDrawable = new TransitionDrawable(arr);
            iv_home_bg.setBackground(transitionDrawable);
            transitionDrawable.startTransition(duration);

            if (openBlur) {
                startBlue();
            }

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
        Logger.i("开始背景轮播图动画");

        if (openBlur) {
            thread2 = new Thread(new MyRunnable2());
            thread2.start();
        }
    }

    public void destroy() {
        Logger.i("轮播图线程销毁");
        threadRun = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }

        if (thread2 != null) {
            thread2.interrupt();
            thread2 = null;
        }
    }

    public void pause() {
        pause = true;
    }

    public void resume() {
        pause = false;
    }

    /**
     * 模糊按钮相关
     * -----------------------------------------------------------------------------
     */

    public void setBlur(boolean b) {
        openBlur = b;

        if (openBlur) {
            initBlurBtn();
        }
    }

    private void startBlue() {
        startBlue = true;
        ThreadUtils.runInThread(() -> {
            startBlue = false;
        }, 2000);
    }

    private void initBlurBtn() {
       /* this.blur_sign = activity.findViewById(R.id.blur_sign);
        this.blur_media = activity.findViewById(R.id.blur_media);
        this.blur_quickstart = activity.findViewById(R.id.blur_quickstart);
        this.blur_program = activity.findViewById(R.id.blur_program);
        this.blur_setting = activity.findViewById(R.id.blur_setting);
*/
        setBlurView(blur_sign);
        setBlurView(blur_media);
        setBlurView(blur_quickstart);
        setBlurView(blur_program);
        setBlurView(blur_setting);
    }

    private void setBlurView(BlurringView blur_program) {
        blur_program.setBlurredView(iv_home_bg);
        //设置圆角；
        blur_program.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 20);
            }
        });
        blur_program.setClipToOutline(true);
    }

    private boolean openBlur = false;

    private boolean startBlue = false;

    private class MyRunnable2 implements Runnable {
        @Override
        public void run() {
            try {
                while (threadRun) {
                    if (pause) {
                        Logger.d("当前处于暂停轮播图2");
                        Thread.sleep(sleep);
                        continue;
                    }
                    // Logger.d("sendHandler 轮播图2");
                    if (startBlue) {
                        Logger.d("模糊");
                        blur_sign.postInvalidate();
                        Thread.sleep(30);

                        blur_media.postInvalidate();
                        Thread.sleep(30);

                        blur_quickstart.postInvalidate();
                        Thread.sleep(30);

                        blur_program.postInvalidate();
                        Thread.sleep(30);

                        blur_setting.postInvalidate();

                        Thread.sleep(120);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
