package com.run.treadmill.activity.floatWindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.VolumeUtils;
import com.run.treadmill.widget.LeftSeekBar;

import java.lang.ref.WeakReference;

public class LeftVoiceFloatWindow {
    private final Context mContext;
    private final WindowManager mWindowManager;

    private WindowManager.LayoutParams wmParams;
    private RelativeLayout mFloatWindow;
    private LeftSeekBar float_window_seek_bar_voice;
    private TextView tv_left_volume;
    private ImageView iv_left_volume_mute;

    public LeftVoiceFloatWindow(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void init() {
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mFloatWindow = createFloatWindow(mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_498_x), mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_81_x));
        mWindowManager.addView(mFloatWindow, wmParams);

        initUI();
    }

    private RelativeLayout createFloatWindow(int w, int h) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.float_left_voice, null);
        // view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        //         View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        RelativeLayout mWindow = (RelativeLayout) view;
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        wmParams.gravity = Gravity.START | Gravity.TOP;
        wmParams.x = mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_289_x);
        wmParams.y = mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_713_x);
        wmParams.width = w;
        wmParams.height = h;
        wmParams.windowAnimations = android.R.style.Animation_Translucent;

        return mWindow;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUI() {
        tv_left_volume = mFloatWindow.findViewById(R.id.tv_left_volume);
        float_window_seek_bar_voice = mFloatWindow.findViewById(R.id.float_window_seek_bar_voice);
        iv_left_volume_mute = mFloatWindow.findViewById(R.id.iv_left_volume_mute);

        // 点击音量图标静音开关
        initVolumeMute();

        // 音量值显示监听
        initVolumeValue();

        // 进度条监听与触摸
        initSeekBar();

        myFloatHandler = new MyFloatHandler(Looper.getMainLooper(), this);
    }

    /**
     * 音量值显示监听
     */
    private void initVolumeValue() {
        SystemSoundManager.getInstance().setVolumeCallBack(volumeStr -> {
            if (mFloatWindow.getVisibility() == View.VISIBLE) {
                tv_left_volume.setText(volumeStr);
            }
        });
    }

    /**
     * 进度条监听与触摸
     */
    private void initSeekBar() {
        float_window_seek_bar_voice.setMax(SystemSoundManager.maxVolume);
        float_window_seek_bar_voice.setProgress(SystemSoundManager.getInstance().getCurrentPro());

        float_window_seek_bar_voice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // 用于标记执行1次
            private boolean flag = true;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mFloatWindow.getVisibility() == View.GONE) {
                    return;
                }
                // Object userPressed = seekBar.getTag(LeftSeekBar.KEY_USER_PRESSED);
                // if (userPressed != null && (boolean) userPressed) {
//                    Logger.e("userPressed 为true， progress == " + progress);
                SystemSoundManager.getInstance().setAudioVolume(progress, seekBar.getMax());
                // mFloatWindowManager.currentPro = progress;
                // }

                if (progress == 0) {
                    flag = false;
                    iv_left_volume_mute.setBackground(null);
                    iv_left_volume_mute.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.img_volume_off));
                } else {
                    if (flag) {
                        return;
                    }
                    flag = true;
                    iv_left_volume_mute.setBackground(null);
                    iv_left_volume_mute.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.img_volume_loud));
                }
            }
        });
        float_window_seek_bar_voice.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (myFloatHandler != null) {
                        myFloatHandler.removeMessages(HIDE_VOICE);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    senHideVoiceMessage();
                    break;
            }
            return false;
        });
    }

    /**
     * 点击音量图标静音开关
     */
    private void initVolumeMute() {
        if (SystemSoundManager.getInstance().isMute()) {
            iv_left_volume_mute.setBackground(null);
            iv_left_volume_mute.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.img_volume_off));
        }
        iv_left_volume_mute.setOnClickListener((v) -> {
            if (myFloatHandler != null) {
                myFloatHandler.removeMessages(HIDE_VOICE);
                senHideVoiceMessage();
            }

            if (SystemSoundManager.getInstance().isMute()) {
                SystemSoundManager.getInstance().closeMute();
                // 音量条要恢复
                float_window_seek_bar_voice.setProgress(SystemSoundManager.getInstance().getCurrentPro());
                iv_left_volume_mute.setBackground(null);
                iv_left_volume_mute.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.img_volume_loud));
            } else {
                SystemSoundManager.getInstance().setMute();
                float_window_seek_bar_voice.setProgress(0);
                iv_left_volume_mute.setBackground(null);
                iv_left_volume_mute.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.img_volume_off));
            }
        });
    }

    public void showOrHide(boolean isShow) {
        if (isShow) {
            mFloatWindow.setVisibility(View.GONE);
            myFloatHandler.removeMessages(HIDE_VOICE);
        } else {
            mFloatWindow.setVisibility(View.VISIBLE);
            senHideVoiceMessage();
        }
    }

    public void showOrHide() {
        if (mFloatWindow.getVisibility() == View.VISIBLE) {
            mFloatWindow.setVisibility(View.GONE);
            myFloatHandler.removeMessages(HIDE_VOICE);
        } else {
            mFloatWindow.setVisibility(View.VISIBLE);

            int currentPro = SystemSoundManager.getInstance().getCurrentPro(SystemSoundManager.maxVolume);
            float_window_seek_bar_voice.setProgress(currentPro);
            if (mFloatWindow.getVisibility() == View.VISIBLE) {
                tv_left_volume.setText(String.valueOf(currentPro));
            }

            senHideVoiceMessage();
        }
    }

    private void senHideVoiceMessage() {
        if (myFloatHandler != null) {
            myFloatHandler.removeMessages(HIDE_VOICE);
            myFloatHandler.sendEmptyMessageDelayed(HIDE_VOICE, 2000);
        }
    }

    void stopFloat() {
        myFloatHandler.removeCallbacksAndMessages(null);
        myFloatHandler = null;
        // mFloatWindowManager.removeView(mFloatWindow);
        mWindowManager.removeView(mFloatWindow);

    }

    private int tempVolume = -1;
    private int tempStatus = -1;

    public void setProgress(int status) {
        if (!VolumeUtils.canResponse()) {
            // Logger.e("!VolumeUtils.canResponse()");
            return;
        }

        if (float_window_seek_bar_voice == null || mFloatWindow == null) {
            return;
        }
        showOrHide(false);

        int pro = float_window_seek_bar_voice.getProgress();
        Logger.d("getProgress == " + pro);
        if (tempVolume == pro && tempStatus == status) {
            Logger.d("return");
            return;
        }
        tempVolume = pro;
        tempStatus = status;

        if (status == 1) {
            if (SystemSoundManager.getInstance().getCurrentPro() < SystemSoundManager.maxVolume) {
                BuzzerManager.getInstance().buzzerRingOnce();
                float_window_seek_bar_voice.setProgress(pro + 1);
            }
        } else {
            if (SystemSoundManager.getInstance().getCurrentPro() > 0) {
                BuzzerManager.getInstance().buzzerRingOnce();
                float_window_seek_bar_voice.setProgress(pro - 1);
            }
        }
    }

    private MyFloatHandler myFloatHandler;
    private final int HIDE_VOICE = 10001;

    private static class MyFloatHandler extends Handler {
        private final WeakReference<LeftVoiceFloatWindow> mWeakRefrence;
        private LeftVoiceFloatWindow vfw;

        MyFloatHandler(Looper looper, LeftVoiceFloatWindow vfw) {
            super(looper);
            mWeakRefrence = new WeakReference<>(vfw);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakRefrence == null) {
                return;
            }
            vfw = mWeakRefrence.get();
            if (msg.what == vfw.HIDE_VOICE) {
                vfw.mFloatWindow.setVisibility(View.GONE);
            }
        }
    }
}
