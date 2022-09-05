package com.run.treadmill.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.run.treadmill.R;

import java.util.ArrayList;

/**********************************************************
 * @文件名称：LineGraphicView.java
 * @文件作者：
 * @创建时间：2015年5月27日 下午3:05:19
 * @文件描述：自定义简单曲线图
 * @修改历史：2015年5月27日创建初始版本
 **********************************************************/
public class LineGraphicView extends View {
    /**
     * 公共部分
     */
    private static final int CIRCLE_SIZE = 10;

    private static final String TAG = "LineGraphicView";

    private static enum Linestyle {
        Line, Curve
    }

    private Context mContext;
    private Paint mPaint;
    private Resources res;
    private DisplayMetrics dm;

    /**
     * data
     */
    private Linestyle mStyle = Linestyle.Curve;

    private int canvasHeight;
    private int canvasWidth;
    private int bheight = 0;
    private int blwidh;
    private boolean isMeasure = true;
    /**
     * Y轴最大值
     */
    private int maxValue;
    /**
     * X轴最大值
     */
    private float maxXValue;
    /**
     * Y轴间距值
     */
    private int averageValue;
    private int marginTop = 0;//20
    private int marginBottom = 0;//40

    /**
     * 曲线上总点数
     */
    private Point[] mPoints;
    /**
     * 纵坐标值
     */
    private ArrayList<Double> yRawData;
    /**
     * 横坐标值
     */
//    private ArrayList<String> xRawDatas;
    private ArrayList<Double> xRawDatas;
    private ArrayList<Integer> xList = new ArrayList<Integer>();// 记录每个x的值
    private int spacingHeight;

    private int color;
    private int haloColor = -1;

    public LineGraphicView(Context context) {
        this(context, null);
    }

    public LineGraphicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    private void initView() {
        this.res = mContext.getResources();
        color = res.getColor(R.color.color_ff4631, null);
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isMeasure) {
            this.canvasHeight = getHeight();
            this.canvasWidth = getWidth();
            if (bheight == 0)
                bheight = (int) (canvasHeight - marginBottom);
//            blwidh = dip2px(30);
            blwidh = 0;
            isMeasure = false;
            /*Log.d("onSizeChanged", "canvasHeight = " + canvasHeight + "canvasWidth = " + canvasWidth + "bheight = "
                    + bheight + "blwidh = " + blwidh);*/
        }
    }

    public void setColor(int m_color) {
        this.color = m_color;
    }

    /**
     * 设置光晕颜色
     *
     * @param color
     */
    public void setHaloColor(int color) {
        this.haloColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Log.d(TAG, "START mPoints LENGTH :" + mPoints.length);
//        mPaint.setColor(res.getColor(R.color.color_f2f2f2));

//        drawAllXLine(canvas);
        // 画直线（纵向）
        drawAllYLine(canvas);
        // 点的操作设置
        mPoints = getPoints();

        mPaint.setColor(color);
        mPaint.setStrokeWidth(dip2px(2.5f));
        mPaint.setStyle(Style.STROKE);
        //第一个参数是阴影扩散半径，紧接着的2个参数是阴影在X和Y方向的偏移量，最后一个参数是颜色
        if (haloColor != -1) {
            mPaint.setShadowLayer(10, 0, 0, haloColor);
        }

        if (mStyle == Linestyle.Curve) {
            drawScrollLine(canvas);
        } else {
            drawLine(canvas);
        }

        mPaint.setStyle(Style.FILL);
        for (int i = 0; i < mPoints.length; i++) {
//            canvas.drawCircle(mPoints[i].x, mPoints[i].y, CIRCLE_SIZE / 2, mPaint);
        }
        // Log.d(TAG, "END mPoints LENGTH :" + mPoints.length);
    }

    /**
     * 画所有横向表格，包括X轴
     */
    private void drawAllXLine(Canvas canvas) {
        for (int i = 0; i < spacingHeight + 1; i++) {
            canvas.drawLine(blwidh, bheight - (bheight / spacingHeight) * i + marginTop, (canvasWidth - blwidh),
                    bheight - (bheight / spacingHeight) * i + marginTop, mPaint);// Y坐标
            drawText(String.valueOf(averageValue * i), blwidh / 2, bheight - (bheight / spacingHeight) * i + marginTop,
                    canvas);
        }
    }

    /**
     * 画所有纵向表格，包括Y轴
     */
    private void drawAllYLine(Canvas canvas) {
        for (int i = 0; i < xRawDatas.size(); i++) {
//        	Log.d("drawAllYLine","blwidh = "+blwidh + "canvasWidth = " + canvasWidth + "yRawData.size() = "
//        			+yRawData.size());
            if (i == (xRawDatas.size() - 1)) {
                xList.add(canvasWidth - blwidh);
            } else {
                xList.add((int) (blwidh + (canvasWidth - blwidh) * (xRawDatas.get(i) / maxXValue)));
            }
//        	Log.d("drawAllXPoint","=================" + 
//        			(int) (blwidh + ( canvasWidth - blwidh) * ( xRawDatas.get(i) / maxXValue )));
            /*xList.add(blwidh + (canvasWidth - blwidh) / (yRawData.size() - 1) * i);
             	canvas.drawLine(blwidh + (canvasWidth - blwidh) / yRawData.size() * i, marginTop, blwidh
                    + (canvasWidth - blwidh) / yRawData.size() * i, bheight + marginTop, mPaint);
            drawText(xRawDatas.get(i), blwidh + (canvasWidth - blwidh) / yRawData.size() * i, bheight + dip2px(26),
                    canvas);// X坐标
*/
        }
    }

    private void drawScrollLine(Canvas canvas) {
        Point startp = new Point();
        Point endp = new Point();
        for (int i = 0; i < mPoints.length - 1; i++) {
            startp = mPoints[i];
            endp = mPoints[i + 1];
            int wt = (startp.x + endp.x) / 2;
            Point p3 = new Point();
            Point p4 = new Point();
            p3.y = startp.y;
            p3.x = wt;
            p4.y = endp.y;
            p4.x = wt;
            /*Log.i("drawLine", "startp.x = "+ startp.x + " startp.Y = " + startp.y +
            		"endp.x = " + endp.x +"endp.Y = " + endp.y);*/
            Path path = new Path();
            path.moveTo(startp.x, startp.y);
            path.cubicTo(p3.x, p3.y, p4.x, p4.y, endp.x, endp.y);
            canvas.drawPath(path, mPaint);
        }
    }

    private void drawLine(Canvas canvas) {
        Point startp = new Point();
        Point endp = new Point();
        for (int i = 0; i < mPoints.length - 1; i++) {
            startp = mPoints[i];
            endp = mPoints[i + 1];
            canvas.drawLine(startp.x, startp.y, endp.x, endp.y, mPaint);
        }
    }

    private void drawText(String text, int x, int y, Canvas canvas) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(dip2px(12));
//        p.setColor(res.getColor(R.color.color_999999));
        p.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(text, x, y, p);
    }

    private Point[] getPoints() {
        Point[] points = new Point[yRawData.size()];
        for (int i = 0; i < yRawData.size(); i++) {
            //Log.d("getPoints", " yRawData.get(i) " + yRawData.get(i));
            int ph = bheight - (int) (bheight * (yRawData.get(i) / maxValue));

            points[i] = new Point(xList.get(i), ph + marginTop);
        }
        return points;
    }

    /**
     * @param yRawData
     * @param xRawData
     * @param maxValue     y轴的最大值
     * @param averageValue
     * @param maxXValue
     */
    public void setData(ArrayList<Double> yRawData, ArrayList<Double> xRawData,
                        int maxValue, int averageValue, float maxXValue) {
        this.maxValue = maxValue;
        this.maxXValue = maxXValue;
        this.averageValue = averageValue;
        this.mPoints = new Point[yRawData.size()];
        this.xRawDatas = xRawData;
        this.yRawData = yRawData;
        this.spacingHeight = maxValue / averageValue;
    }

    public void setTotalvalue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setPjvalue(int averageValue) {
        this.averageValue = averageValue;
    }

    public void setMargint(int marginTop) {
        this.marginTop = marginTop;
    }

    public void setMarginb(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    public void setMstyle(Linestyle mStyle) {
        this.mStyle = mStyle;
    }

    public void setBheight(int bheight) {
        this.bheight = bheight;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        return (int) (dpValue * dm.density + 0.5f);
    }

}
