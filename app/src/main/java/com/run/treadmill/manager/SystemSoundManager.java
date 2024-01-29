package com.run.treadmill.manager;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;

import com.run.treadmill.util.Logger;


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

    public static int maxVolume = 12;
    public static int Go321Volume = 3;

    public void init(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Logger.i("系统最大音量 " + maxVolume);
        getCurrentPro(maxVolume);
    }

    // 获取记录的音量，这个值在每次设置的时候会更新
    public int getCurrentPro() {
        return currentPro;
    }

    public int getCurrentPro(int max) {
        currentPro = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * max / maxVolume;
        return currentPro;
    }

    // 设置多媒体声音大小
    public void setAudioVolume(int progress, int max) {
        if (progress > max || progress < 0) {
            return;
        }
        currentPro = progress;
        int toset = (progress * maxVolume / max);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, toset, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        if (volumeCallBack != null) {
            volumeCallBack.setText(String.valueOf(toset));
        }
    }

    private VolumeCallBack volumeCallBack;

    public void setVolumeCallBack(VolumeCallBack volumeCallBack) {
        this.volumeCallBack = volumeCallBack;
    }

    public interface VolumeCallBack {
        void setText(String volumeStr);
    }

    //关闭按键音
    public void setEffectsEnabled(int value) {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, value);
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

    public int getCurrentSystemVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public boolean isMute() {
        return getCurrentSystemVolume() == 0;
    }

    private int tempVolume = 0;

    public void setMute() {
        tempVolume = currentPro;
        setAudioVolume(0, maxVolume);
    }

    public void closeMute() {
        setAudioVolume(tempVolume, maxVolume);
    }
}
