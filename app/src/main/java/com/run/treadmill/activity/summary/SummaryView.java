package com.run.treadmill.activity.summary;

import com.run.treadmill.base.BaseView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/20
 */
public interface SummaryView extends BaseView {

    /**
     * 显示vo2成绩
     *
     * @param grade 等级
     * @param score 分数
     */
    void showVo2(int grade, float score);
}