package com.run.treadmill.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.run.treadmill.manager.BuzzerManager;

import java.lang.ref.WeakReference;


@SuppressLint("AppCompatCustomView")
public class LongClickView extends ImageView {

    /**
     * 长按连续响应的监听，长按时将会多次调用该接口中的方法直到长按结束
     */
    /*private LongClickRepeatListener repeatListener;*/


    /**
     * 间隔时间（ms）
     */
    private long intervalTime = 80;
    private MyHandler handler;
    private int threshold = 2;

    private boolean isEnable = true;

    public LongClickView(Context context) {
        super(context);
        init();
    }

    public LongClickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LongClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化监听
     */
    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        handler = new MyHandler(this);
        setOnLongClickListener(v -> {
            new Thread(new LongClickThread()).start();
            return true;
        });

        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                BuzzerManager.canBuzzerWhenLongKey = true;
            }

            return false;
        });
    }

    /**
     * 长按时，该线程将会启动
     */
    private class LongClickThread implements Runnable {
        private long num;

        @Override
        public void run() {
            while (LongClickView.this.isPressed()) {
                if (!isEnable) {
                    continue;
                }
//                num++;
//                if (num % threshold == 0) {
//                }
                handler.sendEmptyMessage(1);
                SystemClock.sleep(intervalTime);
            }
        }
    }

    /**
     * 通过handler，使监听的事件响应在主线程中进行
     */
    private static class MyHandler extends Handler {

        private WeakReference<LongClickView> ref;

        MyHandler(LongClickView button) {
            ref = new WeakReference<LongClickView>(button);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LongClickView button = ref.get();
            if (!button.isEnable) {
                return;
            }
            if (button != null) {
                //直接调用普通点击事件
                button.setTag(1);
                button.performClick();

                // 长按时是否响按键音
                BuzzerManager.canBuzzerWhenLongKey = false;
            }
        }
    }

    public void setIntervalTime(long intervalTime) {
        this.intervalTime = intervalTime;
    }

    public void setLongCycle(int param) {
        threshold = param;
    }

    public void setPressEnable(Boolean enable) {
        isEnable = enable;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        isEnable = enabled;
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
    }
}
