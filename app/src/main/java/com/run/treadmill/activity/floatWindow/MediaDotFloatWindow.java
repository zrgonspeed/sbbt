package com.run.treadmill.activity.floatWindow;import android.content.Context;import android.graphics.PixelFormat;import android.view.Gravity;import android.view.LayoutInflater;import android.view.MotionEvent;import android.view.View;import android.view.WindowManager;import android.view.WindowManager.LayoutParams;import android.widget.ImageView;import android.widget.LinearLayout;import com.run.treadmill.R;import com.run.treadmill.activity.runMode.RunningParam;import com.run.treadmill.manager.BuzzerManager;import com.run.treadmill.util.clicktime.FullButtonUtils;/** * @Description 控制悬浮窗的显示和隐藏 * @Author GaleLiu * @Time 2019/06/18 */class MediaDotFloatWindow implements View.OnClickListener {    private Context mContext;    private WindowManager mWindowManager;    private FloatWindowManager mFloatWindowManager;    private LayoutParams wmParams;    private LinearLayout mFloatWindow;    private ImageView btn_media_dot;    private int lastX, lastY;    MediaDotFloatWindow(Context context, WindowManager windowManager) {        this.mContext = context;        this.mWindowManager = windowManager;    }    void startFloat(FloatWindowManager floatWindowManager) {        this.mFloatWindowManager = floatWindowManager;        mFloatWindow = createFloatWindow(mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_110_x), mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_110_y));        floatWindowManager.addView(mFloatWindow, wmParams);        btn_media_dot = (ImageView) mFloatWindow.findViewById(R.id.btn_media_dot);        btn_media_dot.setOnClickListener(this);        btn_media_dot.setOnTouchListener((v, event) -> {            switch (event.getAction()) {                case MotionEvent.ACTION_DOWN:                    lastX = (int) event.getRawX();                    lastY = (int) event.getRawY();                    break;                case MotionEvent.ACTION_MOVE:                    wmParams.x = (int) event.getRawX() - mFloatWindow.getMeasuredWidth() / 2;                    wmParams.y = (int) event.getRawY() - mFloatWindow.getMeasuredHeight() / 2;                    mWindowManager.updateViewLayout(mFloatWindow, wmParams);                    break;                case MotionEvent.ACTION_UP:                    if (event.getRawX() > mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_960_x)) {                        wmParams.x = mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_1920_x) - mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_110_x);                    } else {                        wmParams.x = 0;                    }                    mWindowManager.updateViewLayout(mFloatWindow, wmParams);                    if (Math.abs(event.getRawX() - lastX) > 120                            || Math.abs(event.getRawY() - lastY) > 120) {                        return true;                    }                    break;            }            return false;        });    }    private LinearLayout createFloatWindow(int w, int h) {        View view = LayoutInflater.from(mContext).inflate(R.layout.float_window_dot, null);        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));        LinearLayout mWindow = (LinearLayout) view;        wmParams = new LayoutParams();        wmParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;        wmParams.format = PixelFormat.RGBA_8888;        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_HARDWARE_ACCELERATED;        wmParams.gravity = Gravity.TOP | Gravity.START;        wmParams.x = mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_1920_x) - mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_110_x);        wmParams.y = (mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_1080_y) - mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_110_y)) / 2;        wmParams.width = w;        wmParams.height = h;        wmParams.windowAnimations = android.R.style.Animation_Translucent;        return mWindow;    }    void stopFloat() {        mFloatWindowManager.removeView(mFloatWindow);    }    @Override    public void onClick(View v) {        if (v.getId() == R.id.btn_media_dot) {            if (FullButtonUtils.canResponse()) {                if (RunningParam.getInstance().isPrepare()) {                    return;                }                BuzzerManager.getInstance().buzzerRingOnce();                mFloatWindowManager.showOrHideFloatWindow();            }        }    }}