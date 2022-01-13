package com.run.treadmill.activity.floatWindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.run.treadmill.R;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.widget.VerticalSeekBar;

public class VoiceFloatWindow {
    private Context mContext;

    private WindowManager mWindowManager;
    private FloatWindowManager mFloatWindowManager;

    private WindowManager.LayoutParams wmParams;
    private RelativeLayout mFloatWindow;
    private VerticalSeekBar float_window_seek_bar_voice;

    VoiceFloatWindow(Context context, WindowManager windowManager) {
        this.mContext = context;
        this.mWindowManager = windowManager;
    }

    void startFloat(FloatWindowManager floatWindowManager) {
        this.mFloatWindowManager = floatWindowManager;

        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mFloatWindow = createFloatWindow(mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_65_x), mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_222_y));
        floatWindowManager.addView(mFloatWindow, wmParams);
        init();
    }


    private RelativeLayout createFloatWindow(int w, int h) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.float_window_voice, null);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        RelativeLayout mWindow = (RelativeLayout) view;
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        wmParams.gravity = Gravity.START | Gravity.TOP;
        wmParams.x = mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_30_x);
        wmParams.y = mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_120_y);
        wmParams.width = w;
        wmParams.height = h;
        wmParams.windowAnimations = android.R.style.Animation_Translucent;

        return mWindow;
    }

    private void init() {
        float_window_seek_bar_voice = (VerticalSeekBar) mFloatWindow.findViewById(R.id.float_window_seek_bar_voice);
//        float_window_seek_bar_voice.setProgress(SystemSoundManager.getInstance().getCurrentPro(float_window_seek_bar_voice.getMax()));
        float_window_seek_bar_voice.setProgress(SystemSoundManager.getInstance().getCurrentPro() > float_window_seek_bar_voice.getMax()
                ? float_window_seek_bar_voice.getMax() : SystemSoundManager.getInstance().getCurrentPro());
        if (SystemSoundManager.getInstance().getCurrentPro() > float_window_seek_bar_voice.getMax()) {
            SystemSoundManager.getInstance().setAudioVolume(float_window_seek_bar_voice.getMax(), 100);
        }
        float_window_seek_bar_voice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mFloatWindow.getVisibility() == View.GONE) {
                    return;
                }
                SystemSoundManager.getInstance().setAudioVolume(progress, seekBar.getMax());
                mFloatWindowManager.currentPro = progress;
            }
        });
    }

    void setProgress(int isUp) {
        if (float_window_seek_bar_voice == null || mFloatWindow == null) {
            return;
        }
        if (mFloatWindow.getVisibility() == View.GONE) {
            if (isUp == 1) {
                if (SystemSoundManager.getInstance().getCurrentPro() < 100) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    SystemSoundManager.getInstance().setAudioVolume(SystemSoundManager.getInstance().getCurrentPro() + 1, 100);
                    mFloatWindowManager.currentPro = SystemSoundManager.getInstance().getCurrentPro();
                    float_window_seek_bar_voice.setProgress(mFloatWindowManager.currentPro);
                }
            } else {
                if (SystemSoundManager.getInstance().getCurrentPro() > 0) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    SystemSoundManager.getInstance().setAudioVolume(SystemSoundManager.getInstance().getCurrentPro() - 1, 100);
                    mFloatWindowManager.currentPro = SystemSoundManager.getInstance().getCurrentPro();
                    float_window_seek_bar_voice.setProgress(mFloatWindowManager.currentPro);
                }
            }
            return;
        }
        if (isUp == 1) {
            if (SystemSoundManager.getInstance().getCurrentPro() < 100) {
                BuzzerManager.getInstance().buzzerRingOnce();
                float_window_seek_bar_voice.setProgress(SystemSoundManager.getInstance().getCurrentPro() + 1);
            }
        } else {
            if (SystemSoundManager.getInstance().getCurrentPro() > 0) {
                BuzzerManager.getInstance().buzzerRingOnce();
                float_window_seek_bar_voice.setProgress(SystemSoundManager.getInstance().getCurrentPro() - 1);
            }
        }

    }

    public void showOrHideFloatWindow(boolean isShow) {
        if (isShow) {
            mFloatWindow.setVisibility(View.GONE);
        } else {
            mFloatWindow.setVisibility(View.VISIBLE);
        }
    }

    public void showOrHideFloatWindow() {
        if (mFloatWindow.getVisibility() == View.VISIBLE) {
            mFloatWindow.setVisibility(View.GONE);
        } else {
            mFloatWindow.setVisibility(View.VISIBLE);
        }
    }

    void stopFloat() {
        mFloatWindowManager.removeView(mFloatWindow);
    }
}
