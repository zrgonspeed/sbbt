package com.run.treadmill.activity.factory;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.run.android.ShellCmdUtils;
import com.run.serial.OTAParam;
import com.run.treadmill.R;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.base.MyApplication;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.http.OkHttpHelper;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.ota.BaseUpdate;
import com.run.treadmill.ota.BinUpdate;
import com.run.treadmill.ota.Md5Manager;
import com.run.treadmill.ota.OTAUtils;
import com.run.treadmill.ota.ReBinUpdate;
import com.run.treadmill.receiver.USBBroadcastReceiver;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.PermissionUtil;
import com.run.treadmill.util.TimeStringUtil;
import com.run.treadmill.util.UnitUtil;
import com.run.treadmill.util.VersionUtil;
import com.run.treadmill.widget.LongClickImage;
import com.run.treadmill.widget.MultiClickAndLongPressView;
import com.run.treadmill.widget.ViewDialog;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfFactory;

import java.io.File;
import java.lang.ref.SoftReference;

import butterknife.BindView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/24
 */
@CreatePresenter(FactoryPresenter.class)
public class FactoryActivity extends BaseActivity<FactoryView, FactoryPresenter> implements CustomTimer.TimerCallBack, FactoryView, View.OnClickListener, CalculatorCallBack, USBBroadcastReceiver.OnUSBCallBack, CompoundButton.OnCheckedChangeListener {

    private String mUdiskPath = ""; // a133的u盘路径不同u盘不一样;
    private static final int SD_ERROR = 40001;

    @BindView(R.id.rl_factory_select)
    public RelativeLayout rl_factory_select;
    @BindView(R.id.btn_factory_one)
    public ImageView btn_factory_one;
    @BindView(R.id.btn_factory_two)
    public ImageView btn_factory_two;
    @BindView(R.id.btn_home)
    public ImageView btn_home;
    @BindView(R.id.btn_logo)
    ImageView btn_logo;

    private RelativeLayout rl_main_one;
    private TextView tip_mark;
    private RadioGroup rg_metric;
    private View include_calibrate;
    private ImageView btn_calibrate;
    private TextView tv_wheel_size;
    private TextView edit_max_speed, edit_min_speed, edit_wheel_size, edit_max_incline, tv_minspeed_unit, tv_maxspeed_unit;
    private ImageView img_loading;
    private TextView tv_ad_max, tv_ad_min;

    private TextView tv_max_incline_per;
    private TextView tv_incline_cal;
    private TextView tv_max_incline;


    private TextView edit_rpm;
    private TextView tv_rpm;
    private ImageView btn_rpm_start_stop;
    private LongClickImage btn_rpm_up, btn_rpm_down;

    private RelativeLayout rl_main_two;
    private View[] views = new View[4];
    private RadioButton[] viewsLeft = new RadioButton[4];
    private int lastItemInx;

    private ToggleButton tb_sleep, tb_display, tb_incline, tb_touch, tb_buzzer, tb_login_ctrl;
    private TextView tv_lube;
    private ImageView btn_setting_reset;

    private TextView tv_total_time, tv_total_distance, tv_sdk, tv_firmware, tv_soffware, tv_ncu;
    private ImageView btn_info_reset;

    private ImageView btn_factory_update;

    private ImageView img_old_logo, img_new_logo, btn_update_logo;
    /**
     * 是否正在更新logo，此时不理任何东西
     */
    private boolean isUpdatingLogo;

    private CalculatorOfFactory.Builder mCalculatorBuilder;

    private View errView;
    private ImageView img_err_bk;
    private TextView tv_err;

    public ViewDialog mResetDialog;
    private ImageView btn_pop_yes, btn_pop_no;

    private boolean oldGSMode;

    private USBBroadcastReceiver mUsbBroadcastReceiver;
    private FactoryHandler mFactoryHandler;

    private boolean isOpenGSMode = false;
    private int curMinAD = 0;

    private boolean isNoShowErr = false;
    private int num;

    // ota apk
    private final String hintTag = "alterUpdatePath";
    private final long HINT_TIME = 3;
    private MultiClickAndLongPressView btn_alter_update_path;
    private CustomTimer mHintTimer;
    private ConstraintLayout cl_hint;
    private TextView tv_hint;

    // ota mcu
    private MultiClickAndLongPressView btn_ota_update;
    private RelativeLayout rl_ota_update, rl_error_tip;
    private ImageView btn_update_pop_yes, btn_update_pop_no, btn_ok;
    private TextView tv_error_tip;
    private BaseUpdate binUpdate;

    private int curRPM;

    /**
     * 是否在校正速度
     */
    private boolean isCalcRpm = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCalculatorBuilder = new BaseCalculator.Builder(new CalculatorOfFactory(this));

        PermissionUtil.grantPermission(this, "com.run.treadmill", Manifest.permission.REQUEST_INSTALL_PACKAGES);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSpManager();
        mFactoryHandler = new FactoryHandler(this);
        FileUtil.setLogoIcon(this, btn_logo);

        mUdiskPath = FileUtil.getStoragePath(this, true);
//        mUdiskPath = "/storage/emulated/0/ota";

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
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_factory;
    }

    private void setViewTop(View view, int dimenId) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.topMargin = (int) getResources().getDimension(dimenId);
        view.setLayoutParams(layoutParams);
        view.invalidate();
    }

    private void initFactoryOne() {
        rl_main_one = (RelativeLayout) findViewById(R.id.rl_main_one);
        include_calibrate = findViewById(R.id.include_calibrate);
        btn_calibrate = (ImageView) findViewById(R.id.btn_calibrate);
        edit_max_speed = (TextView) findViewById(R.id.edit_max_speed);
        edit_min_speed = (TextView) findViewById(R.id.edit_min_speed);
        tv_wheel_size = (TextView) findViewById(R.id.tv_wheel_size);
        edit_wheel_size = (TextView) findViewById(R.id.edit_wheel_size);
        edit_max_incline = (TextView) findViewById(R.id.edit_max_incline);
        tv_minspeed_unit = (TextView) findViewById(R.id.tv_minspeed_unit);
        tv_maxspeed_unit = (TextView) findViewById(R.id.tv_maxspeed_unit);
        rg_metric = (RadioGroup) findViewById(R.id.rg_metric);
        tip_mark = (TextView) findViewById(R.id.tip_mark);

        tv_max_incline_per = (TextView) findViewById(R.id.tv_max_incline_per);
        tv_incline_cal = (TextView) findViewById(R.id.tv_incline_cal);
        tv_max_incline = (TextView) findViewById(R.id.tv_max_incline);

        tv_rpm = (TextView) findViewById(R.id.tv_rpm);
        edit_rpm = (TextView) findViewById(R.id.edit_rpm);
        btn_rpm_up = (LongClickImage) findViewById(R.id.btn_rpm_up);
        btn_rpm_down = (LongClickImage) findViewById(R.id.btn_rpm_down);
        btn_rpm_start_stop = (ImageView) findViewById(R.id.btn_rpm_start_stop);

        if (MyApplication.DEFAULT_DEVICE_TYPE == CTConstant.DEVICE_TYPE_DC) {
            tv_rpm.setVisibility(View.GONE);
            edit_rpm.setVisibility(View.GONE);
            btn_rpm_up.setVisibility(View.GONE);
            btn_rpm_down.setVisibility(View.GONE);
            btn_rpm_start_stop.setVisibility(View.GONE);
            setViewTop(tv_incline_cal, R.dimen.dp_px_362_y);
            setViewTop(tv_max_incline_per, R.dimen.dp_px_494_y);
            setViewTop(tv_max_incline, R.dimen.dp_px_494_y);
            setViewTop(edit_max_incline, R.dimen.dp_px_468_y);
            setViewTop(btn_calibrate, R.dimen.dp_px_713_y);
        } else if (MyApplication.DEFAULT_DEVICE_TYPE == CTConstant.DEVICE_TYPE_AA) {
            tv_rpm.setVisibility(View.VISIBLE);
            edit_rpm.setVisibility(View.VISIBLE);
            btn_rpm_up.setVisibility(View.VISIBLE);
            btn_rpm_down.setVisibility(View.VISIBLE);
            btn_rpm_start_stop.setVisibility(View.VISIBLE);
            setViewTop(tv_incline_cal, R.dimen.dp_px_135_y);
            setViewTop(tv_max_incline_per, R.dimen.dp_px_228_y);
            setViewTop(tv_max_incline, R.dimen.dp_px_228_y);
            setViewTop(edit_max_incline, R.dimen.dp_px_218_y);
            setViewTop(btn_calibrate, R.dimen.dp_px_300_y);
        }

        tv_minspeed_unit.setText(getString(isMetric ? R.string.string_unit_kmh : R.string.string_unit_mileh));
        tv_maxspeed_unit.setText(getString(isMetric ? R.string.string_unit_kmh : R.string.string_unit_mileh));
        edit_max_speed.setText(String.valueOf(SpManager.getMaxSpeed(isMetric)));
        edit_min_speed.setText(String.valueOf(SpManager.getMinSpeed(isMetric)));
        rg_metric.check(isMetric ? R.id.rb_metric : R.id.rb_imperial);
        rg_metric.setOnCheckedChangeListener((group, checkedId) -> {
            BuzzerManager.getInstance().buzzerRingOnce();
            if (checkedId == R.id.rb_metric) {
                edit_max_speed.setText(String.valueOf(SpManager.getMaxSpeed(true)));
                edit_min_speed.setText(String.valueOf(SpManager.getMinSpeed(true)));
                tv_minspeed_unit.setText(getString(R.string.string_unit_kmh));
                tv_maxspeed_unit.setText(getString(R.string.string_unit_kmh));
                //暂时的公英制，最后还需根据情况设置
//                SpManager.setIsMetric(true);
                isMetric = true;
                mCalculatorBuilder.setMetric(true);
            } else if (checkedId == R.id.rb_imperial) {
                edit_max_speed.setText(String.valueOf(SpManager.getMaxSpeed(false)));
                edit_min_speed.setText(String.valueOf(SpManager.getMinSpeed(false)));
                tv_minspeed_unit.setText(getString(R.string.string_unit_mileh));
                tv_maxspeed_unit.setText(getString(R.string.string_unit_mileh));
                //暂时的公英制，最后还需根据情况设置
//                SpManager.setIsMetric(false);
                isMetric = false;
                mCalculatorBuilder.setMetric(false);
            }
        });

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            tv_wheel_size.setText(R.string.string_factory_speed_rate);
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {

        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            tv_wheel_size.setText(R.string.string_factory_wheel);
        }

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            edit_wheel_size.setText(String.valueOf(SpManager.getSpeedRate()));

        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            isCalcRpm = false;
            curRPM = SpManager.getRpmRate();
            curRPM = getPresenter().changeRPM(curRPM, 0);
            edit_rpm.setText(curRPM + "");

        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            edit_wheel_size.setText(String.valueOf(SpManager.getWheelSize()));

        }
        edit_max_incline.setText(String.valueOf(SpManager.getMaxIncline()));

        btn_rpm_up.setTag(-1);
        btn_rpm_down.setTag(-1);

        edit_max_speed.setOnClickListener(this);
        edit_min_speed.setOnClickListener(this);
        edit_wheel_size.setOnClickListener(this);
        edit_max_incline.setOnClickListener(this);
        btn_calibrate.setOnClickListener(this);

        edit_rpm.setOnClickListener(this);
        btn_rpm_up.setOnClickListener(this);
        btn_rpm_down.setOnClickListener(this);
        btn_rpm_start_stop.setOnClickListener(this);

        btn_calibrate.setEnabled(false);
        btn_rpm_start_stop.setEnabled(false);
    }

    private void inflateLoading() {
        include_calibrate.setVisibility(View.VISIBLE);

        img_loading = (ImageView) findViewById(R.id.img_loading);
        tv_ad_max = (TextView) findViewById(R.id.tv_ad_max);
        tv_ad_min = (TextView) findViewById(R.id.tv_ad_min);

        tv_ad_max.setText(String.valueOf(SpManager.getMaxAd()));
        tv_ad_min.setText(String.valueOf(SpManager.getMinAd()));

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        animation.setInterpolator(new LinearInterpolator());
        img_loading.startAnimation(animation);

        btn_home.setEnabled(false);
    }

    private void initFactoryTwo() {
        oldGSMode = SpManager.getGSMode();
        rl_main_two = (RelativeLayout) findViewById(R.id.rl_main_two);
        RadioGroup rg_factory = (RadioGroup) findViewById(R.id.rg_factory);
        views[0] = findViewById(R.id.layout_setting);
        views[1] = findViewById(R.id.layout_information);
        views[2] = findViewById(R.id.layout_update);
        views[3] = findViewById(R.id.layout_logo);

        viewsLeft[0] = rl_main_two.findViewById(R.id.rb_setting);
        viewsLeft[1] = rl_main_two.findViewById(R.id.rb_information);
        viewsLeft[2] = rl_main_two.findViewById(R.id.rb_update);
        viewsLeft[3] = rl_main_two.findViewById(R.id.rb_logo);

        tb_sleep = (ToggleButton) views[0].findViewById(R.id.tb_sleep);
        tb_display = (ToggleButton) views[0].findViewById(R.id.tb_display);
        tb_incline = (ToggleButton) views[0].findViewById(R.id.tb_incline);
        tb_touch = (ToggleButton) views[0].findViewById(R.id.tb_touch);
        tb_buzzer = (ToggleButton) views[0].findViewById(R.id.tb_buzzer);
        tb_login_ctrl = (ToggleButton) views[0].findViewById(R.id.tb_login_ctrl);
        tv_lube = (TextView) views[0].findViewById(R.id.tv_lube);
        btn_setting_reset = (ImageView) views[0].findViewById(R.id.btn_setting_reset);

        tv_total_time = (TextView) views[1].findViewById(R.id.tv_total_time);
        tv_total_distance = (TextView) views[1].findViewById(R.id.tv_total_distance);
        tv_sdk = (TextView) views[1].findViewById(R.id.tv_sdk);
        tv_firmware = (TextView) views[1].findViewById(R.id.tv_firmware);
        tv_soffware = (TextView) views[1].findViewById(R.id.tv_soffware);
        tv_ncu = (TextView) views[1].findViewById(R.id.tv_ncu);
        btn_info_reset = (ImageView) views[1].findViewById(R.id.btn_info_reset);

        btn_factory_update = (ImageView) views[2].findViewById(R.id.btn_factory_update);
        img_old_logo = (ImageView) views[3].findViewById(R.id.img_old_logo);
        img_new_logo = (ImageView) views[3].findViewById(R.id.img_new_logo);
        btn_update_logo = (ImageView) views[3].findViewById(R.id.btn_update_logo);

        SpManager.setDisplay(readTouchesOptions());
        tb_touch.setChecked(SpManager.getDisplay());

        initNeedResetSetting();
        // 服务器切换
        btn_alter_update_path = (MultiClickAndLongPressView) views[2].findViewById(R.id.btn_alter_update_path);
        cl_hint = (ConstraintLayout) views[2].findViewById(R.id.cl_hint);
        tv_hint = (TextView) views[2].findViewById(R.id.tv_hint);
        btn_alter_update_path.setOnMultiClickListener(new MultiClickAndLongPressView.OnMultiClickListener() {
            @Override
            public void onMultiClick() {
                if (!SpManager.getAlterUpdatePath()) {
                    SpManager.setAlterUpdatePath(true);
                    SpManager.setChangedServer(true);
                    OkHttpHelper.cancel("HomeActivity");
                }

                tv_hint.setText(getString(R.string.server_hint) + "\n" + InitParam.getUpdateHost(FactoryActivity.this));
                cl_hint.setVisibility(View.VISIBLE);
                startTimerOfHint();

            }
        });

        // OTA
        rl_ota_update = (RelativeLayout) views[2].findViewById(R.id.rl_ota_update);
        rl_error_tip = (RelativeLayout) views[2].findViewById(R.id.rl_error_tip);
        btn_ota_update = (MultiClickAndLongPressView) views[2].findViewById(R.id.btn_ota_update);
        btn_ok = (ImageView) views[2].findViewById(R.id.btn_ok);
        tv_error_tip = (TextView) views[2].findViewById(R.id.tv_error_tip);
        btn_update_pop_yes = (ImageView) views[2].findViewById(R.id.btn_update_pop_yes);
        btn_update_pop_no = (ImageView) views[2].findViewById(R.id.btn_update_pop_no);
        btn_ota_update.setOnMultiClickListener(new MultiClickAndLongPressView.OnMultiClickListener() {
            @Override
            public void onMultiClick() {
                Logger.i("mUdiskPath == " + mUdiskPath);
                if (mUdiskPath.isEmpty() || !OTAUtils.checkOtaFileExist(mUdiskPath)) {
                    tv_error_tip.setText(getString(R.string.no__update_files));
                    rl_error_tip.setVisibility(View.VISIBLE);
                } else {
                    rl_ota_update.setVisibility(View.VISIBLE);
                }
            }
        });

        btn_update_pop_yes.setOnClickListener(this);
        btn_update_pop_no.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        btn_setting_reset.setOnClickListener(this);
        btn_info_reset.setOnClickListener(this);
        btn_factory_update.setOnClickListener(this);
        btn_update_logo.setOnClickListener(this);
        tb_sleep.setOnCheckedChangeListener(this);
        tb_display.setOnCheckedChangeListener(this);
        tb_incline.setOnCheckedChangeListener(this);
        tb_touch.setOnCheckedChangeListener(this);
        tb_buzzer.setOnCheckedChangeListener(this);
        tb_login_ctrl.setOnCheckedChangeListener(this);

        initNeedResetInfo();
        tv_sdk.setText(VersionUtil.getSdkVersion());
        tv_firmware.setText(VersionUtil.getFireWareVersion_2());
        tv_soffware.setText(InitParam.PROJECT_NAME + "\n" + VersionUtil.getAppVersionName(this));
        tv_ncu.setText(InitParam.PROJECT_NAME + "\n" + SpManager.getNcuVer());
        btn_factory_update.setEnabled(FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.APK_NAME));
        btn_update_logo.setEnabled(FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.LOGO_NAME));
        FileUtil.setLogoIcon(this, img_old_logo);
        if (FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.LOGO_NAME)) {
            img_new_logo.setImageBitmap(FileUtil.imgFileTOBitmap(mUdiskPath + "/" + InitParam.LOGO_NAME));
        }
        for (RadioButton radioButton : viewsLeft) {
            radioButton.setOnClickListener((v) -> {
                BuzzerManager.getInstance().buzzerRingOnce();
            });
        }


        // 左侧按钮选中事件
        rg_factory.setOnCheckedChangeListener((group, checkedId) -> {
            if (mCalculatorBuilder != null && mCalculatorBuilder.isPopShowing()) {
                mCalculatorBuilder.stopPopWin();
            }

            views[lastItemInx].setVisibility(View.GONE);
            for (int i = 0; i < viewsLeft.length; i++) {
                if (checkedId == viewsLeft[i].getId()) {
                    views[i].setVisibility(View.VISIBLE);
                    lastItemInx = i;
                }
            }
        });
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
        rg_factory.check(viewsLeft[lastItemInx].getId());

        // 注册U盘拔插广播
        regisBroadcastReceiver();
    }

    private void initNeedResetSetting() {
        tb_sleep.setChecked(SpManager.getSleep());
        tb_incline.setChecked(false);
        tb_incline.setEnabled(false);
        tb_buzzer.setChecked(SpManager.getBuzzer());
        tv_lube.setText(String.format("%s %s"
                , UnitUtil.getFloatToIntClear(SpManager.getMaxLubeDis() - SpManager.getRunLubeDis() <= 0 ? 0 : SpManager.getMaxLubeDis() - SpManager.getRunLubeDis())
                , getString(isMetric ? R.string.string_unit_km : R.string.string_unit_mile)));
    }

    private void initNeedResetInfo() {
        tv_total_time.setText(TimeStringUtil.getSecToHrMin(SpManager.getRunTotalTime()));
//        if (isMetric) {
//            tv_total_distance.setText(UnitUtil.getFloatToInt(SpManager.getRunTotalDis()) + " " + getString(R.string.string_unit_km));
//        } else {
//            tv_total_distance.setText(UnitUtil.getFloatToInt(UnitUtil.getKmToMileByFloat1(SpManager.getRunTotalDis())) + " " + getString(R.string.string_unit_mile));
//        }
        tv_total_distance.setText(UnitUtil.getFloatToInt(SpManager.getRunTotalDis()) + " " + getString(isMetric ? R.string.string_unit_km : R.string.string_unit_mile));
    }

    private void showErrPop(RelativeLayout rl_main) {
        if (ErrorManager.getInstance().errStatus != ErrorManager.ERR_INCLINE_CALIBRATE) {
            return;
        }
        if (errView == null) {
            errView = View.inflate(this, R.layout.layout_error, null);
            img_err_bk = (ImageView) errView.findViewById(R.id.img_err_bk);
            tv_err = (TextView) errView.findViewById(R.id.tv_err);
        }
        tv_err.setTextColor(getResources().getColor(R.color.textView_black, null));
        tv_err.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size_36pt));
        img_err_bk.setVisibility(View.VISIBLE);
        tv_err.setText(R.string.string_calibration_failure);
        rl_main.addView(errView);
    }

    private boolean isSetMaxAd = false;//防止没有上升过程

    @Override
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

    @Override
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

    @Override
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

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            SpManager.setSpeedRate(Float.valueOf(edit_wheel_size.getText().toString()));
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {

        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            SpManager.setWheelSize(Float.valueOf(edit_wheel_size.getText().toString()));
        }

        SpManager.setMaxIncline(Integer.valueOf(edit_max_incline.getText().toString()));
        SpManager.resetRunTotalTime(0L);
        SpManager.resetRunTotalDis(0f);

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            //内缩+/-10
            SpManager.setMaxAd(Integer.valueOf(tv_ad_max.getText().toString()) - 10);
            SpManager.setMinAd(Integer.valueOf(tv_ad_min.getText().toString()) + 10);
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            //内缩+/-10
            SpManager.setMaxAd(Integer.valueOf(tv_ad_max.getText().toString()) - 2);
            SpManager.setMinAd(Integer.valueOf(tv_ad_min.getText().toString()) + 2);
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
            SpManager.setMaxAd(Integer.valueOf(tv_ad_max.getText().toString()));
            SpManager.setMinAd(Integer.valueOf(tv_ad_min.getText().toString()));
        }

        if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
            getPresenter().setParam();
        } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
            getPresenter().setParam();
        }
    }

    @Override
    public void onCalibrationSuccessGoBackHome() {
        ControlManager.getInstance().emergencyStop();
        img_loading.clearAnimation();
        startActivity(new Intent(FactoryActivity.this, HomeActivity.class));
        finish();
    }

    @Override
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

    @Override
    public void safeError() {
        if (isUpdatingLogo) {
            return;
        }
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

        if (isUpdatingLogo) {
            return;
        }

        super.showError(errCode);
        if (include_calibrate != null && include_calibrate.getVisibility() == View.VISIBLE) {
            img_loading.clearAnimation();
        }
//        if (rl_main_one != null) {
//            showErrPop(rl_main_one);
//        }
    }

    @Override
    public void commOutError() {
        if (isNoShowErr) {
            return;
        }
        if (include_calibrate != null && include_calibrate.getVisibility() == View.VISIBLE) {
            include_calibrate.setVisibility(View.GONE);
            img_loading.clearAnimation();
        }
        if (isUpdatingLogo) {
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
            case SerialKeyValue.HOME_KEY_CLICK:
                BuzzerManager.getInstance().buzzerRingOnce();
                btn_home.performClick();
                break;
        }

    }

    @Override
    public void onClick(View v) {
        if (isUpdatingLogo) {
            return;
        }
        BuzzerManager.getInstance().buzzerRingOnce();
        if (v.getId() == R.id.edit_max_speed || v.getId() == R.id.edit_min_speed || v.getId() == R.id.edit_wheel_size
                || v.getId() == R.id.edit_max_incline
                || v.getId() == R.id.edit_rpm) {
            tip_mark.setVisibility(View.VISIBLE);
        }
        switch (v.getId()) {
            case R.id.btn_rpm_up:
                longClickBuzzer(btn_rpm_up);
                curRPM = getPresenter().changeRPM(curRPM, 1);
                edit_rpm.setText(curRPM + "");
                break;
            case R.id.btn_rpm_down:
                longClickBuzzer(btn_rpm_down);
                curRPM = getPresenter().changeRPM(curRPM, -1);
                edit_rpm.setText(curRPM + "");
                break;
            case R.id.btn_rpm_start_stop:
                getPresenter().isRpmStart = !getPresenter().isRpmStart;
                if (getPresenter().isRpmStart) {
                    isCalcRpm = true;
                    btn_calibrate.setEnabled(false);
                    btn_home.setEnabled(false);
                    ControlManager.getInstance().startRun();
                    ControlManager.getInstance().calibrateSpeedByRpm(1.0f, curRPM);
                } else {
                    isCalcRpm = false;
                    btn_home.setEnabled(true);
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
                if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                    if (checkCalcView()) {
                        break;
                    }
                }
                inflateLoading();
                int checkedRadioButtonId = rg_metric.getCheckedRadioButtonId();
                if ((ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC)
                        || (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC)) {
                    getPresenter().calibrate(checkedRadioButtonId == R.id.rb_metric,
                            Float.valueOf(edit_max_speed.getText().toString()),
                            Float.valueOf(edit_min_speed.getText().toString()),
                            Float.valueOf(edit_wheel_size.getText().toString()),
                            Integer.valueOf(edit_max_incline.getText().toString()));
                } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                    getPresenter().calibrate(checkedRadioButtonId == R.id.rb_metric,
                            Float.valueOf(edit_max_speed.getText().toString()),
                            Float.valueOf(edit_min_speed.getText().toString()),
                            0,
                            Integer.valueOf(edit_max_incline.getText().toString()));
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
            case R.id.edit_wheel_size:
                selectTv(edit_wheel_size, true);
                if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
                    showCalculator(CTConstant.TYPE_FACTORY_SPEED_RATE, edit_wheel_size, R.string.string_keybord_speed_rate, 1, this, rl_main_one,
                            getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                    showCalculator(CTConstant.TYPE_FACTORY_SPEED_RATE, edit_wheel_size, R.string.string_keybord_speed_rate, 1, this, rl_main_one,
                            getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                    showCalculator(CTConstant.TYPE_FACTORY_WHEEL_SIZE, edit_wheel_size, R.string.string_keybord_wheel_size, 2, this, rl_main_one,
                            getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                }
                break;
            case R.id.edit_max_incline:
                selectTv(edit_max_incline, true);
                showCalculator(CTConstant.TYPE_FACTORY_MAX_INCLINE, edit_max_incline, R.string.string_keybord_max_incline, 0, this, rl_main_one,
                        getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                break;
            case R.id.tv_lube:
                selectTv(tv_lube, true);
                showCalculator(CTConstant.TYPE_FACTORY_LUBE, tv_lube, R.string.string_keybord_lube, 0, this, rl_main_two,
                        getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                break;
            case R.id.btn_factory_update:
                if (FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.APK_NAME)) {
                    try {
                        SpManager.setInstallOpen(false);
                        SpManager.setUpdateIsNetwork(false);
                        FileUtil.installApk(this, new File(mUdiskPath + "/" + InitParam.APK_NAME));
                    } catch (Exception e) {
                        mFactoryHandler.sendEmptyMessage(SD_ERROR);
                    }
                }
                break;
            case R.id.btn_update_logo:
                isUpdatingLogo = true;
                if (FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.LOGO_NAME)) {
                    try {
                        FileUtil.CopySdcardFile(this, mUdiskPath + "/" + InitParam.LOGO_NAME,
                                getFilesDir() + "/" + InitParam.LOGO_NAME);
                        SpManager.setIsInnerLogo(false);
                        FileUtil.setLogoIcon(this, img_old_logo);
                        FileUtil.setLogoIcon(this, btn_logo);
                    } catch (Exception e) {
                        mFactoryHandler.sendEmptyMessage(SD_ERROR);
                    }
                }
                isUpdatingLogo = false;
                break;
            case R.id.btn_setting_reset:
                createResetDialog(v1 -> {
                    BuzzerManager.getInstance().buzzerRingOnce();
//                    SpManager.setSleep(true);
//                    SpManager.setDisplay(true);
//                    SpManager.setGSMode(true);
//                    SpManager.setBuzzer(true);
//                    SpManager.setLoginCtrl(true);
//                    SpManager.setMaxLubeDis(InitParam.MAX_LUBE_DISTANCE);
                    mResetDialog.dismiss();
//                    initNeedResetSetting();

                    tv_lube.setSelected(true);
                    showCalculator(CTConstant.TYPE_FACTORY_LUBE, tv_lube, R.string.string_keybord_lube, 0, this, rl_main_two,
                            getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                });
                break;
            case R.id.btn_info_reset:
                createResetDialog(v1 -> {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    SpManager.resetRunTotalTime(0L);
                    SpManager.resetRunTotalDis(0f);
                    mResetDialog.dismiss();
                    initNeedResetInfo();
                });
                break;
            case R.id.btn_factory_one:
                rl_factory_select.removeAllViews();
                LayoutInflater.from(this).inflate(R.layout.layout_factory_one, rl_factory_select, true);
                initFactoryOne();
                break;
            case R.id.btn_factory_two:
                rl_factory_select.removeAllViews();
                LayoutInflater.from(this).inflate(R.layout.layout_factory_two, rl_factory_select, true);
                initFactoryTwo();
                break;
            case R.id.btn_update_pop_yes:
                // 从多个bin文件中选择日期最新的bin文件进行拷贝。
                boolean result = OTAUtils.copyUpanToAn(mUdiskPath, getFilesDir() + "/OTA");
                if (!result) {
                    tv_error_tip.setText(getString(R.string.no__update_files));
                    rl_error_tip.setVisibility(View.VISIBLE);
                    rl_ota_update.setVisibility(View.GONE);
                    return;
                }
                if (OTAParam.isSendBinCnt) {
                    rl_ota_update.setVisibility(View.GONE);
                    return;
                }

                // 获取复制到Android的bin文件的MD5
                File anOtaDir = new File(getFilesDir() + "/OTA");
                // 保证安卓OTA目录只有一个文件
                File otaFile = anOtaDir.listFiles()[0];
                String otaFileName = otaFile.getName();
                // 从文件名取MD5部分
                String MD5FromFileName = otaFileName.substring(InitParam.PROJECT_NAME.length() + 12, otaFileName.indexOf("."));

                String anOtaFilePath = getFilesDir() + "/OTA/" + otaFileName;
                if (mUdiskPath.isEmpty() || !FileUtil.isCheckExist(anOtaFilePath)) {
                    tv_error_tip.setText(getString(R.string.no__update_files));
                    rl_error_tip.setVisibility(View.VISIBLE);
                    rl_ota_update.setVisibility(View.GONE);
                } else {
                    String apkMD5 = Md5Manager.fileToMD5(anOtaFilePath);
                    Logger.d("比对MD5: apkMD5==" + apkMD5 + "  MD5FromFileName==" + MD5FromFileName);

                    if (apkMD5.compareTo(MD5FromFileName) == 0) {
                        if (binUpdate == null) {
                            Logger.d("准备更新 " + otaFileName);

                            if (ErrorManager.getInstance().errStatus == CTConstant.SHOW_TIPS_COMM_ERROR || ErrorManager.getInstance().errStatus == ErrorManager.ERR_TIME_OUT) {
                                Logger.e("通信超时，使用 ReBinUpdate");
                                binUpdate = new ReBinUpdate();
                            } else {
                                Logger.e("使用 BinUpdate");
                                binUpdate = new BinUpdate();
                            }

                            binUpdate.procBin(this, anOtaFilePath);
                            btn_update_pop_yes.setVisibility(View.GONE);
                            btn_update_pop_no.setVisibility(View.GONE);
                        }
                    } else {
                        tv_error_tip.setText(getString(R.string.incorrect_signature));
                        rl_error_tip.setVisibility(View.VISIBLE);
                        rl_ota_update.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.btn_update_pop_no:
                rl_ota_update.setVisibility(View.GONE);
                break;
            case R.id.btn_ok:
                rl_error_tip.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private boolean checkCalcView() {
        int curIncline = Integer.valueOf(edit_max_incline.getText().toString());
        float cutMaxSpeed = Float.valueOf(edit_max_speed.getText().toString());
        float curMinSpeed = Float.valueOf(edit_min_speed.getText().toString());
        float curWheel = Float.valueOf(edit_wheel_size.getText().toString());
        boolean hasChange = false;

        if (curIncline > InitParam.MAX_INCLINE_MAX || curIncline < InitParam.MAX_INCLINE_MIN) {
            curIncline = InitParam.DEFAULT_MAX_INCLINE;
            hasChange = true;
        }

        if (isMetric) {
            if (cutMaxSpeed > InitParam.MAX_SPEED_MAX_METRIC || cutMaxSpeed < InitParam.MAX_SPEED_MIN_METRIC) {
                cutMaxSpeed = InitParam.DEFAULT_MAX_SPEED_METRIC;
                hasChange = true;
            }
            if (curMinSpeed > InitParam.MIN_SPEED_MAX_METRIC || curMinSpeed < InitParam.MIN_SPEED_MIN_METRIC) {
                curMinSpeed = InitParam.DEFAULT_MIN_SPEED_METRIC;
                hasChange = true;
            }

        } else {
            if (cutMaxSpeed > InitParam.MAX_SPEED_MAX_IMPERIAL || cutMaxSpeed < InitParam.MAX_SPEED_MIN_IMPERIAL) {
                cutMaxSpeed = InitParam.DEFAULT_MAX_SPEED_IMPERIAL;
                hasChange = true;
            }
            if (curMinSpeed > InitParam.MIN_SPEED_MAX_IMPERIAL || curMinSpeed < InitParam.MIN_SPEED_MIN_IMPERIAL) {
                curMinSpeed = InitParam.DEFAULT_MIN_SPEED_IMPERIAL;
                hasChange = true;
            }
        }
        if (curWheel > InitParam.MAX_WHEEL_SIZE || curWheel < InitParam.MIN_WHEEL_SIZE) {
            curWheel = InitParam.DEFAULT_WHEEL_SIZE;
            hasChange = true;
        }

        edit_max_incline.setText(curIncline + "");
        edit_max_speed.setText(cutMaxSpeed + "");
        edit_min_speed.setText(curMinSpeed + "");
        edit_wheel_size.setText(curWheel + "");
        return hasChange;

    }

    public void goHome(View view) {
        BuzzerManager.getInstance().buzzerRingOnce();
        finish();
    }

    @Override
    public void usbStatus(boolean isConnection, String udiskPath) {
        if (!isConnection) {
            if (btn_factory_update.isEnabled()) {
                mUdiskPath = "";
                btn_factory_update.setEnabled(false);
            }
            if (btn_update_logo.isEnabled()) {
                img_new_logo.setImageDrawable(null);
                btn_update_logo.setEnabled(false);
            }
            return;
        }

        if (!udiskPath.isEmpty()) {
            mUdiskPath = udiskPath;
        }
        Logger.d("u 盘路径==== >" + mUdiskPath);
//        if (!FileUtil.isCheckExist(FileUtil.getUdiskPath(this, mUdiskPath))) {
        if (!FileUtil.isCheckExist(mUdiskPath)) {
            if (btn_factory_update.isEnabled()) {
                btn_factory_update.setEnabled(false);
            }
            if (btn_update_logo.isEnabled()) {
                btn_update_logo.setEnabled(false);
            }
            return;
        }
        Logger.d("u 盘 文件 路径==== >" + mUdiskPath + "/" + InitParam.APK_NAME);
//        if (FileUtil.isCheckExist(FileUtil.getUdiskPath(this, mUdiskPath) + InitParam.APK_NAME)) {
        if (FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.APK_NAME)) {
            if (!btn_factory_update.isEnabled()) {
                btn_factory_update.setEnabled(true);
            }
        }
//        if (FileUtil.isCheckExist(FileUtil.getUdiskPath(this, mUdiskPath) + InitParam.LOGO_NAME)) {
        if (FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.LOGO_NAME)) {
            img_new_logo.setImageBitmap(FileUtil.imgFileTOBitmap(mUdiskPath + "/" + InitParam.LOGO_NAME));
            if (!btn_update_logo.isEnabled()) {
                btn_update_logo.setEnabled(true);
            }
        }
    }

    private void regisBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);

        intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);

        intentFilter.addDataScheme("file");
        mUsbBroadcastReceiver = new USBBroadcastReceiver();
        mUsbBroadcastReceiver.setUSBCallBack(this);
        registerReceiver(mUsbBroadcastReceiver, intentFilter);
    }

    /**
     * 显示pop
     *
     * @param listener 点击yes的处理事件
     */
    private void createResetDialog(View.OnClickListener listener) {
        if (mResetDialog == null) {
            mResetDialog = new ViewDialog.Builder(this)
                    .setView(getLayoutInflater().inflate(R.layout.layout_yes_no_dialog, rl_main_two, false))
                    .create();
            mResetDialog.setCanceledOnTouchOutside(true);
            btn_pop_yes = mResetDialog.findViewById(R.id.btn_pop_yes);
            btn_pop_no = mResetDialog.findViewById(R.id.btn_pop_no);
            btn_pop_no.setOnClickListener(v -> {
                BuzzerManager.getInstance().buzzerRingOnce();
                mResetDialog.dismiss();
            });
        }
        btn_pop_yes.setOnClickListener(listener);
        mResetDialog.show();
    }

    private void selectTv(TextView tv, boolean isSelect) {
        if (mCalculatorBuilder.isPopShowing()) {
            return;
        }
        tv.setSelected(isSelect);
        if (isSelect) {
            tv.setTextColor(getResources().getColor(R.color.blue, null));
        } else {
            tv.setTextColor(getResources().getColor(R.color.textView_black, null));
        }
    }

    protected void longClickBuzzer(LongClickImage btn) {
        if ((Integer) btn.getTag() != 1) {
            BuzzerManager.getInstance().buzzerRingOnce();
        } else {
            btn.setTag(-1);
        }
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
    private synchronized void showCalculator(@CTConstant.EditType int type, TextView tv, int stringId, int point, CalculatorCallBack callBack, View view, int x, int y) {
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
    public void enterCallBack(int type, String value) {
        if (type == CTConstant.TYPE_FACTORY_LUBE) {
            tv_lube.setText(String.format("%s %s", value, isMetric ? getString(R.string.string_unit_km) : getString(R.string.string_unit_mile)));
            SpManager.setMaxLubeDis(Integer.valueOf(value));
            SpManager.reSetRunLubeDis(0f);
        }
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
            } else if (edit_wheel_size.isSelected()) {
                selectTv(edit_wheel_size, false);
            } else if (edit_max_incline.isSelected()) {
                selectTv(edit_max_incline, false);
            } else if (edit_rpm.isSelected()) {
                selectTv(edit_rpm, false);
            }
            if (tip_mark.getVisibility() == View.VISIBLE) {
                tip_mark.setVisibility(View.GONE);
            }
        }
        if (rl_main_two != null) {
            if (tv_lube.isSelected()) {
                tv_lube.setSelected(false);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isMetric = SpManager.getIsMetric();
        if (ErrorManager.getInstance().isHasInclineError() || ErrorManager.getInstance().errStatus != ErrorManager.ERR_NO_ERROR) {
            return;
        }
        if (SpManager.getGSMode() != oldGSMode) {
            if (oldGSMode) {
                ControlManager.getInstance().reset();
                ControlManager.getInstance().setIncline(0.0f);
            } else {
                ControlManager.getInstance().stopIncline();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (rl_main_one != null) {
            getPresenter().stopGetAd();
        }
        super.onDestroy();
        if (mUsbBroadcastReceiver != null) {
            unregisterReceiver(mUsbBroadcastReceiver);
            mUsbBroadcastReceiver = null;
        }
        mCalculatorBuilder.stopPopWin();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        BuzzerManager.getInstance().buzzerRingOnce();
        switch (buttonView.getId()) {
            case R.id.tb_sleep:
                SpManager.setSleep(isChecked);
                break;
//            case R.id.tb_display:
//                SpManager.setDisplay(isChecked);
//                break;
            case R.id.tb_incline:
                SpManager.setGSMode(isChecked);
                break;
            case R.id.tb_touch:
                SpManager.setDisplay(isChecked);
                if (isChecked) {
                    writeShowTouchesOptions(1);
                } else {
                    writeShowTouchesOptions(0);
                }
                break;
            case R.id.tb_buzzer:
                SpManager.setBuzzer(isChecked);
                BuzzerManager.getInstance().setBuzzerEnable(isChecked);
                if (BuzzerManager.getInstance().getBuzzerType() == BuzzerManager.BUZZER_SYSTEM) {
                    SystemSoundManager.getInstance().setEffectsEnabled(isChecked ? 1 : 0);
                }
                break;
//            case R.id.tb_login_ctrl:
//                SpManager.setLoginCtrl(isChecked);
//                break;
            default:
                break;
        }
    }

    private void loadSpManager() {
        SpManager.setInstallOpen(false);
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
        return true;
    }

    private synchronized void writeShowTouchesOptions(final int param) {
        new Thread() {
            @Override
            public void run() {
                ShellCmdUtils.getInstance().execCommand("settings put system pointer_location " + param);
            }
        }.start();

    }

    private boolean readTouchesOptions() {
        return Settings.System.getInt(getContentResolver(), "pointer_location", 0) != 0;
    }

    static class FactoryHandler extends Handler {
        private SoftReference<FactoryActivity> mSoftReference;
        private FactoryActivity mFactoryActivity;

        FactoryHandler(FactoryActivity activity) {
            mSoftReference = new SoftReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mSoftReference.get() == null) {
                return;
            }
            mFactoryActivity = mSoftReference.get();
            switch (msg.what) {
                case SD_ERROR:
                    //TODO:提示错误

                    break;
                default:
                    break;
            }
        }
    }

    private void startTimerOfHint() {
        if (mHintTimer == null) {
            mHintTimer = new CustomTimer();
            mHintTimer.setTag(hintTag);
        }
        mHintTimer.closeTimer();
        mHintTimer.setTag(hintTag);
        mHintTimer.startTimer(1000, 1000, this);
    }

    @Override
    public void timerComply(long lastTime, String tag) {
        if (hintTag.equals(tag)) {
            if (lastTime < HINT_TIME) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cl_hint.setVisibility(View.GONE);
                }
            });
            mHintTimer.closeTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHintTimer != null) {
            mHintTimer.closeTimer();
        }
        if (mUsbBroadcastReceiver != null) {
            unregisterReceiver(mUsbBroadcastReceiver);
            mUsbBroadcastReceiver = null;
        }
    }

    @Override
    public void isFitShowConnect(boolean isConnect) {
        if (isConnect && include_calibrate.getVisibility() == View.GONE) {
            finish();
        }
    }
}