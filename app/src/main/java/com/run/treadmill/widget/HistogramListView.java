package com.run.treadmill.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.run.treadmill.R;
import com.run.treadmill.util.UnitUtil;

public class HistogramListView extends View {
    public static final int TYPE_INCLINE = 0;
    public static final int TYPE_SPEED = 1;
    /**
     * 图类型，主要是返回的30段数据整型还是浮点型
     */
    private int type;

    /**
     * 控件高度与layout_width保持一致(必须是数值)
     */
    private float viewHeight = getResources().getDimension(R.dimen.dp_px_500_y);

    /**
     * 控件宽度与layout_height保持一致(必须是数值)
     */
    private float viewWidth = getResources().getDimension(R.dimen.dp_px_1120_x);

    /**
     * 柱状宽度
     */
    private float chartWidth = 35.3f;
    /**
     * 包括间隔柱宽
     */
    private float chartWidth2 = 65f;

    /**
     * 柱状宽度
     */
    private float chartHeight = 360f;

    /**
     * 每一条柱的间隔
     */
    private float chartPadding = 2f;

    /**
     * 底部背景色高度
     */
    private int bottomHeight = 50;

    /**
     * 当前正在运动的位置
     */
    private int runStageNum = 0;


    private Paint viewPain;

    private Paint bottomPain;

    private Paint lineBgPain;

    private Paint finishPain, workPain, unFinishPain;

    /**
     * 运动模式名称
     */
    private String modeName = "";
    private Paint txtPain;
    /**
     * 设置的字体长度
     */
    private float txtWidth = 0.0f;

    /**
     * 显示的最大值(标尺)
     */
    private int maxValue = 20;
    private float avgHeight = 30;
    /**
     * 最大绘制值
     */
    private float maxDrawValue = maxValue;
    private float minDrawValue = 0;
    private final int maxSize = 30;
    private float[] valueArray = new float[maxSize];
//    private float[] valueArray = new float[]{16,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10};

    private boolean clickable = false;
    private float touchMaxY, touchMinY;


    public HistogramListView(Context context) {
        super(context);
    }

    public HistogramListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray type = context.obtainStyledAttributes(attrs, R.styleable.HistogramListView);

        viewHeight = type.getDimension(R.styleable.HistogramListView_viewHeight, viewHeight);
        viewWidth = type.getDimension(R.styleable.HistogramListView_viewWidth, viewWidth);

        maxValue = type.getInteger(R.styleable.HistogramListView_maxValue, maxValue);
        bottomHeight = type.getInteger(R.styleable.HistogramListView_bottomHeight, bottomHeight);

        chartHeight = type.getFloat(R.styleable.HistogramListView_chartHeight, chartHeight);
        chartWidth = type.getFloat(R.styleable.HistogramListView_chartWidth, chartWidth);
        chartPadding = type.getFloat(R.styleable.HistogramListView_chartPadding, chartPadding);
        chartWidth2 = chartWidth + chartPadding;


        touchMaxY = viewHeight - bottomHeight;
        touchMinY = viewHeight - (chartHeight + bottomHeight);

        avgHeight = (chartHeight / maxValue);

        int histogramListView_bg = ContextCompat.getColor(context, R.color.histogramListView_bg);

        viewPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        viewPain.setColor(histogramListView_bg);

        int histogramListView_bottom = ContextCompat.getColor(context, R.color.histogramListView_bottom);

        bottomPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottomPain.setColor(histogramListView_bottom);

        int histogramListView_bottom_text = ContextCompat.getColor(context, R.color.histogramListView_bottom_text);
        modeName = type.getString(R.styleable.HistogramListView_modeName);
        if (modeName == null) {
            modeName = "";
        }

        float txtSize = 50f;
        txtSize = type.getDimension(R.styleable.HistogramListView_txtSize, txtSize);

        txtPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPain.setColor(histogramListView_bottom_text);
        txtPain.setStrokeWidth(0.3f);
        txtPain.setTextSize(txtSize);
        Rect rect = new Rect();
        txtPain.getTextBounds(modeName, 0, modeName.length(), rect);
        txtWidth = rect.width();

        int histogramListView_line_bg_head = ContextCompat.getColor(context, R.color.histogramListView_line_bg_head);
        int histogramListView_line_bg_bottom = ContextCompat.getColor(context, R.color.histogramListView_line_bg_bottom);

        lineBgPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient linearGradient = new LinearGradient(
                chartWidth / 2, (viewHeight - chartHeight),
                chartWidth / 2, viewHeight - bottomHeight,
                histogramListView_line_bg_head, histogramListView_line_bg_bottom,
                Shader.TileMode.CLAMP
        );
        lineBgPain.setShader(linearGradient);


        int histogramListView_line_passed = ContextCompat.getColor(context, R.color.histogramListView_line_passed);
        finishPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        finishPain.setColor(histogramListView_line_passed);

        int histogramListView_line_working = ContextCompat.getColor(context, R.color.histogramListView_line_working);
        workPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        workPain.setColor(histogramListView_line_working);

        int histogramListView_line_future = ContextCompat.getColor(context, R.color.histogramListView_line_future);
        unFinishPain = new Paint(Paint.ANTI_ALIAS_FLAG);
        unFinishPain.setColor(histogramListView_line_future);
    }

    public HistogramListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!clickable) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            calcFingerTouchPoint(event.getX(), event.getY());
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            calcFingerTouchPoint(event.getX(), event.getY());
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            runStageNum = -1;
            invalidate();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, viewWidth, viewHeight, viewPain);
        canvas.drawRect(0, viewHeight - bottomHeight, viewWidth, viewHeight, bottomPain);
        canvas.drawText(modeName, (viewWidth - txtWidth) / 2, (viewHeight - bottomHeight / 4), txtPain);

        for (int i = 0; i < maxSize; i++) {
            if (i % 2 == 0) {
                canvas.drawRect((i * (chartWidth + chartPadding)) + chartPadding, viewHeight - chartHeight, chartWidth + i * (chartWidth + chartPadding) + chartPadding, viewHeight - bottomHeight, lineBgPain);
            }
        }
        if (clickable) {
            for (int i = 0; i < maxSize; i++) {
                if (i == runStageNum) {
                    drawRect(canvas, workPain, i, valueArray[i]);
                } else {
                    drawRect(canvas, unFinishPain, i, valueArray[i]);
                }
            }
        } else {
            for (int i = 0; i < maxSize; i++) {
                if (i > runStageNum) {
                    drawRect(canvas, unFinishPain, i, valueArray[i]);
                } else if (i < runStageNum) {
                    drawRect(canvas, finishPain, i, valueArray[i]);
                } else {
                    drawRect(canvas, workPain, i, valueArray[i]);
                }
            }
        }

    }

    private void drawRect(Canvas canvas, Paint pain, int num, float value) {
        if (value == 0) {
            canvas.drawRect((num * (chartWidth + chartPadding)) + chartPadding, viewHeight - bottomHeight - avgHeight * 0.1f, chartWidth + num * (chartWidth + chartPadding) + chartPadding, viewHeight - bottomHeight, pain);
        } else {
            canvas.drawRect((num * (chartWidth + chartPadding)) + chartPadding, viewHeight - bottomHeight - avgHeight * value, chartWidth + num * (chartWidth + chartPadding) + chartPadding, viewHeight - bottomHeight, pain);
        }
    }

    private void calcFingerTouchPoint(float touchX, float touchY) {
        float value;
        if (touchY > (touchMaxY + 10)) {
            value = 0;
        } else if (touchY < (touchMinY - 10)) {
            value = maxValue;
        } else {
            value = UnitUtil.getFloatBy1f((touchMaxY - touchY) / avgHeight);
        }
        if (value > maxDrawValue) {
            value = maxDrawValue;
        } else if (value < minDrawValue) {
            value = minDrawValue;
        }

        runStageNum = (int) (touchX / chartWidth2);
        if (runStageNum >= maxSize || runStageNum < 0) {
            return;
        }
        if (type == TYPE_INCLINE) {
            value = UnitUtil.getFloatToInt(value);
        }
        valueArray[runStageNum] = value;
        invalidate();
    }

    /**
     * 设置图类型，0：扬升，1：速度，默认是扬升
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    public void setMaxValue(float value) {
        //小数点自动进一
        this.maxValue = (int) (Math.ceil(value));
        avgHeight = (chartHeight / maxValue);
    }

    /**
     * 设置最大绘制值（速度扬升值）
     *
     * @param value
     */
    public void setMaxDrawValue(float value) {
        this.maxDrawValue = value;
    }

    public void setMinDrawValue(float value) {
        this.minDrawValue = value;
    }

    public void setRunStageNum(int stageNum) {
        if (stageNum < 0) {
            stageNum = 0;
        }
        if (stageNum > maxSize) {
            stageNum = maxSize - 1;
        }
        runStageNum = stageNum;
    }

    public void setClickAble(boolean enable) {
        clickable = enable;
        runStageNum = -1;
    }

    public void setValueArray(float[] valueArray) {
//        for (int i = 0; i < maxSize; i++) {
//            this.valueArray[i] = valueArray[i];
//        }
        System.arraycopy(valueArray, 0, this.valueArray, 0, maxSize);
//        this.valueArray = valueArray;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
        Rect rect = new Rect();
        txtPain.getTextBounds(modeName, 0, modeName.length(), rect);
        txtWidth = rect.width();
        postInvalidate();
    }

    public float[] getValueArray() {
        return valueArray;
    }
}
