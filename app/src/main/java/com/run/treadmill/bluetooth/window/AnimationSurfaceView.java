package com.run.treadmill.bluetooth.window;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.run.treadmill.R;

/**
 * @Description 自定义view
 * @Author
 * @Time 2019/01/25
 */
public class AnimationSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "AnimationSurfaceView";
    private Bitmap rotateImg;

    private SurfaceHolder mSurfaceHolder;
    private Bitmap mBitmap;
    private static final long REFRESH_INTERVAL_TIME = 35l;//每间隔ms刷一帧

    private OnStausChangedListener mStausChangedListener; //动画状态改变监听事件

    private boolean isSurfaceDestoryed = true;            //默认未创建，相当于Destory
    private Thread mThread;                               //动画刷新线程
    private boolean isRunThread = false;
    private Canvas canvas = null;

    boolean isDarkBg;

    public AnimationSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AnimationSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimationSurfaceView(Context context) {
        super(context);
        init();
    }

    //初始化
    private void init() {
        rotateImg = BitmapFactory.decodeResource(getResources(), R.drawable.btn_icon_white_ground_reset_1);
        //rotateImg.setDensity(160);
        //rotateImg = Bitmap.createScaledBitmap(rotateImg, 42, 42, true);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setZOrderOnTop(true);//设置画布背景透明
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mThread = new Thread(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceDestoryed = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceDestoryed = true;
        isRunThread = false;
    }

    //执行
    private void executeAnimationStrategy() {

        Paint tempPaint = new Paint();
        /*tempPaint.setAntiAlias(true);
        tempPaint.setColor(Color.TRANSPARENT);*/

        /*Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.CYAN);*/
        if (mStausChangedListener != null) {
            mStausChangedListener.onAnimationStart(this);
        }
        int rotate = 0;

        while (isRunThread) {
            try {
                canvas = mSurfaceHolder.lockCanvas(new Rect(0, 0, rotateImg.getWidth(), rotateImg.getHeight())); //获取画布
                if (canvas == null) {
                    Thread.sleep(REFRESH_INTERVAL_TIME);
                    continue;
                }
                //Log.d(TAG, " AnimationSurfaceView isRunThread " + isRunThread);
                canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);// 设置画布的背景为透明
                //canvas.drawBitmap(rotateImg, 0, 0, paint); //绘制旋转的背景
                //创建矩阵控制图片旋转和平移
                Matrix matrix = new Matrix();
                //设置旋转角度
                matrix.postRotate(-(rotate += 20) % 360,
                        rotateImg.getWidth() / 2, rotateImg.getHeight() / 2);
                //设置左边距和上边距
                matrix.postTranslate(0, 0);
                //绘制旋转图片
                canvas.drawBitmap(rotateImg, matrix, tempPaint);

                mSurfaceHolder.unlockCanvasAndPost(canvas);
                Thread.sleep(REFRESH_INTERVAL_TIME);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // clear屏幕内容
        if (isSurfaceDestoryed == false) {// 如果直接按Home键回到桌面，这时候SurfaceView已经被销毁了，lockCanvas会返回为null。
            canvas = mSurfaceHolder.lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }

        canvas = mSurfaceHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(rotateImg, 0, 0, tempPaint);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }

        if (mStausChangedListener != null) {
            mStausChangedListener.onAnimationEnd(this);
        }
    }

    /**
     * 开始播放动画
     */
    public void startAnimation() {
        if (mThread.getState() == Thread.State.NEW) {
            isRunThread = true;
            mThread.start();
        } else if (mThread.getState() == Thread.State.TERMINATED) {
            isRunThread = true;
            mThread = new Thread(this);
            mThread.start();
        }
    }

    /**
     * 是否正在播放动画
     */
    public boolean isShowAnim() {
        return isRunThread;
    }

    /**
     * 结束动画
     */
    public void endAnimation() {
        isRunThread = false;
    }

    /*private void draw() {
        Canvas canvas = getHolder().lockCanvas(null);
        if (canvas != null) {
            doDraw(canvas);
        }
        if (canvas != null) {
            getHolder().unlockCanvasAndPost(canvas);
        }
    }*/

    /**
     * 设置要播放动画的bitmap
     *
     * @param bitmap
     */
    public void setIcon(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    /**
     * 获取要播放动画的bitmap
     */
    public Bitmap getIcon() {
        return mBitmap;
    }

    /**
     * 设置动画状态改变监听器
     */
    public void setOnAnimationStausChangedListener(OnStausChangedListener listener) {
        this.mStausChangedListener = listener;
    }

    @Override
    public void run() {
        executeAnimationStrategy();
    }

    public interface OnStausChangedListener {
        void onAnimationStart(AnimationSurfaceView view);

        void onAnimationEnd(AnimationSurfaceView view);
    }

    public void setDarkBg(boolean darkBg) {
        isDarkBg = darkBg;
        if (isDarkBg) {
            rotateImg = BitmapFactory.decodeResource(getResources(), R.drawable.btn_icon_dark_ground_reset_1);
        } else {
            rotateImg = BitmapFactory.decodeResource(getResources(), R.drawable.btn_icon_white_ground_reset_1);
        }
    }
}
