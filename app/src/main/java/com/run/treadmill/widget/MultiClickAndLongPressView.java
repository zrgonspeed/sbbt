package com.run.treadmill.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.run.treadmill.R;
import com.run.treadmill.util.Logger;

/**
 * @Description 多次点击+长按
 * @Author GaleLiu
 * @Time 2020/06/09
 */
@SuppressLint("AppCompatCustomView")
public class MultiClickAndLongPressView extends ImageView {
    /**
     * 长度决定点击的次数
     */
    private long[] mHints;
    /**
     * 多少秒内点完触发
     */
    private long limiit = 5000;
    /**
     * 连续点击次数
     */
    private int mCount = 10;
    // 是否移动了
    private boolean isMoved;
    // 是否释放了
    private boolean isReleased = true;
    // 计数器，防止多次点击导致最后一次形成longpress的时间变短
    private int mCounter = 0;
    // 长按的runnable
    private Runnable mLongPressRunnable;

    private int pressTime = 10000;
    private boolean isEnable = true;
    private OnMultiClickListener multiClickListener;

    public MultiClickAndLongPressView(Context context) {
        super(context);
        init();
    }

    public MultiClickAndLongPressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiClickAndLongPressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiClickImageView);
        mCount = typedArray.getInt(R.styleable.MultiClickImageView_clickCount, 10);
        limiit = typedArray.getInt(R.styleable.MultiClickImageView_clickTime, 5) * 1000L;
        typedArray.recycle();
        init();
    }

    private void init() {
        mHints = new long[mCount - 1];
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //将原数组的第二位到最后一个复制到第1位到倒数第2位
                System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
                mHints[mHints.length - 1] = SystemClock.uptimeMillis();
                //Logger.d("onClick", "mHints[0]=" + mHints[0] + ",m(SystemClock.uptimeMillis() - limiit)=" + (SystemClock.uptimeMillis() - limiit));
                if (mHints[0] >= (SystemClock.uptimeMillis() - limiit)) {
                    if (multiClickListener != null) {
                        mHints = new long[mCount];
                        multiClickListener.onMultiClick();
                    }
                }
            }
        });
        mLongPressRunnable = () -> {
            mCounter--;
            //计数器大于0，说明当前执行的Runnable不是最后一次down产生的。
            if (mCounter > 0 || isReleased || isMoved) {
                return;
            }
            if (multiClickListener != null) {
                mHints = new long[mCount];
                multiClickListener.onMultiClick();
            }
        };
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        if (!isEnable) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCounter++;
                isReleased = false;
                isMoved = false;
                postDelayed(mLongPressRunnable, pressTime);// 按下pressTime后调用线程
                break;
            case MotionEvent.ACTION_MOVE:
                if (!(x >= getLeft() && x <= getRight() &&
                        y >= getTop() && y <= getBottom())) {
                    // 移动超过阈值，则表示移动了
                    isMoved = true;
                    removeCallbacks(mLongPressRunnable);
                }
                break;
            case MotionEvent.ACTION_UP:
                isReleased = true;
                if (mCounter > 0) {
                    mCounter--;
                }
                removeCallbacks(mLongPressRunnable);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 长按是否释放了
     *
     * @return
     */
    public boolean isReleased() {
        return isReleased;
    }

    /**
     * 外部释放长按事件
     */
    public void releasedLongClick() {
        isReleased = true;
    }

    /**
     * 设置点击次数
     *
     * @param count
     */
    public void setClickCount(int count) {
        if (count < 1) {
            throw new RuntimeException("多次点击请大于1次好吧！！！");
        }
        mCount = count;
        mHints = new long[mCount];
    }

    /**
     * 设置总时间
     *
     * @param time 秒
     */
    public void setClickLimit(long time) {
        if (time < 1) {
            throw new RuntimeException("你设置那么小你能点得了？？？");
        }
        limiit = time * 1000L;
    }

    public void reset() {
        mHints = new long[mCount];
    }

    public void setOnMultiClickListener(OnMultiClickListener listener) {
        multiClickListener = listener;
    }

    public interface OnMultiClickListener {
        void onMultiClick();
    }
}