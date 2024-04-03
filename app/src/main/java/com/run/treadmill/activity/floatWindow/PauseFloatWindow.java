package com.run.treadmill.activity.floatWindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.run.treadmill.R;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.util.ResourceUtils;

public class PauseFloatWindow {
    private final Context mContext;
    private final WindowManager mWindowManager;
    private FloatWindowManager mFwm;
    private WindowManager.LayoutParams wmParams;
    private RelativeLayout mFloatWindow;
    private View btn_pause_quit;
    private View btn_pause_continue;

    PauseFloatWindow(Context context, WindowManager windowManager) {
        this.mContext = context;
        this.mWindowManager = windowManager;
    }

    void startFloat(FloatWindowManager floatWindowManager) {
        this.mFwm = floatWindowManager;

        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mFloatWindow = createFloatWindow(
                ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_1920_x),
                ResourceUtils.getDimensionPixelSize(R.dimen.dp_px_1080_x)
        );
        floatWindowManager.addView(mFloatWindow, wmParams);
        init();
    }

    public void show() {
        mFloatWindow.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mFloatWindow.setVisibility(View.GONE);
    }

    private RelativeLayout createFloatWindow(int w, int h) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.run_pop_pause, null);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        RelativeLayout mWindow = (RelativeLayout) view;
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        wmParams.gravity = Gravity.START | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = w;
        wmParams.height = h;
        wmParams.windowAnimations = 0;

        // 悬浮窗默认隐藏
        mWindow.setVisibility(View.GONE);
        return mWindow;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        btn_pause_continue = mFloatWindow.findViewById(R.id.btn_pause_continue);
        btn_pause_quit = mFloatWindow.findViewById(R.id.btn_pause_quit);

        btn_pause_continue.setOnClickListener(v -> {
            resume();
        });
        btn_pause_quit.setOnClickListener(v -> {
            quitRun();
        });

    }

    public void disContinue() {
        if (btn_pause_continue.isEnabled()) {
            btn_pause_continue.setEnabled(false);
        }
    }

    public void enContinue() {
        if (!btn_pause_continue.isEnabled()) {
            btn_pause_continue.setEnabled(true);
        }
    }

    public void resume() {
        BuzzerManager.getInstance().buzzerRingOnce();
        // stopPauseTimer();
        if (mFwm.mRunningParam.isRunningEnd()) {
            return;
        }
        if (mFwm.mRunningParam.isStopStatus()) {
            hide();

            mFwm.mRunningParam.setToContinue();
            mFwm.startPrepare();
        }
    }

    private void quitRun() {
        BuzzerManager.getInstance().buzzerRingOnce();
        // stopPauseTimer();
        mFwm.goBackMyAppToSummary();
    }

    public void clickQuit() {
        quitRun();
        // performClick 无效
        // btn_pause_quit.performClick();
    }

    void stopFloat() {
        mFwm.removeView(mFloatWindow);
    }
}
