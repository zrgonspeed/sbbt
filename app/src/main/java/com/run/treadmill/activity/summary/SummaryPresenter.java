package com.run.treadmill.activity.summary;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.common.InitParam;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/20
 */
public class SummaryPresenter extends BasePresenter<SummaryView> {

    private final float[] vo2 = {
            31.15f, 32.55f, 33.6f, 34.65f, 35.35f, 37.45f, 39.55f, 41.3f, 43.4f, 44.1f,
            45.15f, 46.2f, 46.5f, 48.6f, 50f, 51.4f, 52.8f, 53.9f, 54.9f, 56f,
            57f, 57.7f, 58.8f, 60.2f, 61.2f, 62.3f, 63.3f, 64f, 65f, 66.5f,
            68.2f, 69f, 70.7f, 72.1f, 73.1f, 73.8f, 74.9f, 76.3f, 77.7f, 79.1f,
            80f
    };

    void countVo2() {
        float score;
        int grade;

        int stageNum = RunningParam.getInstance().getRunLccurStageNum() + (30 * (RunningParam.getInstance().round - 1));
        score = vo2[stageNum];

        if (RunningParam.getInstance().curGender == InitParam.DEFAULT_GENDER_MALE) {
            grade = getMaleGrade(score);
        } else {
            grade = getFemaleGrade(score);
        }

        getView().showVo2(grade, score);
    }

    private int getMaleGrade(float score) {
        int grade;
        if (RunningParam.getInstance().curAge <= 19) {
            if (score < 35.0f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 38.4f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 45.2f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 51.0f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 55) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else if (RunningParam.getInstance().curAge <= 29) {
            if (score < 33.0f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 36.5f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 42.5f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 46.5f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 52f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else if (RunningParam.getInstance().curAge <= 39) {
            if (score < 31.5f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 35.5f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 41.0f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 45.0f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 49f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else if (RunningParam.getInstance().curAge <= 49) {
            if (score < 30.2f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 33.6f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 39.0f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 43.8f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 48f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else if (RunningParam.getInstance().curAge <= 59) {
            if (score < 26.1f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 31.0f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 35.8f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 41.0f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 45f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else {
            if (score < 20.5f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 26.1f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 32.3f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 36.5f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 44f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        }
        return grade;
    }

    private int getFemaleGrade(float score) {
        int grade;
        if (RunningParam.getInstance().curAge <= 19) {
            if (score < 25.0f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 31.0f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 35.0f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 39.0f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 41f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else if (RunningParam.getInstance().curAge <= 29) {
            if (score < 23.6f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 29.0f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 33.0f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 37.0f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 41f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else if (RunningParam.getInstance().curAge <= 39) {
            if (score < 22.8f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 27.0f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 31.5f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 35.7f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 40f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else if (RunningParam.getInstance().curAge <= 49) {
            if (score < 21.0f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 24.5f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 29.0f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 32.9f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 36f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else if (RunningParam.getInstance().curAge <= 59) {
            if (score < 20.2f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 22.8f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 27.0f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 31.5f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 35f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        } else {
            if (score < 17.5f) {
                grade = R.string.string_summary_grade_6;
            } else if (score < 20.2f) {
                grade = R.string.string_summary_grade_5;
            } else if (score < 24.5f) {
                grade = R.string.string_summary_grade_4;
            } else if (score < 30.3f) {
                grade = R.string.string_summary_grade_3;
            } else if (score <= 31.4f) {
                grade = R.string.string_summary_grade_2;
            } else {
                grade = R.string.string_summary_grade_1;
            }
        }
        return grade;
    }
}