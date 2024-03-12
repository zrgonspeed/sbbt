package com.run.treadmill.activity.home;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.run.treadmill.R;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.update.homeupdate.main.HomeApkUpdateManager;
import com.run.treadmill.util.Logger;

public class HomeTipsDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private OnTipDialogStatusChange mOnTipDialogStatusChange;


    private RelativeLayout rl_update;
    private ImageView btn_update_pop_yes;
    private ImageView btn_update_pop_no;


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
        Logger.i("setContentView(R.layout.layout_tip)");

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
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
               /* boolean machineLube1 = presenter.checkMachineLubeNull();
                if (machineLube1) {
                    showTipPop(CTConstant.SHOW_TIPS_MACHINE_LUBE_NULL);
                    break;
                }*/
                if (HomeApkUpdateManager.getInstance().isNewVersion && SpManager.getUpdateIsNetwork()) {
                    showTipPop(CTConstant.SHOW_TIPS_POINT);
                    break;
                }
                stopTipsPop();
                break;

            default:
                break;
        }
    }

    private void onCreate() {

        rl_update = (RelativeLayout) findViewById(R.id.rl_update);
        btn_update_pop_yes = (ImageView) findViewById(R.id.btn_update_pop_yes);
        btn_update_pop_no = (ImageView) findViewById(R.id.btn_update_pop_no);

        rl_update.setVisibility(View.GONE);

        initListener();
    }

    private void initListener() {


        View.OnClickListener sleepClick = v -> {
            if (presenter != null) {
                if (presenter.inOnSleep) {
                    presenter.wakeUpSleep();
                } else {
                    presenter.reSetSleepTime();
                }
            }
        };


        RelativeLayout rl_tip = (RelativeLayout) findViewById(R.id.rl_tip);
        ImageView im_update_bk = (ImageView) findViewById(R.id.im_update_bk);

        rl_tip.setOnClickListener(sleepClick);
        im_update_bk.setOnClickListener(sleepClick);


        btn_update_pop_yes.setOnClickListener(this);
        btn_update_pop_no.setOnClickListener(this);

    }

    protected void setPresent(HomePresenter p) {
        presenter = p;
    }

    public boolean isShowTips() {
        return !(lastTips == CTConstant.NO_SHOW_TIPS);
    }

    public int getLastTips() {
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
                break;
            case CTConstant.SHOW_TIPS_SAFE_ERROR:

                break;
            case CTConstant.SHOW_TIPS_OTHER_ERROR:
            default:

                break;
        }
    }


    private void reSet(@CTConstant.TipPopType int reSetType) {
        lastTips = reSetType;
        lastError = ErrorManager.ERR_NO_ERROR;
        isShowLockError = false;


        rl_update.setVisibility(View.GONE);
        btn_update_pop_yes.setVisibility(View.VISIBLE);
        btn_update_pop_no.setVisibility(View.VISIBLE);


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

    public void setOnTipDialogStatusChange(OnTipDialogStatusChange change) {
        this.mOnTipDialogStatusChange = change;
    }

    public boolean isSafeError() {
        return getLastTips() == CTConstant.SHOW_TIPS_SAFE_ERROR;
    }

    public boolean isOtherError() {
        return getLastTips() == CTConstant.SHOW_TIPS_OTHER_ERROR;
    }

    public interface OnTipDialogStatusChange {
        void onTipDialogShow(@CTConstant.TipPopType int tipPopType);

        void onTipDialogDismiss(@CTConstant.TipPopType int tipPopType);
    }
}
