package com.run.treadmill.activity.floatWindow.help;

import com.run.treadmill.Custom;
import com.run.treadmill.activity.EmptyMessageTask;
import com.run.treadmill.activity.floatWindow.FloatWindowManager;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.MsgWhat;
import com.run.treadmill.util.ThreadUtils;
import com.run.treadmill.util.VolumeUtils;

import java.util.Timer;

public class FloatWindow321Go {

    public static void startPrepare(FloatWindowManager fwm) {
        fwm.baseRunBottomFloat.disClick();

        fwm.backDotFloatWindow.disClick();
        fwm.mMediaDotFloatWindow.disClick();

        fwm.baseRunBottomFloat.stopPauseTimer();
        fwm.currentPro = SystemSoundManager.getInstance().getCurrentPro();
        //设置音量
        SystemSoundManager.getInstance().setAudioVolume(VolumeUtils.Go321Volume, SystemSoundManager.maxVolume);
        fwm.showOrHideVoiceFloatWindow(true);
        fwm.mCountdownTask = new EmptyMessageTask(fwm.myFloatHandler, MsgWhat.MSG_PREPARE_TIME);
        if (fwm.mTimer == null) {
            fwm.mTimer = new Timer();
        }
        fwm.mTimer.schedule(fwm.mCountdownTask, 1000, 1000);
    }

    public static void prepare(FloatWindowManager mFwm) {
        if (mFwm.mRunningParam.countDown == 0) {
            BuzzerManager.getInstance().buzzRingLongObliged(1000);
        } else if (mFwm.mRunningParam.countDown == -1) {
            mFwm.mRunningParam.countDown = 3;
            mFwm.cancelTime();
            // mFwm.mBaseRunCtrlFloatWindow.btn_start_stop_skip.setEnabled(false);

            mFwm.baseRunBottomFloat.afterPrepare();
            mFwm.backDotFloatWindow.remove();
            mFwm.mMediaDotFloatWindow.enClick();

            //防止go声音没结束就修改回原来的声音
            ThreadUtils.runInThread(() -> {
                //音量恢复
                SystemSoundManager.getInstance().setAudioVolume(mFwm.currentPro, SystemSoundManager.maxVolume);
            }, 1000);

            // 321GO之后，禁用1秒暂停键
            mFwm.disPauseBtn = true;
            Logger.i("disPauseBtn = true");
            ThreadUtils.postOnMainThread(() -> {
                try {
                    mFwm.disPauseBtn = false;
                    Logger.i("disPauseBtn = false");
                    mFwm.baseRunBottomFloat.enClick();

                } catch (Exception e) {
                    Logger.e("悬浮窗321go后按安全key或返回quickstart，mFwm.mBaseRunCtrlFloatWindow.btn_start_stop_skip.setEnabled(true);");
                }
            }, 1000);

            return;
        } else {
            if (Custom.DEF_DEVICE_TYPE == CTConstant.DEVICE_TYPE_DC) {
                if (mFwm.mRunningParam.countDown == 1) {
                    ControlManager.getInstance().reset();
                    ControlManager.getInstance().setSpeed(SpManager.getMinSpeed(mFwm.isMetric));
                }
            }
            BuzzerManager.getInstance().buzzRingLongObliged(200);
        }
        mFwm.mRunningParam.countDown--;

    }

}
