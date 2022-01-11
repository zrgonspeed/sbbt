package com.run.treadmill.activity.setting;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.R;
import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.appStore.AppStoreActivity;
import com.run.treadmill.activity.floatWindow.SettingBackFloatWindow;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.manager.SystemBrightManager;
import com.run.treadmill.manager.VoiceManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThirdApkSupport;
import com.run.treadmill.util.TimeStringUtil;
import com.run.treadmill.util.UnitUtil;
import com.run.treadmill.widget.ViewDialog;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfSetting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

@CreatePresenter(SettingPresenter.class)
public class SettingActivity extends BaseActivity<SettingView, SettingPresenter> implements SettingView, CalculatorCallBack {

    @BindView(R.id.rl_main)
    RelativeLayout rl_main;

    @BindView(R.id.sp_language)
    Spinner sp_language;

    @BindView(R.id.btn_home)
    ImageView btn_home;
    @BindView(R.id.btn_logo)
    ImageView btn_logo;

    @BindView(R.id.btn_back)
    ImageView btn_back;

    @BindView(R.id.rg_setting)
    RadioGroup rg_setting;

    @BindView(R.id.rb_setting_type1)
    RadioButton rb_setting_type1;
    @BindView(R.id.rb_setting_type2)
    RadioButton rb_setting_type2;
    @BindView(R.id.rb_setting_type3)
    RadioButton rb_setting_type3;
    @BindView(R.id.rb_setting_type4)
    RadioButton rb_setting_type4;

    @BindView(R.id.layout_setting_1)
    RelativeLayout layout_setting_1;

    @BindView(R.id.layout_setting_2_1)
    RelativeLayout layout_setting_2_1;

    @BindView(R.id.layout_setting_2_2)
    RelativeLayout layout_setting_2_2;

    @BindView(R.id.sb_setting_brightness)
    SeekBar sb_setting_brightness;

    @BindView(R.id.sb_setting_sound)
    SeekBar sb_setting_sound;

    @BindView(R.id.txt_setting_update)
    TextView txt_setting_update;
    @BindView(R.id.btn_setting_update)
    ImageView btn_setting_update;


    @BindView(R.id.txt_setting_time_1)
    TextView txt_setting_time_1;
    @BindView(R.id.txt_setting_time_2)
    TextView txt_setting_time_2;
    @BindView(R.id.txt_setting_distance_1)
    TextView txt_setting_distance_1;
    @BindView(R.id.txt_setting_distance_2)
    TextView txt_setting_distance_2;

    @BindView(R.id.txt_setting_customer_psw)
    TextView txt_setting_customer_psw;

    @BindView(R.id.txt_setting_sr_psw)
    TextView txt_setting_sr_psw;

    @BindView(R.id.rl_delete_accounts)
    RelativeLayout rl_delete_accounts;
    @BindView(R.id.img_delete_accounts)
    ImageView img_delete_accounts;
    @BindView(R.id.btn_delete_accounts_yes)
    ImageView btn_delete_accounts_yes;
    @BindView(R.id.btn_delete_accounts_no)
    ImageView btn_delete_accounts_no;

    private boolean isShowSetting2 = false;
    /**
     * 是否可以切换语言
     */
    private boolean isCanChange = false;
    /**
     * 是否正在切换语言
     */
    private boolean isChangeLanguage = false;
    /**
     * 当前的语言下标
     */
    private int currLanguagePos;

    private boolean isDeleteAccounts = false;

    private Locale locale;

    private ViewDialog mResetDialog;

    private BaseCalculator.Builder mCalcBuilder;
    private String sysSetting = "com.android.settings";
    private String setting = "com.android.settings.Settings";
    private String bt = "com.android.settings.bluetooth.BluetoothSettings";
    private String wifi = "com.android.settings.wifi.WifiSettings";
    private final int minBright = 80;
    private final int maxProgressBright = 255 - minBright;

    private boolean isOpenGSMode = false;
    private int curMinAD = 0;
    private SettingBackFloatWindow settingBackFloatWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FileUtil.setLogoIcon(this, btn_logo);
        loadSpManager();
        if (settingBackFloatWindow != null) {
            settingBackFloatWindow.stopFloatWindow();
        }
        btn_home.setEnabled(true);
    }

    @OnClick({R.id.btn_back,
            R.id.rb_setting_type1, R.id.rb_setting_type2, R.id.rb_setting_type3, R.id.rb_setting_type4,
            R.id.btn_setting_update, R.id.btn_setting_delete_accounts, R.id.btn_delete_accounts_yes, R.id.btn_delete_accounts_no,
            R.id.btn_setting_reset_time, R.id.btn_setting_reset_distance, R.id.btn_setting_reset_customer_psw,})
    public void click(View view) {
        if (isChangeLanguage) {
            return;
        }
        BuzzerManager.getInstance().buzzerRingOnce();
        switch (view.getId()) {
            case R.id.btn_back:
                if (mCalcBuilder.isPopShowing()) {
                    mCalcBuilder.stopPopWin();
                }
                isShowSetting2 = false;
                btn_back.setVisibility(View.GONE);
                btn_home.setVisibility(View.VISIBLE);

                rb_setting_type1.setChecked(true);

                rb_setting_type1.setText(R.string.string_setting_system);
                rb_setting_type2.setText(R.string.string_setting_bluetooth);
                rb_setting_type3.setVisibility(View.VISIBLE);
                rb_setting_type4.setVisibility(View.VISIBLE);

                layout_setting_1.setVisibility(View.VISIBLE);
                layout_setting_2_1.setVisibility(View.GONE);
                layout_setting_2_2.setVisibility(View.GONE);

                rg_setting.setBackground(getResources().getDrawable(R.drawable.img_factory_select_bar_4, null));
                //其他输入框显示隐藏 暂未实现
                break;
            case R.id.rb_setting_type1:
                if (isShowSetting2) {
                    layout_setting_2_2.setVisibility(View.GONE);
                    layout_setting_2_1.setVisibility(View.VISIBLE);
                    mCalcBuilder.stopPopWin();
                    //其他输入框显示隐藏 暂未实现
                } else {
                    if (mCalcBuilder.isPopShowing()) {
                        mCalcBuilder.stopPopWin();
                    }
                    layout_setting_1.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.rb_setting_type2:
                if (isShowSetting2) {
                    layout_setting_2_2.setVisibility(View.VISIBLE);
                    layout_setting_2_1.setVisibility(View.GONE);
                    mCalcBuilder.stopPopWin();
                } else {
                    //进入蓝牙
                    layout_setting_1.setVisibility(View.GONE);
                    if (settingBackFloatWindow != null) {
                        settingBackFloatWindow.stopFloatWindow();
                        settingBackFloatWindow = null;
                    }
                    settingBackFloatWindow = new SettingBackFloatWindow(getApplicationContext(), SettingActivity.this);
                    settingBackFloatWindow.startFloat();
                    ThirdApkSupport.doStartApplicationWithPackageName(this, sysSetting, bt);
                    btn_home.setEnabled(false);
                }
                break;
            case R.id.rb_setting_type3:
                //进入WiFi
                layout_setting_1.setVisibility(View.GONE);
                if (settingBackFloatWindow != null) {
                    settingBackFloatWindow.stopFloatWindow();
                    settingBackFloatWindow = null;
                }
                settingBackFloatWindow = new SettingBackFloatWindow(getApplicationContext(), SettingActivity.this);
                settingBackFloatWindow.startFloat();
                ThirdApkSupport.doStartApplicationWithPackageName(this, sysSetting, wifi);
                btn_home.setEnabled(false);
                break;
            case R.id.rb_setting_type4:
                layout_setting_1.setVisibility(View.GONE);
                //显示数字键盘
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_SETTING_LOCK)
                        .editTypeName(R.string.string_setting_psw)
                        .callBack(this)
                        .maxLength(4)
                        .mainView(rl_main)
                        .setXAndY(getResources().getDimensionPixelSize(R.dimen.dp_px_453_x), getResources().getDimensionPixelSize(R.dimen.dp_px_195_y))
                        .startPopWindow();
                break;
            case R.id.btn_setting_reset_time:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_SETTING_TIME)
                        .editTypeName(R.string.string_common_time)
                        .callBack(this)
                        .mainView(rl_main)
                        .setXAndY(getResources().getDimensionPixelSize(R.dimen.dp_px_453_x), getResources().getDimensionPixelSize(R.dimen.dp_px_195_y))
                        .startPopWindow();
                break;
            case R.id.btn_setting_reset_distance:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_SETTING_DISTANCE)
                        .editTypeName(R.string.string_common_distance)
                        .callBack(this)
                        .mainView(rl_main)
                        .setXAndY(getResources().getDimensionPixelSize(R.dimen.dp_px_453_x), getResources().getDimensionPixelSize(R.dimen.dp_px_195_y))
                        .startPopWindow();
                break;
            case R.id.btn_setting_reset_customer_psw:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_SETTING_LOCK_RESET)
                        .callBack(this)
                        .maxLength(4)
                        .mainView(rl_main)
                        .setXAndY(getResources().getDimensionPixelSize(R.dimen.dp_px_453_x), getResources().getDimensionPixelSize(R.dimen.dp_px_195_y))
                        .startPopWindow();
                break;
            case R.id.btn_setting_update:
                startActivity(new Intent(this, AppStoreActivity.class));
                break;
            case R.id.btn_setting_delete_accounts:
                if (rl_delete_accounts.getVisibility() == View.GONE) {
                    rl_delete_accounts.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_delete_accounts_yes:
                if (!isCanChange || isDeleteAccounts) {
                    break;
                }
                isDeleteAccounts = true;
                img_delete_accounts.setImageResource(R.drawable.img_pop_updated_restart);
                btn_delete_accounts_yes.setEnabled(false);
                btn_delete_accounts_no.setEnabled(false);
                btn_delete_accounts_yes.setVisibility(View.GONE);
                btn_delete_accounts_no.setVisibility(View.GONE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // android9以后 的账户数据路径不同
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ShellCmdUtils.getInstance().execCommand("rm -rf /data/system_de/0/*");
                            } else {
                                ShellCmdUtils.getInstance().execCommand("rm -rf /data/system/users/0/accounts.db");
                                ShellCmdUtils.getInstance().execCommand("rm -rf /data/system/users/0/accounts.db-journal");
                            }
                            String[] pkNames = getResources().getStringArray(R.array.ignore_thirdAPK_send_message);
                            for (String pkName : pkNames) {
                                ThirdApkSupport.killCommonApp(getApplicationContext(), pkName);
                            }
                            for (String pkName : pkNames) {
                                if (pkName.contains("youtube")) {
                                    ShellCmdUtils.getInstance().execCommand("rm -rf /data/data/" + pkName + "/databases/*");
                                } else {
                                    ShellCmdUtils.getInstance().execCommand("rm -rf /data/data/" + pkName + "/*");
                                }

                            }

                            Thread.sleep(3000);
                            ShellCmdUtils.getInstance().execCommand("reboot");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.btn_delete_accounts_no:
                rl_delete_accounts.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    public void hideTips() {

    }

    @Override
    public void safeError() {
        if (isDeleteAccounts) {
            return;
        }
        super.safeError();
    }

    @Override
    public void commOutError() {
        if (isDeleteAccounts) {
            return;
        }
        super.commOutError();
    }

    @Override
    public void showError(int errCode) {
        if (isDeleteAccounts) {
            return;
        }
        super.showError(errCode);
    }

    @Override
    public void cmdKeyValue(int keyValue) {
        if (isChangeLanguage || isDeleteAccounts) {
            return;
        }
        switch (keyValue) {
      /*      case SerialKeyValue.VOICE_UP_CLICK:
            case SerialKeyValue.VOICE_UP_CLICK_LONG_1:
            case SerialKeyValue.VOICE_UP_CLICK_LONG_2:
                if (sb_setting_sound != null && VoiceManager.getInstance().getCurrentPro() < 100) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    sb_setting_sound.setProgress(VoiceManager.getInstance().getCurrentPro() + 1);
                    sb_setting_sound.postInvalidate();
                }
                break;
            case SerialKeyValue.VOICE_DOWN_CLICK:
            case SerialKeyValue.VOICE_DOWN_CLICK_LONG_1:
            case SerialKeyValue.VOICE_DOWN_CLICK_LONG_2:
                if (sb_setting_sound != null && VoiceManager.getInstance().getCurrentPro() > 0) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    sb_setting_sound.setProgress(VoiceManager.getInstance().getCurrentPro() - 1);
                    sb_setting_sound.postInvalidate();
                }
                break;*/
            case SerialKeyValue.HOME_KEY_CLICK:
                if (btn_home.isEnabled() && btn_home.getVisibility() == View.VISIBLE) {
                    btn_home.performClick();
                }
                break;
            case SerialKeyValue.BACK_KEY_CLICK:
                if (btn_back.isEnabled() && btn_back.getVisibility() == View.VISIBLE) {
                    btn_back.performClick();
                }
                break;
        }
    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        if (!SafeKeyTimer.getInstance().getIsSafe()) {
            if (isCanChange) {
                isCanChange = false;
            }
            return;
        }
        if (beltStatus != 0) {
            if (isCanChange) {
                isCanChange = false;
            }
            return;
        }

        //有扬升错误，不管扬升状态
        if (ErrorManager.getInstance().isHasInclineError()) {
            if (!isCanChange) {
                isCanChange = true;
            }
            return;
        }

        //扬升状态为0，扬升ad在最小ad的+/- 15内
        if (inclineStatus == 0) {
            if (checkADValueIsInSafe(curInclineAd)) {
                if (!isCanChange) {
                    isCanChange = true;
                }
                return;
            }
        }

        if (isCanChange) {
            isCanChange = false;
        }
    }

    @Override
    public void enterCallBack(int type, String value) {
        switch (type) {
            default:
                break;
            case CTConstant.TYPE_SETTING_TIME:
                txt_setting_time_1.setText(String.format("%s %s", value, getString(R.string.string_unit_hr)));
                txt_setting_time_2.setText(String.format("%s 0 min", txt_setting_time_1.getText().toString()));
                SpManager.resetBackUpRunTotalTime(Long.valueOf(value));
                break;
            case CTConstant.TYPE_SETTING_DISTANCE:
                txt_setting_distance_1.setText(String.format("%s %s", value, getString(isMetric ? R.string.string_unit_km : R.string.string_unit_mile)));
                txt_setting_distance_2.setText(txt_setting_distance_1.getText().toString());
                SpManager.resetBackUpRunTotalDis(Float.valueOf(value));
                break;
            case CTConstant.TYPE_SETTING_LOCK:
                getPresenter().checkLock(value);
                break;
            case CTConstant.TYPE_SETTING_LOCK_RESET:
                SpManager.setCustomPass(value);
                txt_setting_customer_psw.setText(getPresenter().loadLockPass());
                break;
        }
    }

    @Override
    public void onCalculatorDismiss() {

    }

    @Override
    public void enterLockResult(boolean isSucceed) {
        if (isSucceed) {
            isShowSetting2 = true;
            layout_setting_2_1.setVisibility(View.VISIBLE);

            rb_setting_type1.setText(R.string.string_setting_lock);
            rb_setting_type2.setText(R.string.string_setting_psw);
            rb_setting_type3.setVisibility(View.GONE);
            rb_setting_type4.setVisibility(View.GONE);
            btn_home.setVisibility(View.GONE);
            btn_back.setVisibility(View.VISIBLE);

            layout_setting_2_2.setVisibility(View.GONE);
            rb_setting_type1.setChecked(true);

            rg_setting.setBackground(getResources().getDrawable(R.drawable.img_factory_select_bar_3, null));
        } else {
            createResetDialog(v -> {
                mResetDialog.dismiss();
                //显示数字键盘
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_SETTING_LOCK)
                        .callBack(SettingActivity.this)
                        .maxLength(4)
                        .mainView(rl_main)
                        .setXAndY(getResources().getDimensionPixelSize(R.dimen.dp_px_453_x), getResources().getDimensionPixelSize(R.dimen.dp_px_195_y))
                        .startPopWindow();
            });
        }
    }

    /**
     * 显示pop
     *
     * @param listener 点击yes的处理事件
     */
    private void createResetDialog(View.OnClickListener listener) {
        if (mResetDialog == null) {
            View view = getLayoutInflater().inflate(R.layout.layout_img_dialog, rl_main, false);
            mResetDialog = new ViewDialog.Builder(this)
                    .setView(view)
                    .create();
            mResetDialog.setCanceledOnTouchOutside(false);
            view.findViewById(R.id.dialog_img).setOnClickListener(listener);
        }
        mResetDialog.show();
    }

    private void init() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.language_spinner_item, getResources().getStringArray(R.array.spinner_language));
        sp_language.setAdapter(adapter);
        locale = getResources().getConfiguration().locale;

        if (locale.getLanguage().endsWith("en")) {
            sp_language.setSelection(0, true);
            currLanguagePos = 0;
        } else if (locale.getLanguage().endsWith("de")) {
            sp_language.setSelection(1, true);
            currLanguagePos = 1;
        } else if (locale.getLanguage().endsWith("fr")) {
            sp_language.setSelection(2, true);
            currLanguagePos = 2;
        } else if (locale.getLanguage().endsWith("es")) {
            sp_language.setSelection(3, true);
            currLanguagePos = 3;
        } else if (locale.getLanguage().endsWith("pt")) {
            sp_language.setSelection(4, true);
            currLanguagePos = 4;
        } else {
            sp_language.setSelection(0, true);
            currLanguagePos = 0;
        }

        sp_language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!SafeKeyTimer.getInstance().getIsSafe()) {
                    sp_language.setSelection(currLanguagePos, true);
                    return;
                }
                if (!isCanChange) {
                    sp_language.setSelection(currLanguagePos, true);
                    return;
                }
                switch (position) {
                    case 0:
                        changeSystemLanguage60(Locale.ENGLISH);
                        break;
                    case 1:
                        changeSystemLanguage60(Locale.GERMAN);
                        break;
                    case 2:
                        changeSystemLanguage60(Locale.FRANCE);
                        break;
                    case 3:
                        changeSystemLanguage60(new Locale("es", "ES"));
                        break;
                    case 4:
                        changeSystemLanguage60(new Locale("pt", "PT"));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sb_setting_brightness.setMax(maxProgressBright);
        int nowBright = SystemBrightManager.getBrightness(this);

        int progress = nowBright - minBright;

        if (progress <= 0) {
            progress = 0;
        }


        sb_setting_brightness.setProgress(progress);
        sb_setting_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SystemBrightManager.setBrightness(SettingActivity.this, progress + minBright);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SystemBrightManager.setBrightness(SettingActivity.this, seekBar.getProgress() + minBright);
            }
        });
        sb_setting_sound.setMax(60);
        sb_setting_sound.setProgress(VoiceManager.getInstance().getCurrentPro() > sb_setting_sound.getMax() ? sb_setting_sound.getMax() : VoiceManager.getInstance().getCurrentPro());
        if (VoiceManager.getInstance().getCurrentPro() > sb_setting_sound.getMax()) {
            VoiceManager.getInstance().setAudioVolume(sb_setting_sound.getMax(), 100);
        }
        sb_setting_sound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                VoiceManager.getInstance().setAudioVolume(seekBar.getProgress(), 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                VoiceManager.getInstance().setAudioVolume(progress, 100);
            }
        });

        txt_setting_customer_psw.setText(getPresenter().loadLockPass());
        txt_setting_sr_psw.setText(getPresenter().loadSrsPass());

        mCalcBuilder = new BaseCalculator.Builder(new CalculatorOfSetting(this));
        rb_setting_type1.setChecked(true);
        txt_setting_time_1.setText((SpManager.getBackUpRunTotalTime() / 60L / 60L) + " " + getString(R.string.string_unit_hr));
        if (SpManager.getBackUpRunTotalTime() == 0) {
            txt_setting_time_2.setText("0 hr 0 min");
        } else {
            txt_setting_time_2.setText(TimeStringUtil.getsecToHrMinOrMinSec(SpManager.getBackUpRunTotalTime() - SpManager.getBackUpRunTime(), "%02d hr %02d min", "%02d min %02d sec"));
        }
        float reminDis = SpManager.getBackUpTotalRunDis() - SpManager.getBackUpRunDis();
//        if (isMetric) {
//            txt_setting_distance_1.setText(UnitUtil.getFloatToIntClear(SpManager.getBackUpTotalRunDis()) + " " + getString(R.string.string_unit_km));
//            txt_setting_distance_2.setText(UnitUtil.getFloatToIntClear(reminDis < 0 ? 0 : reminDis) + " " + getString(R.string.string_unit_km));
//        } else {
//            txt_setting_distance_1.setText(UnitUtil.getFloatToIntClear(UnitUtil.getKmToMileByFloat1(SpManager.getBackUpTotalRunDis())) + " " + getString(R.string.string_unit_mile));
//            txt_setting_distance_2.setText(UnitUtil.getFloatToIntClear(UnitUtil.getKmToMileByFloat1(reminDis < 0 ? 0 : reminDis)) + " " + getString(R.string.string_unit_mile));
//        }
        txt_setting_distance_1.setText(UnitUtil.getFloatToIntClear(SpManager.getBackUpTotalRunDis()) + " " + getString(isMetric ? R.string.string_unit_km : R.string.string_unit_mile));
        txt_setting_distance_2.setText(UnitUtil.getFloatToIntClear(reminDis < 0 ? 0 : reminDis) + " " + getString(isMetric ? R.string.string_unit_km : R.string.string_unit_mile));
        boolean result = getIntent().getBooleanExtra(CTConstant.IS_ENTER_LOCK, false);
        if (result) {
            //直接进入Lock后的界面
            layout_setting_1.setVisibility(View.GONE);
            enterLockResult(true);
        }

        if (locale.getLanguage().endsWith("zh")) {
            btn_setting_update.setEnabled(false);
            txt_setting_update.setVisibility(View.GONE);
            btn_setting_update.setVisibility(View.GONE);
        }
    }

    public void goHome(View view) {
        if (isChangeLanguage) {
            return;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCalcBuilder.isPopShowing()) {
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

    private synchronized void changeSystemLanguage60(final Locale locale) {
        isChangeLanguage = true;
        sp_language.setClickable(false);
        sp_language.setEnabled(false);

        SpManager.setAlterUpdatePath(false);
        SpManager.setChangedServer(false);

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (locale != null) {
                    try {
                        Class classActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
                        Method getDefault = classActivityManagerNative.getDeclaredMethod("getDefault");
                        Object objIActivityManager = getDefault.invoke(classActivityManagerNative);
                        Class classIActivityManager = Class.forName("android.app.IActivityManager");
                        Method getConfiguration = classIActivityManager.getDeclaredMethod("getConfiguration");
                        Configuration config = (Configuration) getConfiguration.invoke(objIActivityManager);
                        config.setLocale(locale);
                        //config.userSetLocale = true;
                        Class clzConfig = Class.forName("android.content.res.Configuration");
                        Field userSetLocale = clzConfig.getField("userSetLocale");
                        userSetLocale.set(config, true);
                        Class[] clzParams = {Configuration.class};
                        Method updateConfiguration = classIActivityManager.getDeclaredMethod("updateConfiguration", clzParams);
                        updateConfiguration.invoke(objIActivityManager, config);
                        BackupManager.dataChanged("com.android.providers.settings");
                    } catch (Exception e) {
                        Logger.d("changeSystemLanguage: " + e.getLocalizedMessage());
                    }
                }
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}