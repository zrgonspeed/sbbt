package com.run.treadmill.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.chuhui.btcontrol.BtCallBack;
import com.chuhui.btcontrol.BtHelper;
import com.chuhui.btcontrol.CbData;
import com.lky.toucheffectsmodule.factory.TouchEffectsFactory;
import com.run.treadmill.Custom;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.base.factory.PresenterFactory;
import com.run.treadmill.base.factory.PresenterFactoryImpl;
import com.run.treadmill.base.factory.PresenterProxy;
import com.run.treadmill.base.factory.PresenterProxyImpl;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.mcu.control.ControlManager;
import com.run.treadmill.mcu.ReBootTask;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.BtHelperUtils;
import com.run.treadmill.util.Logger;

import butterknife.ButterKnife;

/**
 * @Description 继承自Activity的基类MvpActivity
 * 使用代理模式来代理Presenter的创建、销毁、绑定、解绑以及Presenter的状态保存,其实就是管理Presenter的生命周期
 * 首先在V层上定义需要创建的Presenter，声明自己模块具体的View接口类型和Presenter类型，最后实现自己View接口就ok了
 * 如果要设置自己的Presenter创建工厂，必须在调用onResume方法和getPresenter方法之前设置
 * @Author GaleLiu
 * @Time 2019/05/29
 */
public abstract class BaseActivity<V extends BaseView, P extends BasePresenter<V>> extends Activity implements
        BaseView, PresenterProxy<V, P>, BtCallBack {
    public String TAG;
    private static final String PRESENTER_SAVE_KEY = "presenter_save_key";

    private PresenterProxyImpl<V, P> mProxyImpl;

    /**
     * 公制 or 英制
     */
    public boolean isMetric = false;

    /**
     * 是否正常回到 home 界面
     */
    public static boolean isNormal = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Custom.CLICK_VIEW_ANIMATION) {
            TouchEffectsFactory.initTouchEffects(this);
        }
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        Logger.i(TAG, "onCreate()");

        hideBottomUIMenu();
        Logger.i(TAG, "onCreate() 开始 setContentView(getLayoutId())");

        setContentView(getLayoutId());
        Logger.i(TAG, "onCreate() 结束 setContentView(getLayoutId())");

        ButterKnife.bind(this);

        //创建被代理对象,传入默认Presenter的工厂
        mProxyImpl = new PresenterProxyImpl<>(PresenterFactoryImpl.<V, P>createFactory(getClass()));

        if (savedInstanceState != null) {
            mProxyImpl.onRestoreInstanceState(savedInstanceState.getBundle(PRESENTER_SAVE_KEY));
        }

        isMetric = SpManager.getIsMetric();
    }

    @Override
    protected void onResume() {
        Logger.i(TAG, "onResume()");
        long start = System.currentTimeMillis();
        {
            super.onResume();
            mProxyImpl.onResume((V) this);
            if (ReBootTask.isReBootFinish) {
                ControlManager.getInstance().regRxDataCallBack(getPresenter());
            } else {
                ReBootTask.getInstance().setPresenter(getPresenter());
            }
            FitShowManager.getInstance().setFitShowStatusCallBack(null);
            if (this.getLocalClassName().contains("HomeActivity")) {
                FitShowManager.getInstance().setNOtConnect(false);
            } else {
                FitShowManager.getInstance().setNOtConnect(true);
            }

            BtHelper.getInstance().setCallback(this);
        }
        long end = System.currentTimeMillis();
        // Logger.i("BaseActivity onResume() time == " + (end - start));
    }

    protected abstract int getLayoutId();

    @Override
    public void safeError() {
        //isNormal有防止多次跳转的作用
        if (!(this instanceof HomeActivity) && isNormal) {
            isNormal = false;
            BuzzerManager.getInstance().buzzerStop();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    @Override
    public void commOutError() {
        if (!(this instanceof HomeActivity) && isNormal) {
            isNormal = false;
            BuzzerManager.getInstance().buzzerStop();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    @Override
    public void showError(int errCode) {
        if (ErrorManager.getInstance().isInclineError()) {
            return;
        }
        if (!(this instanceof HomeActivity) && isNormal) {
            isNormal = false;
            BuzzerManager.getInstance().buzzerStop();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.i(TAG, "onPause()");
        //FitShowManager.getInstance().setFitShowStatusCallBack(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "onDestroy()");
        mProxyImpl.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(PRESENTER_SAVE_KEY, mProxyImpl.onSaveInstanceState());
    }

    @Override
    public void setPresenterFactory(PresenterFactory<V, P> presenterFactory) {
        mProxyImpl.setPresenterFactory(presenterFactory);
    }

    @Override
    public PresenterFactory<V, P> getPresenterFactory() {
        return mProxyImpl.getPresenterFactory();
    }

    @Override
    public P getPresenter() {
        return mProxyImpl.getPresenter();
    }

    private void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    /**********************************  外接蓝牙模块部分  ***********************************/
    @Override
    public void onRequestConnect() {
        BtHelperUtils.onRequestConnect();
    }

    @Override
    public void onLastConnect() {

    }

    @Override
    public void onDataCallback(CbData data) {

    }
}