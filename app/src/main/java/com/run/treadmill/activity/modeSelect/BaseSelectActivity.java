package com.run.treadmill.activity.modeSelect;

import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.ImageView;

import com.run.treadmill.R;
import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.widget.calculator.BaseCalculator;

import butterknife.BindView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/22
 */
public abstract class BaseSelectActivity<V extends BaseSelectView, P extends BaseSelectPresenter<V>> extends BaseActivity<V, P> {

    @BindView(R.id.btn_start)
    public ImageView btn_start;
    @BindView(R.id.btn_logo)
    ImageView btn_logo;
    @BindView(R.id.btn_home)
    ImageView btn_home;

    protected int calcX, calcY;
    protected BaseCalculator.Builder mCalcBuilder;

    /**
     * 是否已经点了start ，防止与实体按键冲突
     */
    protected boolean isOnclickStart;

    private boolean isOpenGSMode = false;
    private int curMinAD = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunningParam.reset();
        FileUtil.setLogoIcon(this, btn_logo);

        calcX = getResources().getDimensionPixelSize(R.dimen.dp_px_679_x);
        calcY = getResources().getDimensionPixelSize(R.dimen.dp_px_125_y);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSpManager();
    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        if (!SafeKeyTimer.getInstance().getIsSafe()) {
            if (btn_start.isEnabled()) {
                btn_start.setEnabled(false);
            }
            return;
        }
        if (beltStatus != 0) {
            if (btn_start.isEnabled()) {
                btn_start.setEnabled(false);
            }
            return;
        }

        //有扬升错误，不管扬升状态
        if (ErrorManager.getInstance().isHasInclineError() && isCanStart()) {
            if (!btn_start.isEnabled()) {
                btn_start.setEnabled(true);
            }
            return;
        }

        //扬升状态为0，扬升ad在最小ad的+/- 15内
        if (inclineStatus == 0 && isCanStart()) {
            if (checkADValueIsInSafe(curInclineAd)) {
                if (!btn_start.isEnabled()) {
                    btn_start.setEnabled(true);
                }
                return;
            }
        }

        if (btn_start.isEnabled()) {
            btn_start.setEnabled(false);
        }
    }

    @Override
    public void cmdKeyValue(int keyValue) {
        switch (keyValue) {
            case SerialKeyValue.START_CLICK:
                if (btn_start.isEnabled()) {
                    btn_start.performClick();
                }
                break;
            case SerialKeyValue.HOME_KEY_CLICK:
                btn_home.performClick();
                break;
        }
    }

    @Override
    public void hideTips() {
    }

    public void goHome(View view) {
        BuzzerManager.getInstance().buzzerRingOnce();
        finish();
    }

    /**
     * 是否可以开始
     *
     * @return
     */
    protected abstract boolean isCanStart();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
            mCalcBuilder.stopPopWin();
        }
    }

    private void loadSpManager() {
        isOpenGSMode = SpManager.getGSMode();
        curMinAD = SpManager.getMinAd();
    }

    /**
     * 检测当前AD值是否在正常范围
     *
     * @param curAD
     * @return
     */
    private boolean checkADValueIsInSafe(int curAD) {
        if (isOpenGSMode) {
            return true;
        }
        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            return (Math.abs(curAD - curMinAD) < InitParam.ABS_AC_AD);

        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            return (Math.abs(curAD - curMinAD) < InitParam.ABS_AA_AD);

        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            return (Math.abs(curAD - curMinAD) < InitParam.ABS_DC_AD);

        } else {
            return (Math.abs(curAD - curMinAD) < InitParam.ABS_AC_AD);
        }
    }
}