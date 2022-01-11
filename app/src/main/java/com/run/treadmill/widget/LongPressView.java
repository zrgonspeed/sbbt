package com.run.treadmill.widget;

import android.content.Context;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/24
 */
public class LongPressView extends ImageView {

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

    public LongPressView(Context context) {
        super(context);
    }

    public LongPressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mLongPressRunnable = () -> {
            mCounter--;
            //计数器大于0，说明当前执行的Runnable不是最后一次down产生的。
            if (mCounter > 0 || isReleased || isMoved) {
                return;
            }
            performLongClick();//回调长按事件
        };
    }

    public LongPressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPressTime(int pressTime) {
        this.pressTime = pressTime;
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
        return true;
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
}