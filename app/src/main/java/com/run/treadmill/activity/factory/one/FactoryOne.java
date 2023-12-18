package com.run.treadmill.activity.factory.one;

import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.factory.FactoryActivity;
import com.run.treadmill.activity.factory.FactoryPresenter;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.widget.LongClickImage;
import com.run.treadmill.widget.calculator.CalculatorCallBack;

public class FactoryOne implements View.OnClickListener, CalculatorCallBack {
    protected FactoryActivity activity;

    public RelativeLayout rl_main_one;

    private TextView tip_mark;
    private RadioGroup rg_metric;
    private View include_calibrate;
    private ImageView btn_calibrate;
    private TextView edit_max_speed, edit_min_speed, edit_max_incline, tv_minspeed_unit, tv_maxspeed_unit;
    private ImageView img_loading;
    private TextView tv_ad_max, tv_ad_min;
    private TextView edit_rpm;
    private ImageView btn_rpm_start_stop;
    private LongClickImage btn_rpm_up, btn_rpm_down;

    private int curRPM;

    /**
     * 是否在校正速度
     */
    private boolean isCalcRpm = false;

    public FactoryOne(FactoryActivity factoryActivity) {
        this.activity = factoryActivity;
    }

    public void initFactoryOne() {
        rl_main_one = (RelativeLayout) findViewById(R.id.rl_main_one);
        include_calibrate = findViewById(R.id.include_calibrate);
        btn_calibrate = (ImageView) findViewById(R.id.btn_calibrate);
        edit_max_speed = (TextView) findViewById(R.id.edit_max_speed);
        edit_min_speed = (TextView) findViewById(R.id.edit_min_speed);
        edit_max_incline = (TextView) findViewById(R.id.edit_max_incline);
        tv_minspeed_unit = (TextView) findViewById(R.id.tv_minspeed_unit);
        tv_maxspeed_unit = (TextView) findViewById(R.id.tv_maxspeed_unit);
        rg_metric = (RadioGroup) findViewById(R.id.rg_metric);
        tip_mark = (TextView) findViewById(R.id.tip_mark);

        edit_rpm = (TextView) findViewById(R.id.edit_rpm);
        btn_rpm_up = (LongClickImage) findViewById(R.id.btn_rpm_up);
        btn_rpm_down = (LongClickImage) findViewById(R.id.btn_rpm_down);
        btn_rpm_start_stop = (ImageView) findViewById(R.id.btn_rpm_start_stop);

        tv_minspeed_unit.setText(getString(activity.isMetric ? R.string.string_unit_kmh : R.string.string_unit_mileh));
        tv_maxspeed_unit.setText(getString(activity.isMetric ? R.string.string_unit_kmh : R.string.string_unit_mileh));
        edit_max_speed.setText(String.valueOf(SpManager.getMaxSpeed(activity.isMetric)));
        edit_min_speed.setText(String.valueOf(SpManager.getMinSpeed(activity.isMetric)));
        rg_metric.check(activity.isMetric ? R.id.rb_metric : R.id.rb_imperial);
        rg_metric.setOnCheckedChangeListener((group, checkedId) -> {
            BuzzerManager.getInstance().buzzerRingOnce();
            if (checkedId == R.id.rb_metric) {
                edit_max_speed.setText(String.valueOf(SpManager.getMaxSpeed(true)));
                edit_min_speed.setText(String.valueOf(SpManager.getMinSpeed(true)));
                tv_minspeed_unit.setText(getString(R.string.string_unit_kmh));
                tv_maxspeed_unit.setText(getString(R.string.string_unit_kmh));
                //暂时的公英制，最后还需根据情况设置
//                SpManager.setIsMetric(true);
                activity.isMetric = true;
                activity.mCalculatorBuilder.setMetric(true);
            } else if (checkedId == R.id.rb_imperial) {
                edit_max_speed.setText(String.valueOf(SpManager.getMaxSpeed(false)));
                edit_min_speed.setText(String.valueOf(SpManager.getMinSpeed(false)));
                tv_minspeed_unit.setText(getString(R.string.string_unit_mileh));
                tv_maxspeed_unit.setText(getString(R.string.string_unit_mileh));
                //暂时的公英制，最后还需根据情况设置
//                SpManager.setIsMetric(false);
                activity.isMetric = false;
                activity.mCalculatorBuilder.setMetric(false);
            }
        });

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            isCalcRpm = false;
            curRPM = SpManager.getRpmRate();
            curRPM = getPresenter().changeRPM(curRPM, 0);
            edit_rpm.setText(curRPM + "");
        }

        edit_max_incline.setText(String.valueOf(SpManager.getMaxIncline()));

        btn_rpm_up.setTag(-1);
        btn_rpm_down.setTag(-1);

        edit_max_speed.setOnClickListener(this);
        edit_min_speed.setOnClickListener(this);
        edit_max_incline.setOnClickListener(this);
        btn_calibrate.setOnClickListener(this);

        edit_rpm.setOnClickListener(this);
        btn_rpm_up.setOnClickListener(this);
        btn_rpm_down.setOnClickListener(this);
        btn_rpm_start_stop.setOnClickListener(this);

        btn_calibrate.setEnabled(false);
        btn_rpm_start_stop.setEnabled(false);

        ControlManager.getInstance().setIncline(0);
    }

    @Override
    public void onClick(View v) {
        BuzzerManager.getInstance().buzzerRingOnce();
        if (v.getId() == R.id.edit_max_speed || v.getId() == R.id.edit_min_speed
                || v.getId() == R.id.edit_max_incline
                || v.getId() == R.id.edit_rpm) {
            tip_mark.setVisibility(View.VISIBLE);
        }

        switch (v.getId()) {
            case R.id.btn_rpm_up:
                curRPM = getPresenter().changeRPM(curRPM, 1);
                edit_rpm.setText(curRPM + "");
                break;
            case R.id.btn_rpm_down:
                curRPM = getPresenter().changeRPM(curRPM, -1);
                edit_rpm.setText(curRPM + "");
                break;
            case R.id.btn_rpm_start_stop:
                getPresenter().isRpmStart = !getPresenter().isRpmStart;
                if (getPresenter().isRpmStart) {
                    isCalcRpm = true;
                    btn_calibrate.setEnabled(false);
                    activity.btn_home.setEnabled(false);
                    ControlManager.getInstance().startRun();
                    ControlManager.getInstance().calibrateSpeedByRpm(1.0f, curRPM);
                } else {
                    isCalcRpm = false;
                    activity.btn_home.setEnabled(true);
                    SpManager.setRpmRate(curRPM);
                    ControlManager.getInstance().stopRun(SpManager.getGSMode());
                }
                break;
            case R.id.edit_rpm:
                selectTv(edit_rpm, true);
                showCalculator(CTConstant.TYPE_FACTORY_RPM, edit_rpm, R.string.string_keybord_rpm, 0, this, rl_main_one,
                        getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                break;
            case R.id.btn_calibrate:
                setRpmEnable(2);
                inflateLoading();
                if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                    ControlManager.getInstance().calibrate();
                }
                break;
            case R.id.edit_max_speed:
                selectTv(edit_max_speed, true);
                showCalculator(CTConstant.TYPE_FACTORY_HIGH_SPEED, edit_max_speed, R.string.string_keybord_max_speed, 1, this, rl_main_one,
                        getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                break;
            case R.id.edit_min_speed:
                selectTv(edit_min_speed, true);
                showCalculator(CTConstant.TYPE_FACTORY_LOW_SPEED, edit_min_speed, R.string.string_keybord_min_speed, 1, this, rl_main_one,
                        getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                break;
            case R.id.edit_max_incline:
                selectTv(edit_max_incline, true);
                showCalculator(CTConstant.TYPE_FACTORY_MAX_INCLINE, edit_max_incline, R.string.string_keybord_max_incline, 0, this, rl_main_one,
                        getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                break;
            default:
                break;
        }
    }

    public void setRpmEnable(int type) {
        if (type == 1) {
            btn_rpm_up.setEnabled(false);
            btn_rpm_down.setEnabled(true);
        } else if (type == -1) {
            btn_rpm_up.setEnabled(true);
            btn_rpm_down.setEnabled(false);
        } else if (type == 0) {
            btn_rpm_up.setEnabled(true);
            btn_rpm_down.setEnabled(true);
        } else if (type == 2) {
            btn_rpm_up.setEnabled(false);
            btn_rpm_up.setEnabled(false);
            btn_rpm_start_stop.setEnabled(false);
        }
    }

    public void inflateLoading() {
        include_calibrate.setVisibility(View.VISIBLE);

        img_loading = (ImageView) findViewById(R.id.img_loading);
        tv_ad_max = (TextView) findViewById(R.id.tv_ad_max);
        tv_ad_min = (TextView) findViewById(R.id.tv_ad_min);

        tv_ad_max.setText(String.valueOf(SpManager.getMaxAd()));
        tv_ad_min.setText(String.valueOf(SpManager.getMinAd()));

        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.rotate);
        animation.setInterpolator(new LinearInterpolator());
        img_loading.startAnimation(animation);

        activity.btn_home.setEnabled(false);
    }

    private FactoryPresenter getPresenter() {
        return activity.getPresenter();
    }

    private Resources getResources() {
        return activity.getResources();
    }

    public void selectTv(TextView tv, boolean isSelect) {
        activity.selectTv(tv, isSelect);
    }

    public synchronized void showCalculator(@CTConstant.EditType int type, TextView tv, int stringId, int point, CalculatorCallBack callBack, View view, int x, int y) {
        activity.showCalculator(type, tv, stringId, point, callBack, view, x, y);
    }

    @Override
    public void enterCallBack(int type, String value) {
        if (type == CTConstant.TYPE_FACTORY_RPM) {
            curRPM = getPresenter().changeRPM(Integer.valueOf(value), 0);
            edit_rpm.setText(curRPM + "");
        }
    }

    @Override
    public void onCalculatorDismiss() {
        if (rl_main_one != null) {
            if (edit_max_speed.isSelected()) {
                selectTv(edit_max_speed, false);
            } else if (edit_min_speed.isSelected()) {
                selectTv(edit_min_speed, false);
            } else if (edit_max_incline.isSelected()) {
                selectTv(edit_max_incline, false);
            } else if (edit_rpm.isSelected()) {
                selectTv(edit_rpm, false);
            }
            if (tip_mark.getVisibility() == View.VISIBLE) {
                tip_mark.setVisibility(View.GONE);
            }
        }
    }

    public void isFitShowConnect(boolean isConnect) {
        if (isConnect && include_calibrate.getVisibility() == View.GONE) {
            activity.finish();
        }
    }

    public void onDestroy() {
        getPresenter().stopGetAd();
    }

    public void onCalibrationSuccessGoBackHome() {
        ControlManager.getInstance().emergencyStop();
        img_loading.clearAnimation();
        activity.startActivity(new Intent(activity, HomeActivity.class));
        activity.finish();
    }

    public void showError(int errCode) {
        if (include_calibrate != null && include_calibrate.getVisibility() == View.VISIBLE) {
            img_loading.clearAnimation();
        }
    }

    public void commOutError() {
        if (include_calibrate != null && include_calibrate.getVisibility() == View.VISIBLE) {
            include_calibrate.setVisibility(View.GONE);
            img_loading.clearAnimation();
        }
    }

    private boolean isSetMaxAd = false;//防止没有上升过程

    public void onCalibrationAd(int max, int min) {
        if (include_calibrate.getVisibility() == View.GONE) {
            return;
        }
        if (-1 != max && Integer.parseInt(tv_ad_max.getText().toString()) != max) {
            isSetMaxAd = true;
            tv_ad_max.setText(String.valueOf(max));
        }
        if (-1 != min && Integer.parseInt(tv_ad_min.getText().toString()) != min) {
            if (!isSetMaxAd) {
                isSetMaxAd = true;
                tv_ad_max.setText(String.valueOf(min));
            }
            tv_ad_min.setText(String.valueOf(min));
        }
    }

    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        if (btn_calibrate == null) {
            return;
        }
        if (!SafeKeyTimer.getInstance().getIsSafe()) {
            if (btn_calibrate.isEnabled()) {
                btn_calibrate.setEnabled(false);
            }
            if (btn_rpm_start_stop.isEnabled()) {
                btn_rpm_start_stop.setEnabled(false);
            }
            return;
        }
        //跑带状态
        if (beltStatus != 0) {
            if (btn_calibrate.isEnabled()) {
                btn_calibrate.setEnabled(false);
            }
            if (!isCalcRpm && btn_rpm_start_stop.isEnabled()) {
                btn_rpm_start_stop.setEnabled(false);
            }
            return;
        }

        //有扬升错误，不管扬升状态
        if (ErrorManager.getInstance().isHasInclineError()) {
            if (isCalcRpm) {
                btn_calibrate.setEnabled(false);
            }
            if (!isCalcRpm && !btn_calibrate.isEnabled()) {
                btn_calibrate.setEnabled(true);
            }
            if (!btn_rpm_start_stop.isEnabled()) {
                btn_rpm_start_stop.setEnabled(true);
            }
            return;
        }

        //扬升状态为0，扬升ad在最小ad的+/- 15内
        if (inclineStatus == 0) {
            if (isCalcRpm && btn_calibrate.isEnabled()) {
                btn_calibrate.setEnabled(false);
            }
            if (!isCalcRpm && !btn_calibrate.isEnabled()) {
                btn_calibrate.setEnabled(true);
            }
            if (!btn_rpm_start_stop.isEnabled()) {
                btn_rpm_start_stop.setEnabled(true);
            }
            return;
        }

        if (btn_calibrate.isEnabled()) {
            btn_calibrate.setEnabled(false);
        }
        if (btn_rpm_start_stop.isEnabled()) {
            btn_rpm_start_stop.setEnabled(false);
        }
    }

    public void onCalibrationSuccess() {
        int checkedRadioButtonId = rg_metric.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.rb_metric) {
            SpManager.setIsMetric(true);
//            isMetric = true;
            ControlManager.getInstance().setMetric(true);
        } else if (checkedRadioButtonId == R.id.rb_imperial) {
            SpManager.setIsMetric(false);
//            isMetric = false;
            ControlManager.getInstance().setMetric(false);
        }
        SpManager.setMaxSpeed(Float.valueOf(edit_max_speed.getText().toString()), SpManager.getIsMetric());
        SpManager.setMinSpeed(Float.valueOf(edit_min_speed.getText().toString()), SpManager.getIsMetric());

        SpManager.setMaxIncline(Integer.valueOf(edit_max_incline.getText().toString()));
        SpManager.resetRunTotalTime(0L);
        SpManager.resetRunTotalDis(0f);

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            //内缩+/-10
            SpManager.setMaxAd(Integer.valueOf(tv_ad_max.getText().toString()) - 2);
            SpManager.setMinAd(Integer.valueOf(tv_ad_min.getText().toString()) + 2);
        }

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            getPresenter().setParam();
        }
    }


    private View findViewById(int id) {
        return activity.findViewById(id);
    }

    private String getString(int id) {
        return activity.getString(id);
    }
}
