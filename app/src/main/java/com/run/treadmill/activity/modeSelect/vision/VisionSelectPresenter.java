package com.run.treadmill.activity.modeSelect.vision;

import android.media.MediaPlayer;

import com.run.treadmill.activity.modeSelect.BaseSelectPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.UserInfoManager;

import java.util.Arrays;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/21
 */
public class VisionSelectPresenter extends BaseSelectPresenter<VisionSelectView> {

    void setRunParam(int targetTime) {
        RunningParam.getInstance().targetTime = targetTime * 60;

        Arrays.fill(RunningParam.getInstance().mSpeedArray, SpManager.getMinSpeed(SpManager.getIsMetric()));
        Arrays.fill(RunningParam.getInstance().mInclineArray, 0.0f);

        UserInfoManager.getInstance().setUserInfo(CTConstant.VISION, new UserDB(targetTime));
    }

    int getMovieDuration(String path) {
        MediaPlayer mediaPlayer = new MediaPlayer();
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