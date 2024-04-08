package com.run.treadmill.activity.appStore;

import com.run.treadmill.base.BaseView;

import java.util.List;


public interface AppStoreView extends BaseView {

    void initList(List<AppBean.AppInfo> apps);

    void showFailure();

    void showLoading();

    void hideLoading();
}