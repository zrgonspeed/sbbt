package com.run.treadmill.activity.runMode.help;

import android.view.View;

import com.run.treadmill.AppDebug;
import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.ThreadUtils;

public class BaseRunClick {
    public static void click(BaseRunActivity activity, View view) {
        if (view.getId() == R.id.btn_start_stop_skip &&
                activity.mediaPopWin != null && activity.mediaPopWin.isShowing()) {
            activity.hideMediaPopWin();
        }
        switch (view.getId()) {
            case R.id.btn_home:
                activity.safeError();
                break;
            case R.id.btn_line_chart_incline:
            case R.id.btn_line_chart_speed:
                BuzzerManager.getInstance().buzzerRingOnce();
                break;
            case R.id.btn_start_stop_skip:
                if (activity.btn_home.getVisibility() == View.VISIBLE) {
                    activity.btn_home.setVisibility(View.GONE);
                }
                if (activity.mRunningParam.isNormal()) {
                    activity.rl_mask.setVisibility(View.GONE);

                    activity.btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
                    activity.mRunningParam.setToPrepare();
                    activity.showPrepare(0);
                    break;
                } else if (activity.mRunningParam.isStopStatus()
                        || activity.mRunningParam.isPrepare()) {
                    break;
                } else if (activity.mRunningParam.isWarmStatus()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    activity.warmUpToRunning();
                } else if (activity.mRunningParam.isCoolDownStatus()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    activity.btn_start_stop_skip.setEnabled(false);
                    activity.finishRunning();
                } else {
                    activity.disFlag = true;
                    Logger.i("disFlag = true");
                    ThreadUtils.runInThread(() -> {
                        activity.disFlag = false;
                        Logger.i("disFlag = false");

                        if (AppDebug.debug) {
                            if (!activity.isDestroyed()) {
                                activity.runOnUiThread(() -> {
                                    activity.btn_pause_continue.setEnabled(true);
                                });
                            }
                        }
                    }, 1000);

                    BuzzerManager.getInstance().buzzerRingOnce();
                    activity.mRunningParam.setToStopStatus();
                    activity.btn_pause_continue.setEnabled(false);
                    // gsMode默认false
                    // 客户要求修改扬升机制
                    ControlManager.getInstance().stopRun(activity.gsMode);
                    // ControlManager.getInstance().resetIncline();

                    if (activity.mVideoPlayerSelf != null) {
                        activity.mVideoPlayerSelf.videoPlayerStartPause();
                    }
                }
                activity.showPopTip();
                break;
            case R.id.btn_pause_continue:
                if (activity.mRunningParam.isRunningEnd()) {
                    return;
                }
                if (activity.mRunningParam.isContinue()) {
                    return;
                }
                activity.mRunningParam.setToContinue();
                BuzzerManager.getInstance().buzzerRingOnce();
                activity.rl_mask.setVisibility(View.GONE);
                activity.stopPauseTimer();
                activity.showPrepare(0);
                break;
            case R.id.btn_pause_quit:
                if (!activity.mRunningParam.isStopStatus()) {
                    return;
                }
                activity.btn_pause_quit.setEnabled(false);
                BuzzerManager.getInstance().buzzerRingOnce();
                if (activity.mVideoPlayerSelf != null) {
                    activity.mVideoPlayerSelf.onRelease();
                }
                activity.stopPauseTimer();
                activity.finishRunning();
                break;
            case R.id.btn_speed_roller:
                if (activity.btn_speed_roller.isSelected()) {
                    return;
                }
                if (activity.btn_incline_roller.isSelected()) {
                    activity.mCalcBuilder.stopPopWin();
                }
                BuzzerManager.getInstance().buzzerRingOnce();
                activity.mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_SPEED)
                        .editTypeName(R.string.string_speed)
                        .floatPoint(1)
                        .mainView(activity.rl_main)
                        .setXAndY(activity.getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), activity.getResources().getDimensionPixelSize(R.dimen.dp_px_234_y))
                        .startPopWindow();

                activity.setControlEnable(false);
                activity.btn_speed_roller.setSelected(true);
                break;
            case R.id.btn_incline_roller:
                if (activity.btn_incline_roller.isSelected()) {
                    return;
                }
                if (activity.btn_speed_roller.isSelected()) {
                    activity.mCalcBuilder.stopPopWin();
                }
                BuzzerManager.getInstance().buzzerRingOnce();
                activity.mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_INCLINE)
                        .editTypeName(R.string.string_incline)
                        .mainView(activity.rl_main)
                        .setXAndY(activity.getResources().getDimensionPixelSize(R.dimen.dp_px_630_x), activity.getResources().getDimensionPixelSize(R.dimen.dp_px_234_y))
                        .startPopWindow();

                activity.setControlEnable(false);
                activity.btn_incline_roller.setSelected(true);
                break;
            default:
                break;
        }
    }

}
