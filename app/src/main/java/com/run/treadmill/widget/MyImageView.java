package com.run.treadmill.widget;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class MyImageView extends ImageView {
    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {


        // getPaint().setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));

        super.onDraw(canvas);
    }
}
