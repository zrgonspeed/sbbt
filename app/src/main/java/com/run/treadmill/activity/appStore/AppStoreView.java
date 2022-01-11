package com.run.treadmill.activity.appStore;

import com.run.treadmill.base.BaseView;

import java.util.List;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/17
 */
public interface AppStoreView extends BaseView {

    void initList(List<AppBean.AppInfo> apps);

    void showFailure();

    void showLoading();

    void hideLoading();
}