package com.run.treadmill.activity.factory.two;

import static org.litepal.LitePalApplication.getContext;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.run.treadmill.R;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.activity.factory.FactoryActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.homeupdate.main.ApkUpdateParam;
import com.run.treadmill.homeupdate.third.HomeThirdAppUpdateManager;
import com.run.treadmill.http.OkHttpHelper;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.SystemSoundManager;
import com.run.treadmill.otamcu.OtaMcuUtils;
import com.run.treadmill.receiver.USBBroadcastReceiver;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.SystemUtils;
import com.run.treadmill.util.TimeStringUtil;
import com.run.treadmill.util.UnitUtil;
import com.run.treadmill.util.VersionUtil;
import com.run.treadmill.widget.MultiClickAndLongPressView;
import com.run.treadmill.widget.ViewDialog;
import com.run.treadmill.widget.calculator.CalculatorCallBack;

import java.io.File;

public class FactoryTwo implements CustomTimer.TimerCallBack, USBBroadcastReceiver.OnUSBCallBack, View.OnClickListener, CompoundButton.OnCheckedChangeListener, CalculatorCallBack {
    protected FactoryActivity activity;
    private USBBroadcastReceiver mUsbBroadcastReceiver;
    private String mUdiskPath = ""; // a133的u盘路径不同u盘不一样;

    public RelativeLayout rl_main_two;
    private View[] views = new View[4];
    private RadioButton[] viewsLeft = new RadioButton[4];
    private int lastItemInx;

    private ToggleButton tb_sleep, tb_display, tb_incline, tb_touch, tb_buzzer, tb_login_ctrl;
    private TextView tv_lube;
    private ImageView btn_setting_reset;
    private ImageView btn_img_reset;
    private TextView tv_total_time, tv_total_distance, tv_sdk, tv_firmware, tv_soffware, tv_ncu;
    private ImageView btn_info_reset;
    private ImageView btn_factory_update;
    private ImageView img_old_logo, img_new_logo, btn_update_logo;
    /**
     * 是否正在更新logo，此时不理任何东西
     */
    public boolean isUpdatingLogo;

    public ViewDialog mResetDialog;
    private ImageView btn_pop_yes, btn_pop_no;

    private boolean oldGSMode;

    // ota apk
    private final String hintTag = "alterUpdatePath";
    private final long HINT_TIME = 3;
    private MultiClickAndLongPressView btn_alter_update_path;
    private CustomTimer mHintTimer;
    private ConstraintLayout cl_hint;
    private TextView tv_hint;

    // ota mcu
    private MultiClickAndLongPressView btn_ota_update;


    public FactoryTwo(FactoryActivity factoryActivity) {
        this.activity = factoryActivity;
    }

    public void initFactoryTwo() {
        mUdiskPath = FileUtil.getStoragePath(activity, true);

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
        btn_img_reset = (ImageView) views[0].findViewById(R.id.btn_img_reset);

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

        SpManager.setDisplay(SystemUtils.readTouchesOptions());
        tb_touch.setChecked(SpManager.getDisplay());

        initNeedResetSetting();
        // 服务器切换
        btn_alter_update_path = (MultiClickAndLongPressView) views[2].findViewById(R.id.btn_alter_update_path);
        cl_hint = (ConstraintLayout) views[2].findViewById(R.id.cl_hint);
        tv_hint = (TextView) views[2].findViewById(R.id.tv_hint);
        btn_alter_update_path.setOnMultiClickListener(() -> {
            HomeThirdAppUpdateManager.getInstance().setNewCheck(true);

            if (!SpManager.getAlterUpdatePath()) {
                SpManager.setAlterUpdatePath(true);
                SpManager.setChangedServer(true);
                OkHttpHelper.cancel("HomeActivity");
            }

            tv_hint.setText(getString(R.string.server_hint) + "\n" + ApkUpdateParam.getUpdateHost(activity));
            cl_hint.setVisibility(View.VISIBLE);
            startTimerOfHint();

        });

        // OTA
        btn_ota_update = (MultiClickAndLongPressView) views[2].findViewById(R.id.btn_ota_update);
        btn_ota_update.setOnMultiClickListener(() -> {
            Logger.i("mUdiskPath == " + mUdiskPath);
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("top.cnzrg.otamcu", "top.cnzrg.otamcu.MainActivity");
                intent.setComponent(cn);

                intent.putExtra("mcu_version", SpManager.getNcuVer());

                activity.startActivity(intent);
            } catch (ActivityNotFoundException exception) {
                Logger.e(exception.getMessage());
            }
        });

        btn_setting_reset.setOnClickListener(this);
        btn_img_reset.setOnClickListener(this);
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
        tv_soffware.setText(InitParam.PROJECT_NAME + "\n" + VersionUtil.getAppVersionName(activity));
        tv_ncu.setText(InitParam.PROJECT_NAME + "\n" + SpManager.getNcuVer());
        btn_factory_update.setEnabled(FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.APK_NAME));
        btn_update_logo.setEnabled(FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.LOGO_NAME));
        FileUtil.setLogoIcon(activity, img_old_logo);
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
            if (activity.mCalculatorBuilder != null && activity.mCalculatorBuilder.isPopShowing()) {
                activity.mCalculatorBuilder.stopPopWin();
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
                , getString(activity.isMetric ? R.string.string_unit_km : R.string.string_unit_mile)));
    }

    private void initNeedResetInfo() {
        tv_total_time.setText(TimeStringUtil.getSecToHrMin(SpManager.getRunTotalTime()));
        tv_total_distance.setText(UnitUtil.getFloatToInt(SpManager.getRunTotalDis()) + " " + getString(activity.isMetric ? R.string.string_unit_km : R.string.string_unit_mile));
    }

    @Override
    public void onClick(View v) {
        BuzzerManager.getInstance().buzzerRingOnce();
        switch (v.getId()) {
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
                        FileUtil.installApk(activity, new File(mUdiskPath + "/" + InitParam.APK_NAME));
                    } catch (Exception e) {
                    }
                }
                break;
            case R.id.btn_update_logo:
                isUpdatingLogo = true;
                if (FileUtil.isCheckExist(mUdiskPath + "/" + InitParam.LOGO_NAME)) {
                    try {
                        FileUtil.CopySdcardFile(activity,
                                mUdiskPath + "/" + InitParam.LOGO_NAME,
                                activity.getFilesDir() + "/" + InitParam.LOGO_NAME);
                        SpManager.setIsInnerLogo(false);
                        FileUtil.setLogoIcon(activity, img_old_logo);
                        FileUtil.setLogoIcon(activity, activity.btn_logo);
                    } catch (Exception e) {
                    }
                }
                isUpdatingLogo = false;
                break;
            case R.id.btn_setting_reset:
                createResetDialog(v1 -> {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    mResetDialog.dismiss();

                    tv_lube.setSelected(true);
                    showCalculator(CTConstant.TYPE_FACTORY_LUBE, tv_lube, R.string.string_keybord_lube, 0, this, rl_main_two,
                            getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), getResources().getDimensionPixelSize(R.dimen.dp_px_265_y));
                });
                break;
            case R.id.btn_img_reset:
                doMasterClear();
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
        }
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
        activity.registerReceiver(mUsbBroadcastReceiver, intentFilter);
    }


    /**
     * 显示pop
     *
     * @param listener 点击yes的处理事件
     */
    private void createResetDialog(View.OnClickListener listener) {
        if (mResetDialog == null) {
            mResetDialog = new ViewDialog.Builder(activity)
                    .setView(activity.getLayoutInflater().inflate(R.layout.layout_yes_no_dialog, rl_main_two, false))
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


    @Override
    public void enterCallBack(int type, String value) {
        if (type == CTConstant.TYPE_FACTORY_LUBE) {
            tv_lube.setText(String.format("%s %s", value, activity.isMetric ? getString(R.string.string_unit_km) : getString(R.string.string_unit_mile)));
            SpManager.setMaxLubeDis(Integer.valueOf(value));
            SpManager.reSetRunLubeDis(0f);
        }
    }


    @Override
    public void onCalculatorDismiss() {
        if (rl_main_two != null) {
            if (tv_lube.isSelected()) {
                tv_lube.setSelected(false);
            }
        }
    }

    public void onStop() {
        if (SpManager.getGSMode() != oldGSMode) {
            if (oldGSMode) {
                ControlManager.getInstance().reset();
                ControlManager.getInstance().setIncline(0.0f);
            } else {
                ControlManager.getInstance().stopIncline();
            }
        }
    }

    public void onDestroy() {
        if (mUsbBroadcastReceiver != null) {
            activity.unregisterReceiver(mUsbBroadcastReceiver);
            mUsbBroadcastReceiver = null;
        }
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
                    SystemUtils.writeShowTouchesOptions(1);
                } else {
                    SystemUtils.writeShowTouchesOptions(0);
                }
                break;
            case R.id.tb_buzzer:
                SpManager.setBuzzer(isChecked);
                BuzzerManager.getInstance().setBuzzerEnable(isChecked);
                if (BuzzerManager.getInstance().getBuzzerType() == BuzzerManager.BUZZER_SYSTEM) {
                    SystemSoundManager.getInstance().setEffectsEnabled(isChecked ? 1 : 0);
                }
                break;
            default:
                break;
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
            activity.runOnUiThread(() -> cl_hint.setVisibility(View.GONE));
            mHintTimer.closeTimer();
        }
    }

    public void onPause() {
        if (mHintTimer != null) {
            mHintTimer.closeTimer();
        }
        if (mUsbBroadcastReceiver != null) {
            activity.unregisterReceiver(mUsbBroadcastReceiver);
            mUsbBroadcastReceiver = null;
        }

        OtaMcuUtils.checkCurIsOtamcu();
    }

    public void selectTv(TextView tv, boolean isSelect) {
        activity.selectTv(tv, isSelect);
    }

    public synchronized void showCalculator(@CTConstant.EditType int type, TextView tv, int stringId, int point, CalculatorCallBack callBack, View view, int x, int y) {
        activity.showCalculator(type, tv, stringId, point, callBack, view, x, y);
    }

    private Resources getResources() {
        return activity.getResources();
    }

    public void doMasterClear() {
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
        intent.putExtra("android.intent.extra.WIPE_EXTERNAL_STORAGE", false);
        intent.addFlags((int) 0x01000000);
        getContext().sendBroadcast(intent);
    }

    private View findViewById(int id) {
        return activity.findViewById(id);
    }

    private String getString(int id) {
        return activity.getString(id);
    }
}