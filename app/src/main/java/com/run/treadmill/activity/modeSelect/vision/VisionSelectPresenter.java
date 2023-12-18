package com.run.treadmill.activity.modeSelect.vision;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.PlaybackParams;

import com.run.treadmill.activity.modeSelect.BaseSelectPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.manager.UserInfoManager;
import com.run.treadmill.util.Logger;

import java.util.Arrays;


public class VisionSelectPresenter extends BaseSelectPresenter<VisionSelectView> {

    void setRunParam(int targetTime) {
        RunningParam.getInstance().targetTime = targetTime * 60;

        Arrays.fill(RunningParam.getInstance().mSpeedArray, SpManager.getMinSpeed(SpManager.getIsMetric()));
        Arrays.fill(RunningParam.getInstance().mInclineArray, 0.0f);

        UserInfoManager.getInstance().setUserInfo(CTConstant.VISION, new UserDB(InitParam.DEFAULT_AGE
                , (int) (SpManager.getIsMetric() ? InitParam.DEFAULT_WEIGHT_METRIC : InitParam.DEFAULT_WEIGHT_IMPERIAL)
                , InitParam.DEFAULT_GENDER_MALE, targetTime));
    }

    int getMovieDuration(String path) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        PlaybackParams params = mediaPlayer.getPlaybackParams();
        params.setSpeed(1);
        mediaPlayer.setPlaybackParams(params);
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            mediaPlayer.release();
            return duration;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    int getMovieDuration(Context context, String path) {
        Logger.d("返回的路径是:"+path);
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (mediaPlayer.isPlaying()){
            PlaybackParams params = mediaPlayer.getPlaybackParams();
            params.setSpeed(1);
            mediaPlayer.setPlaybackParams(params);
        }
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            mediaPlayer.release();
            return duration;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}