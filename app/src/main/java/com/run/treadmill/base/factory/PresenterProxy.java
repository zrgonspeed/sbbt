package com.run.treadmill.base.factory;

import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.base.BaseView;


public interface PresenterProxy<V extends BaseView, P extends BasePresenter<V>> {

    /**
     * 设置创建presenter的工厂
     *
     * @param presenterFactory
     */
    void setPresenterFactory(PresenterFactory<V, P> presenterFactory);

    /**
     * 获取presenter的工厂类
     *
     * @return
     */
    PresenterFactory<V, P> getPresenterFactory();

    /**
     * 获取创建的presenter
     *
     * @return
     */
    P getPresenter();
}