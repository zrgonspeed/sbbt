package com.run.treadmill.activity.modeSelect.program;

import com.run.treadmill.R;
import com.run.treadmill.activity.modeSelect.BaseSelectPresenter;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.common.RunModeTable;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.UserInfoManager;
import com.run.treadmill.util.UnitUtil;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/10/26
 */
public class ProgramSelectPresenter extends BaseSelectPresenter<ProgramSelectView> {

    private float mSpeedItemValueArray[] = {0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
            0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f};
    private float mInclineItemValueArray[] = {0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
            0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f};

    private Integer[] mImageIds = {R.drawable.btn_program_p01, R.drawable.btn_program_p02, R.drawable.btn_program_p03,
            R.drawable.btn_program_p04, R.drawable.btn_program_p05, R.drawable.btn_program_p06,
            R.drawable.btn_program_p07, R.drawable.btn_program_p08, R.drawable.btn_program_p09,
            R.drawable.btn_program_p10, R.drawable.btn_program_p11, R.drawable.btn_program_p12,
            R.drawable.btn_program_p13, R.drawable.btn_program_p14, R.drawable.btn_program_p15,
            R.drawable.btn_program_p16, R.drawable.btn_program_p17, R.drawable.btn_program_p18,
            R.drawable.btn_program_p19, R.drawable.btn_program_p20, R.drawable.btn_program_p21,
            R.drawable.btn_program_p22, R.drawable.btn_program_p23, R.drawable.btn_program_p24,
            R.drawable.btn_program_p25, R.drawable.btn_program_p26, R.drawable.btn_program_p27,
            R.drawable.btn_program_p28, R.drawable.btn_program_p29, R.drawable.btn_program_p30,
            R.drawable.btn_program_p31, R.drawable.btn_program_p32,};

    Integer[] getPModeImg() {
        return mImageIds;
    }

    void getPModeDate(int pMode) {
        getPModeInclineDate(pMode);
        getPModeSpeedDate(pMode);
        getView().selectPModeChangeView(mInclineItemValueArray, mSpeedItemValueArray);
    }

    void setUpRunningParam(int age, int weight, int time, int gender) {
        RunningParam.getInstance().curAge = age;
        RunningParam.getInstance().curWeight = weight;
        RunningParam.getInstance().targetTime = time;
        RunningParam.getInstance().curGender = gender;

        UserInfoManager.getInstance().setUserInfo(CTConstant.PROGRAM, new UserDB(age, weight, gender, time / 60));
    }

    private void getPModeInclineDate(int pMode) {
        System.arraycopy(RunModeTable.PModeTable[2 * pMode + 1], 0, mInclineItemValueArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
    }

    private void getPModeSpeedDate(int pMode) {
        System.arraycopy(RunModeTable.PModeTable[2 * pMode], 0, mSpeedItemValueArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
        boolean isMetric = SpManager.getIsMetric();
        if (!isMetric) {
            float maxSpeed = SpManager.getMaxSpeed(false);
            float minSpeed = SpManager.getMinSpeed(false);
            for (int i = 0; i < InitParam.TOTAL_RUN_STAGE_NUM; i++) {
                float cur = UnitUtil.getKmToMileByFloat1(mSpeedItemValueArray[i]);
                if (cur >= maxSpeed) {
                    cur = maxSpeed;
                } else if (cur <= minSpeed) {
                    cur = minSpeed;
                }
                mSpeedItemValueArray[i] = cur;
            }
        }
    }

    public void changeHistogramListView() {
        getView().selectPModeChangeView(mInclineItemValueArray, mSpeedItemValueArray);
    }
}