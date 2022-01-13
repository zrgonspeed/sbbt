package com.run.treadmill.manager;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.SoundEffectConstants;

/**
 * 封装系统声音
 *
 * @author
 */
public class SystemSoundManager {

    private static SystemSoundManager ourInstance;
    private AudioManager mAudioManager;
    private Context mContext;

    /**
     * 记录当前音量
     */
    private int currentPro;

    private SystemSoundManager() {

    }

    public static SystemSoundManager getInstance() {
        if (null == ourInstance) {
            synchronized (SystemSoundManager.class) {
                if (null == ourInstance) {
                    ourInstance = new SystemSoundManager();
                }
            }
        }
        return ourInstance;
    }

    public void init(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        getCurrentPro(100);
    }

    // 获取多媒体声音大小(小心这个有时候很耗时间， 可能是kill mp4的时候)
    public int getCurrentPro(int max) {
        currentPro = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * max / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return currentPro;
    }

    // 设置多媒体声音大小
    public void setAudioVolume(int progress, int max) {
        if (progress > max || progress < 0) {
            return;
        }
        currentPro = progress;
        int toset = (progress * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / max);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, toset, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    //关闭按键音
    public void setEffectsEnabled(int value) {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, value);
    }

    //按键音
    public void playClickSound() {
        mAudioManager.playSoundEffect(SoundEffectConstants.CLICK);
    }

    // 设置多媒体声音大小
    public void pulsAudioVolume() {
        int curVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        curVol += 1;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    // 设置多媒体声音大小
    public void decAudioVolume() {
        int curVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        curVol -= 1;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    public void startMp3Play() {
        mAudioManager.abandonAudioFocus(null);
    }

    public void stopMp3Play() {
        if (mAudioManager.isMusicActive()) {
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    // 获取记录的音量，这个值在每次设置的时候会更新
    public int getCurrentPro() {
        return currentPro;
    }


    /**
     * 停止播放音乐
     *
     * @param mContext
     */
    public static void MusicPause(Context mContext) {
        String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
        Intent intent = new Intent();
        intent.setAction(PAUSE_ACTION);
        intent.putExtra("command", "pause");
        mContext.sendBroadcast(intent);
    }

}
