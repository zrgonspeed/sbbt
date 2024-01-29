package com.run.treadmill.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.run.treadmill.R;
import com.run.treadmill.util.Logger;

public class LeftSeekBar extends SeekBar {

    public static final int KEY_USER_PRESSED = R.id.tag_pressed;

    public LeftSeekBar(Context context) {
        super(context);
    }

    public LeftSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LeftSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }


    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    //初始值从下往上
    @Override
    protected void onDraw(Canvas c) {
/*        c.rotate(-90);
        c.translate(-getHeight(), 0);*/

        super.onDraw(c);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(l);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                setTag(KEY_USER_PRESSED, true);
                // Logger.i("event.getX() == " + event.getX() + "   getWidth() == " + getWidth() + "  getMax() == " + (getMax() + 1));
                // Logger.i("getMax() * event.getX() / getWidth() == " + ((getMax() + 1) * event.getX() / getWidth()));
                int i = (int) ((getMax() + 1) * event.getX() / getWidth());
                // Logger.i("i == " + i);
                setProgress(i);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;
            case MotionEvent.ACTION_UP:
                setTag(KEY_USER_PRESSED, false);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        // onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

}