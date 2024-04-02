package com.run.treadmill.activity.runMode.help;

import android.view.View;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;

public class BaseRunClick {
    public static void click(BaseRunActivity activity, View view) {
        switch (view.getId()) {
            case R.id.btn_home:
                activity.safeError();
                break;
            case R.id.btn_line_chart_incline:
            case R.id.btn_line_chart_speed:
                BuzzerManager.getInstance().buzzerRingOnce();
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

            // 暂停相关按钮
            case R.id.iv_pause:
                activity.clickPause();
                break;
            case R.id.btn_pause_continue:
                BuzzerManager.getInstance().buzzerRingOnce();
                activity.btn_pause_continue.postDelayed(() -> {
                    if (activity.mRunningParam.isRunningEnd()) {
                        return;
                    }
                    if (activity.mRunningParam.isContinue()) {
                        return;
                    }
                    activity.mRunningParam.setToContinue();
                    activity.run_pop_pause.setVisibility(View.GONE);
                    activity.runPause.stopPauseTimer();
                    activity.showPreparePlayVideo(0);
                }, 300);
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
                activity.runPause.stopPauseTimer();
                activity.finishRunning();
                break;

            // 顶部icon
            case R.id.iv_run_media:
                activity.runMedia.clickMedia();
                break;
            default:
                break;
        }
    }
}
