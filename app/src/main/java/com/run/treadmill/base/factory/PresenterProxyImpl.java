package com.run.treadmill.base.factory;

import android.os.Bundle;

import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.base.BaseView;
import com.run.treadmill.util.Logger;


public class PresenterProxyImpl<V extends BaseView, P extends BasePresenter<V>> implements PresenterProxy<V, P> {
    private String TAG = "PresenterProxyImpl";

    private static final String PRESENTER_KEY = "presenter_key";
    private PresenterFactory<V, P> mPresenterFactory;
    private P mPresenter;

    private Bundle mBundle;
    private boolean mIsAttchView;

    public PresenterProxyImpl(PresenterFactory<V, P> presenterFactory) {
        mPresenterFactory = presenterFactory;
    }

    /**
     * 设置Presenter的工厂类,这个方法只能在创建Presenter之前调用,也就是调用getPresenter()之前，如果Presenter已经创建则不能再修改
     *
     * @param presenterFactory
     */
    @Override
    public void setPresenterFactory(PresenterFactory<V, P> presenterFactory) {
        if (mPresenter != null) {
            throw new IllegalArgumentException("这个方法只能在getPresenter()之前调用，如果Presenter已经创建则不能再修改!");
        }
        mPresenterFactory = presenterFactory;
    }

    @Override
    public PresenterFactory<V, P> getPresenterFactory() {
        return mPresenterFactory;
    }

    @Override
    public P getPresenter() {
//        Logger.i(TAG,"ProxyImpl getPresenter");
        if (mPresenterFactory == null) {
            throw new RuntimeException("PresenterFactory 创建失败!，检查当前activity是否有绑定presenter");
        }
        if (mPresenter == null) {
            mPresenter = mPresenterFactory.createPresenter();
            mPresenter.onCreatePresenter(mBundle == null ? null : mBundle.getBundle(PRESENTER_KEY));
        }
//        Logger.i(TAG,"ProxyImpl getPresenter = " + mPresenter);
        return mPresenter;
    }

    /**
     * 绑定Presenter和view
     *
     * @param view
     */
    public void onResume(V view) {
        getPresenter();
        // Logger.i(TAG, "ProxyImpl onResume");
        if (mPresenter != null && !mIsAttchView) {
            mPresenter.attachView(view);
            mIsAttchView = true;
        }
    }

    /**
     * 销毁Presenter持有的View
     */
    void onDeAttachView() {
        // Logger.i(TAG, "ProxyImpl onDeAttachView");
        if (mPresenter != null && mIsAttchView) {
            mPresenter.deAttachView();
            mIsAttchView = false;
        }
    }

    /**
     * 销毁Presenter
     */
    public void onDestroy() {
        // Logger.i(TAG, "ProxyImpl onDestroy");
        if (mPresenter != null) {
            onDeAttachView();
            mPresenter.onDestroyPresenter();
            mPresenter = null;
        }
    }

    /**
     * 意外销毁的时候调用
     *
     * @return 存入回调给Presenter的Bundle和当前Presenter的id
     */
    public Bundle onSaveInstanceState() {
        // Logger.i(TAG, "ProxyImpl onSaveInstanceState");
        Bundle bundle = new Bundle();
        getPresenter();
        if (mPresenter != null) {
            Bundle presenterBundle = new Bundle();
            mPresenter.onSaveInstanceState(presenterBundle);
            bundle.putBundle(PRESENTER_KEY, presenterBundle);
        }
        return bundle;
    }

    /**
     * 意外关闭恢复Presenter
     *
     * @param savedInstanceState savedInstanceState 意外关闭时存储的Bundler
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Logger.i(TAG, "ProxyImpl onRestoreInstanceState");
        Logger.i(TAG, "ProxyImpl onRestoreInstanceState Presenter = " + mPresenter);
        mBundle = savedInstanceState;
    }
}