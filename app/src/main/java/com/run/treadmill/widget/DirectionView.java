package com.run.treadmill.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.run.treadmill.R;

import java.lang.reflect.Field;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/04
 */
public class DirectionView extends View {
    private Context mContext;

    private Paint bgPaint1, bgPaint2, bgPaint3;
    private Paint ballPaint, imgPaint;
    private Path ballPath;
    private PathMeasure mPathMeasure;
    /**
     * 用于记录点的坐标值
     */
    private float[] mCurrentPosition;
    /**
     * 用于记录点的正切值
     */
    private float[] mTan;

    /**
     * 外弧的半径
     */
    private int outArcRadius;
    /**
     * 内弧的半径
     */
    private int inArcRadius;
    /**
     * 弧的粗细
     */
    private int arcWidth;
    /**
     * 箭头缩放大小
     */
    private float arrowSize;
    /**
     * 球的大小
     */
    private int ballRadius;

    /**
     * 箭头图片
     */
//    private Bitmap arrow;
//    private Matrix arrowMatrix;

    private ValueAnimator mValueAnimator;
    /**
     * 小球x坐标
     */
    private float ballX;
    /**
     * 小球y坐标
     */
    private float ballY;
    /**
     * 最后一次方向
     */
    private boolean lastPositive;
    /**
     * 最后一次速度
     */
    private float lastSpeed;
    /**
     * 箭头角度
     */
    private int degrees;

    /**
     * 是否隐藏小球
     */
    private boolean isHideBall = true;

    public DirectionView(Context context) {
        super(context);
    }

    public DirectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DirectionView);
        inArcRadius = typedArray.getInteger(R.styleable.DirectionView_in_arc_radius, 125);
        outArcRadius = typedArray.getInteger(R.styleable.DirectionView_out_arc_radius, 150);
        arcWidth = typedArray.getInteger(R.styleable.DirectionView_arc_width, 8);
        arrowSize = typedArray.getFloat(R.styleable.DirectionView_arrow_size, 1.7f);
        ballRadius = typedArray.getInteger(R.styleable.DirectionView_ball_radius, 30);

        typedArray.recycle();

        resetAnimatorDurationScale();

        init();
    }

    /**
     * 开启系统设置的动画x1（防止用户在设置界面关闭动画）
     */
    private void resetAnimatorDurationScale() {
        try {
            Field field = ValueAnimator.class.getDeclaredField("sDurationScale");
            field.setAccessible(true);
            if (field.getFloat(null) == 0) {
                field.setFloat(null, 1);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        bgPaint1 = new Paint();
        bgPaint1.setColor(mContext.getColor(R.color.white));
        bgPaint1.setStyle(Paint.Style.STROKE);
        bgPaint1.setStrokeWidth(arcWidth);
        bgPaint1.setAntiAlias(true);
        bgPaint1.setShader(new LinearGradient(150, 50, 150, 350,
                mContext.getColor(R.color.gray), mContext.getColor(R.color.white), Shader.TileMode.MIRROR));

        bgPaint2 = new Paint();
        bgPaint2.setColor(mContext.getColor(R.color.colorAccent));
        bgPaint2.setStyle(Paint.Style.STROKE);
        bgPaint2.setStrokeWidth(arcWidth);
        bgPaint2.setAntiAlias(true);
        Shader bgShader = new SweepGradient(150, 150,
                new int[]{mContext.getColor(R.color.yellow), mContext.getColor(R.color.orange), mContext.getColor(R.color.red), mContext.getColor(R.color.pink),
                        mContext.getColor(R.color.purple), mContext.getColor(R.color.darkBlue), mContext.getColor(R.color.lightBlue), mContext.getColor(R.color.grassGreen),
                        mContext.getColor(R.color.yellow)}, new float[]{0.0f, 0.1f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.95f, 1.0f});
        bgPaint2.setShader(bgShader);

        bgPaint3 = new Paint();
        bgPaint3.setColor(mContext.getColor(R.color.transparent));
        bgPaint3.setStyle(Paint.Style.STROKE);

        ballPaint = new Paint();
        ballPaint.setColor(mContext.getColor(R.color.colorPrimaryDark));
        ballPaint.setStyle(Paint.Style.FILL);
        ballPaint.setAntiAlias(true);

        ballPath = new Path();
//        ballPath.addArc(ballRadius + 12,ballRadius + 12,outArcRadius * 2 + ballRadius - 12,outArcRadius * 2 + ballRadius - 12,
//                30,-240);
        ballPath.addCircle(ballRadius + outArcRadius, ballRadius + outArcRadius, outArcRadius - 12, Path.Direction.CCW);
        mPathMeasure = new PathMeasure(ballPath, true);
        mValueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
        mCurrentPosition = new float[2];
        mTan = new float[2];

        imgPaint = new Paint();
        imgPaint.setAntiAlias(true);

//        arrow = getImgIdToBitmap(R.drawable.img_direction);
//        arrowMatrix = new Matrix();
    }

    public DirectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 完成view的测量过程（大小）
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 确定view的位置
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制外弧
        canvas.drawArc(ballRadius, ballRadius, outArcRadius * 2 + ballRadius, outArcRadius * 2 + ballRadius,
                30, -240, false, bgPaint1);

        canvas.drawPath(ballPath, bgPaint3);

        //绘制内弧
        canvas.drawArc(outArcRadius - inArcRadius + ballRadius, outArcRadius - inArcRadius + ballRadius,
                inArcRadius * 2 + outArcRadius - inArcRadius + ballRadius, inArcRadius * 2 + outArcRadius - inArcRadius + ballRadius,
                35, -250, false, bgPaint2);

        //绘制小球
        if (!isHideBall) {
            ballPaint.setShader(new RadialGradient(ballX, ballY, ballRadius, mContext.getColor(R.color.white), mContext.getColor(R.color.green), Shader.TileMode.CLAMP));
            canvas.drawCircle(ballX, ballY, ballRadius, ballPaint);
        }

        //绘制箭头
//        arrowMatrix.setTranslate(outArcRadius + ballRadius - arrow.getWidth() / 2f,outArcRadius + ballRadius - arrow.getWidth() / 2f);
//        arrowMatrix.postRotate(degrees,outArcRadius + ballRadius,outArcRadius + ballRadius);
//        canvas.drawBitmap(arrow,arrowMatrix,imgPaint);
    }

    /**
     * 开始动画
     *
     * @param positive 旋转方向；true：顺时针；false：逆时针
     * @param speed    旋转速度
     */
    public synchronized void startPathAnim(boolean positive, float speed) {
        if (positive == lastPositive && speed == lastSpeed) {
            return;
        }

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPathMeasure.getPosTan(value, mCurrentPosition, mTan);

                //计算方位角(用于箭头的旋转)
                degrees = (int) (Math.atan2(mTan[1], mTan[0]) * 180.0 / Math.PI);
                if (-51 < degrees && degrees < 51) {
                    if (!isHideBall) {
                        isHideBall = true;
                    }
                } else if (isHideBall) {
                    isHideBall = false;
                }
                //计算变换矩阵,用于按照方位角旋转移动的那个图像(矩阵后面有篇幅讲解，这里先不管)
//                matrix.postRotate(degrees, bm_offsetX, bm_offsetY);
                //按照矩阵（包括了旋转和位移）绘制图像
//                canvas.drawBitmap(bm, matrix, null);

                setBallXY(mCurrentPosition[0], mCurrentPosition[1]);
                postInvalidate();
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        mValueAnimator.setDuration((long) (5000 / speed));
        mValueAnimator.setDuration(3000);

        if (!mValueAnimator.isStarted()) {
            mValueAnimator.start();
        } else if (lastPositive != positive) {
            mValueAnimator.reverse();
        }

        lastPositive = positive;
        lastSpeed = speed;
    }

    /**
     * @param id 图片资源id
     * @return bitmap
     */
    private Bitmap getImgIdToBitmap(int id) {
        return scaleBitmap(BitmapFactory.decodeResource(getResources(), id, null), arrowSize);
    }

    /**
     * 按比例缩放图片
     *
     * @param origin
     * @param ratio
     * @return
     */
    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBitmap = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBitmap.equals(origin)) {
            return newBitmap;
        }
        origin.recycle();
        return newBitmap;
    }

    /**
     * 设置小球的坐标
     *
     * @param ballX
     * @param ballY
     */
    public void setBallXY(float ballX, float ballY) {
        this.ballX = ballX;
        this.ballY = ballY;
        invalidate();
    }
}