package com.run.treadmill.activity.factory;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.activity.factory.one.FactoryOne;
import com.run.treadmill.activity.factory.two.FactoryTwo;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.base.factory.CreatePresenter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.otamcu.OtaMcuUtils;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.PermissionUtil;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfFactory;

import butterknife.BindView;

@CreatePresenter(FactoryPresenter.class)
public class FactoryActivity extends BaseActivity<FactoryView, FactoryPresenter> implements FactoryView, View.OnClickListener {

    @BindView(R.id.rl_factory_select)
    public RelativeLayout rl_factory_select;
    @BindView(R.id.btn_factory_one)
    public ImageView btn_factory_one;
    @BindView(R.id.btn_factory_two)
    public ImageView btn_factory_two;
    @BindView(R.id.btn_home)
    public ImageView btn_home;
    @BindView(R.id.btn_logo)
    public ImageView btn_logo;

    public CalculatorOfFactory.Builder mCalculatorBuilder;
    private boolean isNoShowErr = false;
    private int num;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        factoryOne = new FactoryOne(this);
        factoryTwo = new FactoryTwo(this);

        mCalculatorBuilder = new BaseCalculator.Builder(new CalculatorOfFactory(this));

        PermissionUtil.grantPermission(this, "com.run.treadmill", Manifest.permission.REQUEST_INSTALL_PACKAGES);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SpManager.setInstallOpen(false);
        FileUtil.setLogoIcon(this, btn_logo);

        btn_factory_one.setOnClickListener(this);
        btn_factory_two.setOnClickListener(this);

        isNoShowErr = getIntent().getBooleanExtra(CTConstant.FACTORY_NO_SHOW_ERR, false);
        num = getIntent().getIntExtra(CTConstant.FACTORY_NUM, 0);
        if (num == 1) {
            btn_factory_one.performClick();
        } else if (num == 2) {
            btn_factory_two.performClick();
        }
        btn_home.setEnabled(true);
        SystemSoundManager.MusicPause(this);

        OtaMcuUtils.curIsOtamcu = false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_factory;
    }

    @Override
    public void onCalibrationAd(int max, int min) {
        factoryOne.onCalibrationAd(max, min);
    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        factoryOne.beltAndInclineStatus(beltStatus, inclineStatus, curInclineAd);
    }

    @Override
    public void onCalibrationSuccess() {
        factoryOne.onCalibrationSuccess();
    }

    @Override
    public void onCalibrationSuccessGoBackHome() {
        factoryOne.onCalibrationSuccessGoBackHome();
    }

    @Override
    public void setRpmEnable(int type) {
        factoryOne.setRpmEnable(type);
    }

    @Override
    public void safeError() {
        if (factoryTwo.isUpdatingLogo) {
            return;
        }
        factoryTwo.safeError();
        super.safeError();
    }

    @Override
    public void showError(int errCode) {
        if (isNoShowErr) {
            return;
        }
        if (ErrorManager.getInstance().isInclineError()) {
            return;
        }

        if (factoryTwo.isUpdatingLogo) {
            return;
        }

        super.showError(errCode);
        factoryOne.showError(errCode);
    }

    @Override
    public void commOutError() {
        if (isNoShowErr) {
            return;
        }
        factoryOne.commOutError();
        if (factoryTwo.isUpdatingLogo) {
            return;
        }
        startActivity(new Intent(FactoryActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    public void hideTips() {

    }

    @Override
    public void cmdKeyValue(int keyValue) {
        if (getPresenter().isRpmStart || getPresenter().isCalibrating) {
            return;
        }
        switch (keyValue) {
            case SerialKeyValue.BACK_KEY_CLICK:
                BuzzerManager.getInstance().buzzerRingOnce();
                if (factoryTwo.rl_main_two != null || factoryOne.rl_main_one != null) {
                    // 返回到选择工厂模式1、2界面
                    // 报错不给返回
                    if (isNoShowErr) {
                        return;
                    }
                    reStartActivity();
                } else {
                    // 处于选择界面
                    btn_home.performClick();
                }
                break;
            case SerialKeyValue.HOME_KEY_CLICK:
                BuzzerManager.getInstance().buzzerRingOnce();
                btn_home.performClick();
                break;
        }

    }

    private void reStartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        BuzzerManager.getInstance().buzzerRingOnce();

        switch (v.getId()) {
            case R.id.btn_factory_one:
                rl_factory_select.removeAllViews();
                LayoutInflater.from(this).inflate(R.layout.layout_factory_one_dc, rl_factory_select, true);
                factoryOne.initFactoryOne();
                break;
            case R.id.btn_factory_two:
                rl_factory_select.removeAllViews();
                LayoutInflater.from(this).inflate(R.layout.layout_factory_two, rl_factory_select, true);
                factoryTwo.initFactoryTwo();
                break;
            default:
                break;
        }
    }

    public void goHome(View view) {
        BuzzerManager.getInstance().buzzerRingOnce();
        finish();
    }

    public void selectTv(TextView tv, boolean isSelect) {
        if (mCalculatorBuilder.isPopShowing()) {
            return;
        }
        tv.setSelected(isSelect);
/*        if (isSelect) {
            tv.setTextColor(getResources().getColor(R.color.blue, null));
        } else {
            tv.setTextColor(getResources().getColor(R.color.textView_black, null));
        }*/
    }

    /**
     * 拉起数字键盘
     *
     * @param type
     * @param tv
     * @param stringId
     * @param point
     * @param callBack
     * @param view
     * @param x
     * @param y
     */
    public synchronized void showCalculator(@CTConstant.EditType int type, TextView tv, int stringId, int point, CalculatorCallBack callBack, View view, int x, int y) {
        if (mCalculatorBuilder.isPopShowing()) {
            return;
        }
        mCalculatorBuilder.reset()
                .editType(type)
                .involvedView(tv)
                .editTypeName(stringId)
                .floatPoint(point)
                .callBack(callBack)
                .mainView(view)
                .setXAndY(x, y)
                .startPopWindow();
    }


    @Override
    protected void onStop() {
        super.onStop();
        isMetric = SpManager.getIsMetric();
        if (ErrorManager.getInstance().isHasInclineError() || ErrorManager.getInstance().errStatus != ErrorManager.ERR_NO_ERROR) {
            return;
        }
        factoryTwo.onStop();
    }

    @Override
    protected void onDestroy() {
        factoryOne.onDestroy();
        super.onDestroy();
        mCalculatorBuilder.stopPopWin();

        factoryTwo.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        factoryTwo.onPause();
    }


    private FactoryOne factoryOne;
    private FactoryTwo factoryTwo;
}