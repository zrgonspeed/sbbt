package com.run.treadmill.activity.runMode.help;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.run.treadmill.Custom;
import com.run.treadmill.R;
import com.run.treadmill.activity.EmptyMessageTask;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.mcu.control.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.ActivityUtils;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.MsgWhat;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.util.thread.ThreadUtils;
import com.run.treadmill.util.VolumeUtils;

import java.lang.ref.WeakReference;
import java.util.Timer;

public class Prepare321Go {
    private BaseRunActivity activity;
    private VideoView vv_go;

    private PrepareHandler prepareHandler;
    private Timer mTimer;
    private EmptyMessageTask mCountdownTask;

    private int goTime = 1500;
    private long delay = 0;

    public void init321Go() {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        vv_go = new VideoView(MyApplication.getContext());
        vv_go.setVisibility(View.GONE);
        activity.rl_main.addView(vv_go, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        vv_go.setClickable(true);  // 防止321go时还能点击到背面
        vv_go.setOnPreparedListener(mp -> {
                    Logger.i("准备好321go的mp4 onPrepared " + mp);
                    Logger.i("delay == " + delay);
                    vv_go.setVisibility(View.VISIBLE);
                    playMusic(delay);
                }
        );
        vv_go.setOnCompletionListener(mp -> {
                    Logger.i("321go的mp4播放完成 onCompletion " + mp);
                    vv_go.setVisibility(View.GONE);
                    vv_go.stopPlayback();
                    vv_go.suspend();
                    // rl_main.removeView(vv_go);
                    disPauseBtn();
                }
        );
    }

    public void play321Go(long delay) {
        this.delay = delay;
        Logger.i("play321Go");
        String uri = "android.resource://" + ActivityUtils.getPackageName() + "/" + R.raw.go;
        vv_go.setVideoURI(Uri.parse(uri));
        vv_go.setVisibility(View.VISIBLE);
        // 真正播放视频的地方
        vv_go.start();
    }

    private void playMusic(long delay) {
        mCountdownTask = new EmptyMessageTask(prepareHandler, MsgWhat.MSG_PREPARE_TIME);
        activity.currentPro = SystemSoundManager.getInstance().getCurrentPro();
        SystemSoundManager.getInstance().setAudioVolume(VolumeUtils.Go321Volume, SystemSoundManager.maxVolume);
        try {
            mTimer.schedule(mCountdownTask, delay, goTime);
        } catch (Exception e) {
            Logger.e("异常，无法倒数。或者已经取消倒数！");
        }
    }

    private void msgDeal() {
        Logger.i("MSG_PREPARE_TIME == " + activity.mRunningParam.countDown);

        if (activity.mRunningParam.countDown == 0) {
            prepareHandler.postDelayed(() -> {
                go_0();
            }, 300);
        } else if (activity.mRunningParam.countDown == -1) {
            go_end();
            return;
        } else {
            if (activity.mRunningParam.countDown == 2 || activity.mRunningParam.countDown == 1) {
                prepareHandler.postDelayed(() -> {
                    go_start();
                }, 300);
            } else {
                go_start();
            }
        }
        activity.mRunningParam.countDown--;
    }

    private void go_start() {
        activity.btn_start_stop_skip.setEnabled(false);

        // 3 2 1
        if (Custom.DEF_DEVICE_TYPE == CTConstant.DEVICE_TYPE_DC) {
            if (activity.mRunningParam.countDown == 1) {
                ControlManager.getInstance().reset();
                ControlManager.getInstance().setSpeed(SpManager.getMinSpeed(activity.isMetric));
            }
        }
        Logger.e("buzzRingLongObliged(200)");
        BuzzerManager.getInstance().buzzRingLongObliged(200);
    }

    private void go_0() {
        Logger.e("buzzRingLongObliged(1000)");
        BuzzerManager.getInstance().buzzRingLongObliged(1000);
    }

    private void go_end() {
        // Go之后
        activity.mRunningParam.countDown = 3;
        mCountdownTask.cancel();
        activity.btn_speed_roller.setEnabled(true);
        activity.btn_incline_roller.setEnabled(!ErrorManager.getInstance().isHasInclineError());
        activity.afterPrepare();
        goEndSetViewParam();
        // mActivity.disPauseBtn();
    }

    // 321GO之后，禁用1秒暂停键
    private void disPauseBtn() {
        activity.disPauseBtn = true;
        activity.iv_pause.setEnabled(false);
        Logger.i("disPauseBtn = true");
        ThreadUtils.runInThread(() -> {
            activity.disPauseBtn = false;
            Logger.i("disPauseBtn = false");

            if (!activity.isDestroyed()) {
                activity.runOnUiThread(() -> {
                    activity.iv_pause.setEnabled(true);
                });
            }
        }, 1000);
    }

    private void goEndSetViewParam() {
        activity.tv_time.setText(activity.mRunningParam.getShowTime());
        activity.tv_distance.setText(activity.getDistanceValue(activity.mRunningParam.getShowDistance()));
        activity.tv_calories.setText(activity.mRunningParam.getShowCalories());
        activity.tv_calories.setText(StringUtil.valueAndUnit(activity.mRunningParam.getShowCalories(), activity.getString(R.string.string_unit_kcal), activity.runParamUnitTextSize));
        activity.setSpeed(activity.getSpeedValue(String.valueOf(activity.mRunningParam.getCurrSpeed())));
        if (!ErrorManager.getInstance().isHasInclineError()) {
            activity.setIncline(
                    StringUtil.valueAndUnit(String.valueOf((int) activity.mRunningParam.getCurrIncline()),
                            activity.getString(R.string.string_unit_percent),
                            activity.runParamUnitTextSize)
            );
        }

        //防止go声音没结束就修改回原来的声音
        prepareHandler.postDelayed(() -> {
            //音量恢复
            SystemSoundManager.getInstance().setAudioVolume(activity.currentPro, SystemSoundManager.maxVolume);
        }, 1000);
    }

    public void destoryVideoView() {
        vv_go.stopPlayback();
        vv_go.setOnCompletionListener(null);
        vv_go.setOnPreparedListener(null);
        vv_go = null;
    }

    public void safeError() {
        destoryTimerTask();
    }

    public void error() {
        destoryTimerTask();
    }

    public void commOutError() {
        destoryTimerTask();
    }

    public void finishRunning() {
        destoryTimerTask();

        if (prepareHandler != null) {
            prepareHandler.removeCallbacksAndMessages(null);
        }
    }

    private void destoryTimerTask() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mCountdownTask != null) {
            mCountdownTask.cancel();
        }
    }

    public void newHandler() {
        if (prepareHandler == null) {
            prepareHandler = new PrepareHandler(this);
        }
    }

    private static class PrepareHandler extends Handler {
        private WeakReference<Prepare321Go> weakReference;
        private Prepare321Go instance;

        public PrepareHandler(Prepare321Go prepare321Go) {
            weakReference = new WeakReference<>(prepare321Go);
        }

        @Override
        public void handleMessage(Message msg) {
            instance = weakReference.get();
            if (instance == null) {
                return;
            }
            switch (msg.what) {
                case MsgWhat.MSG_PREPARE_TIME:
                    instance.msgDeal();
                    break;
                default:
                    break;
            }
        }
    }

    public Prepare321Go(BaseRunActivity baseRunActivity) {
        this.activity = baseRunActivity;
    }
}
