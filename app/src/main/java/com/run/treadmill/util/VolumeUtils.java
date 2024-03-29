package com.run.treadmill.util;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;

import com.run.treadmill.Custom;
import com.run.treadmill.manager.SystemSoundManager;

public class VolumeUtils implements Custom.Volume {


    public static void changeVolume(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        ThreadUtils.runInThread(() -> {
            while (true) {
                boolean isHeadSetOn = mAudioManager.isWiredHeadsetOn();
                int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Logger.i("isHeadSetOn == " + isHeadSetOn);
                // Logger.i("volume == " + volume);

                boolean ioOutSideInsert = ioOutSideInsert();

                // ToastUtils.toast(context, "插入耳机 " + isHeadSetOn);
                // Logger.i("音源输入 " + ioOutSideInsert + "   插入耳机 " + isHeadSetOn  + "  音量 " + volume);

/*                if (ButtonUtils.canResponse2()) {
                    ThreadUtils.postOnMainThread(() -> {
                        ToastUtils.toast(context, "音源输入 " + ioOutSideInsert + "   插入耳机 " + isHeadSetOn + "  音量 " + volume);
                    });
                }*/

                if (ioOutSideInsert) {
                    // 音源输入
                    SystemSoundManager.maxVolume = inputMaxVolume;
                } else {
                    // 非音源输入
                    if (isHeadSetOn) {
                        // 插入耳机  音源输出?
                        SystemSoundManager.maxVolume = insertEarMaxVolume;
                    } else {
                        // 正常喇叭播放走这里
                        SystemSoundManager.maxVolume = normalMaxVolume;
                    }
                }

                int maxVolume = SystemSoundManager.maxVolume;
                if (volume > maxVolume) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                SystemClock.sleep(100);
            }
        });
    }

    private static boolean ioOutSideInsert() {
        // 两个硬件插口 都代表音频输入
        boolean ioOutSideInsert;
        int b3 = GpIoUtils.checkOutSideSoundState_B3();
        int b4 = GpIoUtils.checkOutSideSoundState_B4();
        if (b3 == 0 || b4 == 0) {
            ioOutSideInsert = true;
        } else {
            ioOutSideInsert = false;
        }

        return ioOutSideInsert;
    }

}
