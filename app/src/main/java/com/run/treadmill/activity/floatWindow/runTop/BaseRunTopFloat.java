package com.run.treadmill.activity.floatWindow.runTop;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextClock;

import com.run.treadmill.R;
import com.run.treadmill.activity.floatWindow.FloatWindowManager;
import com.run.treadmill.manager.ErrorManager;

public abstract class BaseRunTopFloat {
    private Context mContext;

    private WindowManager mWindowManager;
    private FloatWindowManager mfwm;

    private WindowManager.LayoutParams wmParams;
    private RelativeLayout mFloatWindow;
    private RelativeLayout home_start_app_top;
    private RelativeLayout run_top;
    private TextClock tv_run_systime;

    protected BaseRunTopFloat(Context context, WindowManager windowManager) {
        this.mContext = context;
        this.mWindowManager = windowManager;
    }

    /**
     * <br>根据运动模式有可能出现界面参数不一样的情况,需要返回对应的布局(注意,不能改变基础布局的默认ID,只能新增)</br>
     */
    public abstract int layoutXml();

    private RelativeLayout createFloatWindow(int w, int h) {
        View view = LayoutInflater.from(mContext).inflate(layoutXml(), null);
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

        return mWindow;
    }

    public void startFloat(FloatWindowManager floatWindowManager) {
        this.mfwm = floatWindowManager;

        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mFloatWindow = createFloatWindow(dm.widthPixels, mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_180_x));
        floatWindowManager.addView(mFloatWindow, wmParams);

        init();
        if (ErrorManager.getInstance().isHasInclineError()) {
            mfwm.inclineError();
        }
    }

    private void init() {
        home_start_app_top = mFloatWindow.findViewById(R.id.home_start_app_top);
        run_top = mFloatWindow.findViewById(R.id.run_top);
        tv_run_systime = mFloatWindow.findViewById(R.id.tv_run_systime);
        if (mfwm.isQuickToMedia()) {
            showHomeTop();
        }
    }

    public String getInclineStr() {
        return "xxx";
    }

    public void stopFloat() {
        mfwm.removeView(mFloatWindow);
    }

    public void showOrHideFloatWindow(boolean isShow) {
        if (isShow) {
            mFloatWindow.setVisibility(View.GONE);
        } else {
            mFloatWindow.setVisibility(View.VISIBLE);
        }
    }

    public void setVoiceEnable(boolean isEnable) {
        // img_voice.setEnabled(isEnable);
    }


    public void enterPauseState() {

    }

    protected boolean curIsHomeTop() {
        return home_start_app_top.getVisibility() == View.VISIBLE;
    }

    private void showHomeTop() {
        home_start_app_top.setVisibility(View.VISIBLE);
        run_top.setVisibility(View.GONE);
        tv_run_systime.setVisibility(View.GONE);
    }

    protected void showRunTop() {
        home_start_app_top.setVisibility(View.GONE);
        run_top.setVisibility(View.VISIBLE);
        tv_run_systime.setVisibility(View.VISIBLE);
    }

    public void afterPrepare() {
        if (curIsHomeTop()) {
            mFloatWindow.postDelayed(() -> {
                showRunTop();
            }, 200);
        }
    }
}
