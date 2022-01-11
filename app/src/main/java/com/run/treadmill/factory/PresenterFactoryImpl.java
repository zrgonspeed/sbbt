package com.run.treadmill.factory;

import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.base.BaseView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/05/29
 */
public class PresenterFactoryImpl<V extends BaseView, P extends BasePresenter<V>> implements PresenterFactory<V, P> {
    /**
     * 需要创建的presenter
     */
    private final Class<P> mPresenterClass;

    private PresenterFactoryImpl(Class<P> presenterClass) {
        this.mPresenterClass = presenterClass;
    }

    /**
     * 根据注解创建presenter的工厂实现类
     *
     * @param viewClass 需要创建presenter的v层实现类
     * @param <V>       当前view实现的接口类型
     * @param <P>       当前要创建的presenter类型
     * @return 工厂类
     */
    public static <V extends BaseView, P extends BasePresenter<V>> PresenterFactoryImpl<V, P> createFactory(Class<?> viewClass) {
        CreatePresenter annotation = viewClass.getAnnotation(CreatePresenter.class);
        return annotation == null ? null : new PresenterFactoryImpl<>((Class<P>) annotation.value());
    }

    @Override
    public P createPresenter() {
        try {
            return mPresenterClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Presenter创建失败!，检查是否声明了@CreatePresenter(xx.class)注解");
        }
    }
}