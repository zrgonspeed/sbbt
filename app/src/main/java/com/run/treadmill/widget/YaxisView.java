package com.run.treadmill.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.util.Logger;

public class YaxisView extends View {
    private float viewHeight = getDimen(R.dimen.dp_px_400_y);
    private float viewWidth = getDimen(R.dimen.dp_px_100_x);

    public YaxisView(Context context) {
        super(context);
    }

    public YaxisView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Logger.i("viewHeight == " + viewHeight + "    viewWidth == " + viewWidth);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(yLineWidth);
        mPaint.setColor(Color.WHITE);
    }

    public YaxisView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 无用
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private Paint mPaint;
    private float yLineWidth = getDimen(R.dimen.dp_px_3_x);
    private float yLineHeight = getDimen(R.dimen.dp_px_360_y);

    public void setyLineHeight(float yLineHeight) {
        this.yLineHeight = yLineHeight;
    }

    private float xLineWidth = getDimen(R.dimen.dp_px_8_x);
    private float xLineHeight = getDimen(R.dimen.dp_px_3_y);

    private float maxValue;
    private float minValue;

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    private float[] arr = {};

    public void setOtherValue(float[] arr) {
        this.arr = arr;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1.画竖线
        drawYLine(canvas);

        // 2.画横线  x1都一样，x2都一样
        // 最大值
        drawMaxValue(canvas);
        // 最小值
        drawMinValue(canvas);

        // 画中间其它值
        for (float a : arr) {
            drawValueLine(canvas, a);
        }
    }

    private void drawYLine(Canvas canvas) {
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(yLineWidth);
        float x1 = viewWidth - yLineWidth / 2;
        float y1 = viewHeight - yLineHeight;
        float x2 = viewWidth - yLineWidth / 2;
        float y2 = viewHeight;
        canvas.drawLine(x1, y1, x2, y2, mPaint);
    }

    private void drawMaxValue(Canvas canvas) {
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(yLineWidth);
        float x11 = viewWidth - (yLineWidth + xLineWidth);
        float y11 = viewHeight - yLineHeight + (xLineHeight / 2);
        float x22 = x11 + xLineWidth;
        float y22 = y11;
        canvas.drawLine(x11, y11, x22, y22, mPaint);

        drawMaxValueText(canvas, x11, y11);
    }

    private void drawMinValue(Canvas canvas) {
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(yLineWidth);
        float x111 = viewWidth - (yLineWidth + xLineWidth);
        float y111 = viewHeight - (xLineHeight / 2);
        float x222 = x111 + xLineWidth;
        float y222 = y111;
        canvas.drawLine(x111, y111, x222, y222, mPaint);

        drawText(canvas, minValue, x111, y111);
    }

    private void drawValueLine(Canvas canvas, float value) {
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(yLineWidth);
        float v = value / maxValue;
        float x111 = viewWidth - (yLineWidth + xLineWidth);
        float y111 = viewHeight - v * yLineHeight + (xLineHeight / 2);
        float x222 = x111 + xLineWidth;
        float y222 = y111;
        canvas.drawLine(x111, y111, x222, y222, mPaint);

        drawText(canvas, value, x111, y111);
    }

    private void drawText(Canvas canvas, float value, float x, float y) {
        setDrawTextPaint();
        if (isIncline) {
            // 画负扬升
            canvas.drawText(String.valueOf((int) value - 5), x - getDimen(R.dimen.dp_px_7_x), y + getDimen(R.dimen.dp_px_10_x), mPaint);
            return;
        }
        canvas.drawText(String.valueOf((int) value), x - getDimen(R.dimen.dp_px_7_x), y + getDimen(R.dimen.dp_px_10_x), mPaint);
    }

    private void setDrawTextPaint() {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(getDimen(R.dimen.dp_px_1_x));
        mPaint.setTextSize(getDimen(R.dimen.font_size_yaxis));
        mPaint.setTextAlign(Paint.Align.RIGHT);
    }

    private void drawMaxValueText(Canvas canvas, float x, float y) {
        setDrawTextPaint();
        if (isIncline) {
            canvas.drawText("15", x - getDimen(R.dimen.dp_px_7_x), y + getDimen(R.dimen.dp_px_10_x), mPaint);
            return;
        }
        canvas.drawText(viewMaxValue, x - getDimen(R.dimen.dp_px_7_x), y + getDimen(R.dimen.dp_px_10_x), mPaint);
    }

    private float getDimen(int id) {
        return getResources().getDimension(id);
    }

    /**
     * 扬升最大15 速度最大22 英制速度最大13.6，所以需要单独设置显示的最大值，不然会全部转为int
     */
    private String viewMaxValue = "";

    public void setDrawMaxValue(String value) {
        viewMaxValue = value;
    }

    private boolean isIncline;

    public void setIsIncline(boolean b) {
        this.isIncline = b;
    }
}
