package com.run.treadmill.activity.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.activity.factory.FactoryActivity;
import com.run.treadmill.reboot.MyApplication;
import com.run.treadmill.update.homeupdate.main.HomeApkUpdateManager;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.WifiBackFloatWindow;
import com.run.treadmill.widget.MultiClickAndLongPressView;
import com.run.treadmill.widget.WifiMultiClickAndLongPressView;

public class HomeTipsDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private OnTipDialogStatusChange mOnTipDialogStatusChange;

    private RelativeLayout rl_error;
    private MultiClickAndLongPressView lpv_update;
    private WifiMultiClickAndLongPressView lpv_wifi;
    private ImageView img_err_bk;
    private ImageView img_err_icon;
    private TextView tv_err;

    private RelativeLayout rl_lock;
    private RelativeLayout rl_lock_key;
    private ImageView img_tip_lock_bg;
    private TextView et_password;

    private RelativeLayout rl_lube;
    private ImageView img_tip_lube_bg;
    private ImageView btn_lube_pop_yes;
    private ImageView btn_lube_pop_no;
    private ImageView btn_lube_pop_reset;

    private RelativeLayout rl_machine_lube;
    private ImageView img_tip_machine_lube_bg;
    private ImageView btn_machine_lube_pop_yes;
    private ImageView btn_machine_lube_pop_no;

    private RelativeLayout rl_update;
    private ImageView btn_update_pop_yes;
    private ImageView btn_update_pop_no;

    private ImageView img_point;

    private HomePresenter presenter;
    private int lastTips = CTConstant.NO_SHOW_TIPS;

    private int lastError = 0;
    /**
     * 是否正在显示上锁的错误密码提示
     */
    private boolean isShowLockError = false;

    public HomeTipsDialog(Context c) {
        super(c, R.style.HomeTipsDialog);
        context = c;

        setContentView(R.layout.layout_tip);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = 1920;
        lp.height = 1080;
        lp.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        dialogWindow.setAttributes(lp);
        dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
        onCreate();
    }

    @Override
    public void onClick(View view) {
        if (presenter != null) {
            if (presenter.inOnSleep) {
                presenter.wakeUpSleep();
                return;
            } else {
                presenter.reSetSleepTime();
            }
        }
        switch (view.getId()) {
            case R.id.btn_update_pop_yes:
                BuzzerManager.getInstance().buzzerRingOnce();
                HomeApkUpdateManager.getInstance().installApk();
                btn_update_pop_yes.setVisibility(View.GONE);
                btn_update_pop_no.setVisibility(View.GONE);
                break;
            case R.id.btn_update_pop_no:
                BuzzerManager.getInstance().buzzerRingOnce();
                lastTips = CTConstant.NO_SHOW_TIPS;
                int errorTip = ErrorManager.getInstance().getErrorTip();
                if (errorTip != CTConstant.NO_SHOW_TIPS) {
                    showTipPop(errorTip);
                    break;
                }
                errorTip = checkLube();
                if (errorTip != CTConstant.NO_SHOW_TIPS) {
                    showTipPop(errorTip);
                    break;
                }
                boolean machineLube1 = presenter.checkMachineLubeNull();
                if (machineLube1) {
                    showTipPop(CTConstant.SHOW_TIPS_MACHINE_LUBE_NULL);
                    break;
                }
                if (HomeApkUpdateManager.getInstance().isNewVersion && SpManager.getUpdateIsNetwork()) {
                    showTipPop(CTConstant.SHOW_TIPS_POINT);
                    break;
                }
                stopTipsPop();
                break;
            case R.id.img_tip_lube_bg:
                if (lastTips == CTConstant.SHOW_TIPS_MACHINE_LUBE_NULL) {
                    int curType2 = checkLock();
                    if (curType2 != CTConstant.NO_SHOW_TIPS) {
                        lastTips = CTConstant.NO_SHOW_TIPS;
                        showTipPop(curType2);
                        return;
                    }
                    stopTipsPop();
                }
                break;
            case R.id.btn_lube_pop_yes:
                BuzzerManager.getInstance().buzzerRingOnce();
                img_tip_lube_bg.setBackground(null);
                img_tip_lube_bg.setImageDrawable(context.getDrawable(R.drawable.img_pop_lube_message_2));
                btn_lube_pop_yes.setVisibility(View.GONE);
                btn_lube_pop_no.setVisibility(View.GONE);
                btn_lube_pop_reset.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_lube_pop_no:
                BuzzerManager.getInstance().buzzerRingOnce();
                //检测当前是否需要重新提示其他内容
                int curType2 = checkLock();
                if (curType2 != CTConstant.NO_SHOW_TIPS) {
                    lastTips = CTConstant.NO_SHOW_TIPS;
                    showTipPop(curType2);
                    break;
                }
                stopTipsPop();
//                presenter.enterFactoryTwo();
                break;
            case R.id.btn_lube_pop_reset:
                BuzzerManager.getInstance().buzzerRingOnce();
                SpManager.reSetRunLubeDis(0f);
                //检测当前是否需要重新提示其他内容
                int curType3 = checkLock();
                if (curType3 != CTConstant.NO_SHOW_TIPS) {
                    lastTips = CTConstant.NO_SHOW_TIPS;
                    showTipPop(curType3);
                    break;
                }
                stopTipsPop();
                break;
            case R.id.img_tip_machine_lube_bg:
                if (lastTips == CTConstant.SHOW_TIPS_MACHINE_LUBE_NULL) {
                    stopTipsPop();
                }
                break;
            case R.id.btn_machine_lube_pop_yes:
                BuzzerManager.getInstance().buzzerRingOnce();
                //进行加油提示
                if (presenter != null) {
                    btn_machine_lube_pop_yes.setVisibility(View.GONE);
                    btn_machine_lube_pop_no.setVisibility(View.GONE);
                    ControlManager.getInstance().setLube(100);
                    presenter.startMachineLubeTimer();
                }
                break;
            case R.id.btn_machine_lube_pop_no:
                BuzzerManager.getInstance().buzzerRingOnce();
                //  检测当前是否需要重新提示其他内容
                int curType4 = checkLock();
                if (curType4 == CTConstant.NO_SHOW_TIPS) {
                    stopTipsPop();
                    break;
                }
                showTipPop(curType4);
                stopTipsPop();
                break;
            case R.id.et_password:
                BuzzerManager.getInstance().buzzerRingOnce();
                rl_lock_key.setVisibility(View.VISIBLE);
                break;
            case R.id.img_tip_lock_bg:
                if (isShowLockError) {
                    et_password.setVisibility(View.VISIBLE);
                    img_tip_lock_bg.setImageResource(R.drawable.img_pop_console_locked);
                    isShowLockError = false;
                }
                break;
            case R.id.img_point:
                HomeApkUpdateManager.getInstance().isNewVersion = false;
                stopTipsPop();
                break;
            case R.id.txt_lock_key_0:
                et_password.append("0");
                break;
            case R.id.txt_lock_key_1:
                et_password.append("1");
                break;
            case R.id.txt_lock_key_2:
                et_password.append("2");
                break;
            case R.id.txt_lock_key_3:
                et_password.append("3");
                break;
            case R.id.txt_lock_key_4:
                et_password.append("4");
                break;
            case R.id.txt_lock_key_5:
                et_password.append("5");
                break;
            case R.id.txt_lock_key_6:
                et_password.append("6");
                break;
            case R.id.txt_lock_key_7:
                et_password.append("7");
                break;
            case R.id.txt_lock_key_8:
                et_password.append("8");
                break;
            case R.id.txt_lock_key_9:
                et_password.append("9");
                break;
            case R.id.txt_lock_key_del:
                String curPs = et_password.getText().toString();
                if (curPs.length() > 0) {
                    et_password.setText(curPs.substring(0, curPs.length() - 1));
                }
                break;
            case R.id.txt_lock_key_summit:
                if (lastTips != CTConstant.SHOW_TIPS_LOCK) {
                    return;
                }
                if (SpManager.getCustomPass().equals(et_password.getText().toString())
                        || SpManager.getSrsPass().equals(et_password.getText().toString())) {
                    isShowLockError = false;
                    presenter.enterSettingLock();
//                    stopTipsPop();
                } else {
                    //显示输入异常
                    isShowLockError = true;
                    img_tip_lock_bg.setImageResource(R.drawable.img_pop_password_error);
                    et_password.setVisibility(View.GONE);
                }
                et_password.setText("");
                rl_lock_key.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void onCreate() {
        rl_error = (RelativeLayout) findViewById(R.id.rl_error);
        lpv_update = (MultiClickAndLongPressView) findViewById(R.id.lpv_update);
        lpv_wifi = (WifiMultiClickAndLongPressView) findViewById(R.id.lpv_wifi);
        img_err_bk = (ImageView) findViewById(R.id.img_err_bk);
        img_err_icon = (ImageView) findViewById(R.id.img_err_icon);
        tv_err = (TextView) findViewById(R.id.tv_err);

        rl_lock = (RelativeLayout) findViewById(R.id.rl_lock);
        rl_lock_key = (RelativeLayout) findViewById(R.id.rl_lock_key);
        img_tip_lock_bg = (ImageView) findViewById(R.id.img_tip_lock_bg);
        et_password = (TextView) findViewById(R.id.et_password);


        rl_lube = (RelativeLayout) findViewById(R.id.rl_lube);
        img_tip_lube_bg = (ImageView) findViewById(R.id.img_tip_lube_bg);
        btn_lube_pop_yes = (ImageView) findViewById(R.id.btn_lube_pop_yes);
        btn_lube_pop_no = (ImageView) findViewById(R.id.btn_lube_pop_no);
        btn_lube_pop_reset = (ImageView) findViewById(R.id.btn_lube_pop_reset);

        rl_machine_lube = (RelativeLayout) findViewById(R.id.rl_machine_lube);
        img_tip_machine_lube_bg = (ImageView) findViewById(R.id.img_tip_machine_lube_bg);
        btn_machine_lube_pop_yes = (ImageView) findViewById(R.id.btn_machine_lube_pop_yes);
        btn_machine_lube_pop_no = (ImageView) findViewById(R.id.btn_machine_lube_pop_no);

        rl_update = (RelativeLayout) findViewById(R.id.rl_update);
        btn_update_pop_yes = (ImageView) findViewById(R.id.btn_update_pop_yes);
        btn_update_pop_no = (ImageView) findViewById(R.id.btn_update_pop_no);

        img_point = (ImageView) findViewById(R.id.img_point);

        rl_error.setVisibility(View.GONE);
        rl_lock.setVisibility(View.GONE);
        rl_lube.setVisibility(View.GONE);
        rl_update.setVisibility(View.GONE);

        initListener();
    }

    private void initListener() {
        img_tip_lock_bg.setOnClickListener(this);

        et_password.setOnClickListener(this);
        findViewById(R.id.txt_lock_key_0).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_1).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_2).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_3).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_4).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_5).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_6).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_7).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_8).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_9).setOnClickListener(this);

        findViewById(R.id.txt_lock_key_del).setOnClickListener(this);
        findViewById(R.id.txt_lock_key_summit).setOnClickListener(this);

        View.OnClickListener sleepClick = v -> {
            if (presenter != null) {
                if (presenter.inOnSleep) {
                    presenter.wakeUpSleep();
                } else {
                    presenter.reSetSleepTime();
                }
            }
        };

        lpv_update.setOnMultiClickListener(new MultiClickAndLongPressView.OnMultiClickListener() {
            @Override
            public void onMultiClick() {
                Intent intent = new Intent(context, FactoryActivity.class);
                intent.putExtra(CTConstant.FACTORY_NUM, 2);
                intent.putExtra(CTConstant.FACTORY_NO_SHOW_ERR, true);
                context.startActivity(intent);
            }
        });

        lpv_wifi.setOnMultiClickListener(new WifiMultiClickAndLongPressView.OnMultiClickListener() {
            private WifiBackFloatWindow wifiBackFloatWindow;

            @Override
            public void onMultiClick() {
                if (wifiBackFloatWindow != null) {
                    wifiBackFloatWindow.stopFloat1();
                    wifiBackFloatWindow = null;
                }
                wifiBackFloatWindow = new WifiBackFloatWindow(MyApplication.getContext(), (Activity) context);
                wifiBackFloatWindow.startFloat();
                WifiBackFloatWindow.enterSystemSetting2(context, "com.android.settings.wifi.WifiSettings");
            }
        });

        RelativeLayout rl_tip = (RelativeLayout) findViewById(R.id.rl_tip);
        ImageView im_update_bk = (ImageView) findViewById(R.id.im_update_bk);

        rl_tip.setOnClickListener(sleepClick);
        im_update_bk.setOnClickListener(sleepClick);
        img_err_bk.setOnClickListener(sleepClick);
        rl_lock_key.setOnClickListener(sleepClick);

        img_tip_lube_bg.setOnClickListener(this);
        btn_lube_pop_yes.setOnClickListener(this);
        btn_lube_pop_no.setOnClickListener(this);
        btn_lube_pop_reset.setOnClickListener(this);

        img_tip_machine_lube_bg.setOnClickListener(this);
        btn_machine_lube_pop_yes.setOnClickListener(this);
        btn_machine_lube_pop_no.setOnClickListener(this);

        btn_update_pop_yes.setOnClickListener(this);
        btn_update_pop_no.setOnClickListener(this);

        img_point.setOnClickListener(this);
    }


    protected void setPresent(HomePresenter p) {
        presenter = p;
    }

    protected boolean isShowTips() {
        return !(lastTips == CTConstant.NO_SHOW_TIPS);
    }

    protected int getLastTips() {
        return lastTips;
    }

    protected void showTipPop(int tips) {
        boolean result = shouldShowTips(tips);
        if (!result) {
            return;
        }
        if (((HomeActivity) context).isFinishing()) {
            return;
        }
        if (!isShowing()) {
            mOnTipDialogStatusChange.onTipDialogShow(tips);
            show();
        }
    }

    protected void stopTipsPop() {
        if (lastTips == CTConstant.SHOW_TIPS_SAFE_ERROR || lastTips == CTConstant.SHOW_TIPS_OTHER_ERROR) {
            lastTips = CTConstant.NO_SHOW_TIPS;
            int errorTip = ErrorManager.getInstance().getErrorTip();
            if (errorTip != CTConstant.NO_SHOW_TIPS) {
                reSet(errorTip);
                return;
            }
            errorTip = checkLube();
            if (errorTip != CTConstant.NO_SHOW_TIPS) {
                reSet(errorTip);
                return;
            }
            if (HomeApkUpdateManager.getInstance().isNewVersion && SpManager.getUpdateIsNetwork()) {
                reSet(CTConstant.SHOW_TIPS_POINT);
                return;
            }
        }
        if (isShowing()) {
            mOnTipDialogStatusChange.onTipDialogDismiss(lastTips);
            reSet(CTConstant.NO_SHOW_TIPS);
            dismiss();
        }
    }

    private boolean shouldShowTips(int tips) {
        if (tips > lastTips) {
            reSet(tips);
            return true;
        } else if (tips == lastTips) {
            if (lastError != ErrorManager.getInstance().errStatus) {
                reSet(lastTips);
                return true;
            }
        }
        return false;
    }

    private void showErrorTips() {
        if (lastError == ErrorManager.getInstance().errStatus) {
            return;
        }
        lastError = ErrorManager.getInstance().errStatus;
        switch (lastTips) {
            case CTConstant.SHOW_TIPS_COMM_ERROR:
                img_err_bk.setImageResource(R.drawable.img_pop_error);
                img_err_bk.setVisibility(View.VISIBLE);
                img_err_icon.setVisibility(View.GONE);
                tv_err.setVisibility(View.GONE);
                break;
            case CTConstant.SHOW_TIPS_SAFE_ERROR:
                img_err_bk.setVisibility(View.GONE);
                tv_err.setTextColor(context.getResources().getColor(R.color.white, null));
                tv_err.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.font_size_55pt));
                tv_err.setText(R.string.string_emergency_stop);
                tv_err.setVisibility(View.VISIBLE);
                break;
            case CTConstant.SHOW_TIPS_OTHER_ERROR:
            default:
                img_err_bk.setImageResource(R.drawable.img_pop_bk);
                img_err_bk.setVisibility(View.VISIBLE);
                tv_err.setTextColor(context.getResources().getColor(R.color.textView_black, null));
                tv_err.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.font_size_30pt));
                if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_INCLINE_CALIBRATE) {
                    tv_err.setText(R.string.string_calibration_failure);
                } else {
                    if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {
                        tv_err.setText(String.format("E %02XH", ErrorManager.getInstance().errStatus));
                    } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {
                        tv_err.setText(String.format("E %02XH", ErrorManager.getInstance().errStatus));
                    } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {
                        tv_err.setText(String.format("E %d", ErrorManager.getInstance().errStatus));
                    }
                }
                tv_err.setVisibility(View.VISIBLE);
                break;
        }
    }


    private void reSet(@CTConstant.TipPopType int reSetType) {
        lastTips = reSetType;
        lastError = ErrorManager.ERR_NO_ERROR;
        isShowLockError = false;

        rl_error.setVisibility(View.GONE);
        img_err_bk.setImageResource(R.drawable.img_pop_bk);
        img_err_bk.setVisibility(View.GONE);
        img_err_icon.setVisibility(View.VISIBLE);
        tv_err.setTextColor(context.getResources().getColor(R.color.white, null));
        tv_err.setText(R.string.string_emergency_stop);
        tv_err.setVisibility(View.VISIBLE);

        rl_lock.setVisibility(View.GONE);
        rl_lock_key.setVisibility(View.GONE);
        img_tip_lock_bg.setImageResource(R.drawable.img_pop_console_locked);
        et_password.setText("");
        et_password.setVisibility(View.VISIBLE);

        rl_lube.setVisibility(View.GONE);
        img_tip_lube_bg.setImageDrawable(context.getDrawable(R.drawable.img_pop_lube_message_1));
        btn_lube_pop_yes.setVisibility(View.VISIBLE);
        btn_lube_pop_no.setVisibility(View.VISIBLE);
        btn_lube_pop_reset.setVisibility(View.GONE);

        rl_machine_lube.setVisibility(View.GONE);
        img_tip_machine_lube_bg.setImageDrawable(context.getDrawable(R.drawable.img_pop_lube_message_3));
        btn_machine_lube_pop_yes.setVisibility(View.GONE);
        btn_machine_lube_pop_no.setVisibility(View.GONE);

        rl_update.setVisibility(View.GONE);
        btn_update_pop_yes.setVisibility(View.VISIBLE);
        btn_update_pop_no.setVisibility(View.VISIBLE);

        img_point.setVisibility(View.GONE);

        switch (reSetType) {
            case CTConstant.SHOW_TIPS_POINT:
                img_point.setImageResource(R.drawable.img_pop_update_2);
                img_point.setVisibility(View.VISIBLE);
                break;
            case CTConstant.SHOW_TIPS_MACHINE_LUBE:
                img_tip_machine_lube_bg.setImageDrawable(context.getDrawable(R.drawable.img_pop_lube_message_3));
                btn_machine_lube_pop_yes.setVisibility(View.VISIBLE);
                btn_machine_lube_pop_no.setVisibility(View.VISIBLE);
                rl_machine_lube.setVisibility(View.VISIBLE);
                break;
            case CTConstant.SHOW_TIPS_MACHINE_LUBE_NULL:
                img_tip_machine_lube_bg.setImageDrawable(context.getDrawable(R.drawable.img_pop_lube_message_4));
                rl_machine_lube.setVisibility(View.VISIBLE);
                break;
            case CTConstant.SHOW_TIPS_LOCK:
                rl_lock.setVisibility(View.VISIBLE);
                break;
            case CTConstant.SHOW_TIPS_LUBE:
                rl_lube.setVisibility(View.VISIBLE);
                break;
            case CTConstant.SHOW_TIPS_COMM_ERROR:
            case CTConstant.SHOW_TIPS_SAFE_ERROR:
            case CTConstant.SHOW_TIPS_OTHER_ERROR:
                showErrorTips();
                rl_error.setVisibility(View.VISIBLE);
                break;
            case CTConstant.SHOW_TIPS_UPDATE:
                rl_update.setVisibility(View.VISIBLE);
                break;
            case CTConstant.NO_SHOW_TIPS:
            default:
                break;
        }

    }

    /**
     * 检测加油提示
     *
     * @return
     */
    private int checkLube() {
        if (SpManager.getMaxLubeDis() > 0 && SpManager.getMaxLubeDis() - SpManager.getRunLubeDis() <= 0) {
            return CTConstant.SHOW_TIPS_LUBE;
        }
        return checkLock();
    }

    /**
     * 检测加锁
     *
     * @return
     */
    private int checkLock() {
        if ((SpManager.getBackUpTotalRunDis() > 0 && (SpManager.getBackUpTotalRunDis() - SpManager.getBackUpRunDis() <= 0))
                || (SpManager.getBackUpRunTotalTime() > 0 && (SpManager.getBackUpRunTotalTime() - SpManager.getBackUpRunTime() <= 0))) {
            return CTConstant.SHOW_TIPS_LOCK;
        }
        return CTConstant.NO_SHOW_TIPS;
    }

    public boolean isShowLockError() {
        return isShowLockError;
    }

    public void machineLubeFinish() {
        if (rl_lube.getVisibility() == View.VISIBLE) {
            btn_lube_pop_reset.setEnabled(true);
            Logger.d("machineLubeFinish>>>>>>>>>>>>>>>>>>>>>");
        }
    }

    public void setOnTipDialogStatusChange(OnTipDialogStatusChange change) {
        this.mOnTipDialogStatusChange = change;
    }

    public interface OnTipDialogStatusChange {
        void onTipDialogShow(@CTConstant.TipPopType int tipPopType);

        void onTipDialogDismiss(@CTConstant.TipPopType int tipPopType);
    }
}
