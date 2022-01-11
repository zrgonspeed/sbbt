package com.run.treadmill.factory;

import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.base.BaseView;

/**
 * @Description 工厂模式创建presenter
 * @Author GaleLiu
 * @Time 2019/05/29
 */
public interface PresenterFactory<V extends BaseView, P extends BasePresenter<V>> {

    /**
     * 创建presenter
     *
     * @return
     */
    P createPresenter();
}