package com.run.treadmill.homeupdate.third;


import android.app.Activity;
import android.content.Intent;

import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.activity.setting.SettingActivity;
import com.run.treadmill.homeupdate.third.utils.HomeUtils;
import com.run.treadmill.homeupdate.third.utils.NetUtils;
import com.run.treadmill.http.OkHttpCallBack;
import com.run.treadmill.http.OkHttpHelper;
import com.run.treadmill.util.Logger;

import java.io.IOException;

import okhttp3.Call;

public class HomeThirdAppUpdateManager implements OkHttpCallBack {
    private static volatile HomeThirdAppUpdateManager instance;
    private static String TAG = HomeThirdAppUpdateManager.class.getSimpleName();

    private HomeThirdAppUpdateManager() {
    }

    public static HomeThirdAppUpdateManager getInstance() {
        if (instance == null) {
            synchronized (HomeThirdAppUpdateManager.class) {
                if (instance == null) {
                    instance = new HomeThirdAppUpdateManager();
                }
            }
        }
        return instance;
    }

    private ThirdUpdateDialog dialog;
    private Activity activity;
    private Thread myThread;
    private boolean newCheck;   // 为true表示进行服务器检测, 上电、切换语言、切换服务器的时候设为true

    public void setNewCheck(boolean newCheck) {
        OkHttpHelper.cancel("HomeGetThirdApp");
        this.newCheck = newCheck;
        isHasNetword = false;
        flagWhile = false;
        resetThreadAndDialog();
    }

    private boolean isHasNetword;

    private boolean flagWhile = false;

    public synchronized void checkOnResume(Activity activity) {
        this.activity = activity;
        buildDialog();

        if (myThread == null) {
            myThread = new Thread(() -> {
                try {
                    flagWhile = true;
                    while (!isHasNetword && flagWhile) {
                        Logger.i("没有网络");
                        isHasNetword = NetUtils.INSTANCE.isOnline();
                        if (isHasNetword) {
                            // 有网络了
                            String buildReqUrl = ThirdUpdateCheck.buildReqUrl();
                            Logger.d("thirdapp请求url " + buildReqUrl);
                            OkHttpHelper.get(buildReqUrl, "HomeGetThirdApp", this);
                            break;
                        }
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            myThread.start();
        }
    }

    private void resetThreadAndDialog() {
        if (myThread != null) {
            myThread.interrupt();
            myThread = null;
        }

        if (dialog != null) {
            dialog.destoryToPopUpDialog();
            dialog = null;
        }
    }

    private void buildDialog() {
        if (dialog == null) {
            dialog = new ThirdUpdateDialog(activity);
            dialog.setOnDiagClick(new ThirdUpdateDialog.OnDiagClick() {
                @Override
                public void onYesClick() {
                    toThirdUpdateUI(activity);
                }

                @Override
                public void onNoClick() {

                }
            });
        }
    }

    public static boolean toThirdAppUI = false;

    private static void toThirdUpdateUI(Activity activity) {
        toThirdAppUI = true;
        Intent intent = new Intent(activity, SettingActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onSuccess(Call call, String response) {
        Logger.i(TAG, "onSuccess");

        // 检查是否有更新
        boolean hasUpdate = ThirdUpdateCheck.hasUpdateResponse(response);
        Logger.i(TAG, "needUpdate == " + hasUpdate);

        if (hasUpdate) {
            if (!HomeUtils.currentIsHomeActivity()) {
                // 不在主界面不能弹框
                Logger.d("第三方更新弹窗不弹，因为不在主界面");
                HomeThirdAppUpdateManager.getInstance().setNewCheck(true);
                return;
            }
            // 安全key时不弹
            if (((HomeActivity) activity).isSafeKeyTips()) {
                Logger.d("当前安全key弹窗，不弹第三方更新");
                HomeThirdAppUpdateManager.getInstance().setNewCheck(true);
                return;
            }
            activity.runOnUiThread(() -> {
                dialog.showToPopUpDialog(-1);
            });
        } else {

        }
    }

    public void hideDialog() {
        if (isShow()) {
            if (dialog != null) {
                dialog.hidePopUpDialog();
            }
        }
    }

    public boolean isShow() {
        if (dialog != null) {
            return dialog.isShowPopUpDialog();
        }
        return false;
    }
}
