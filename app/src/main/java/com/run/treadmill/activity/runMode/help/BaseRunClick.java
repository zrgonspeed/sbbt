package com.run.treadmill.activity.runMode.help;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.run.treadmill.R;
import com.run.treadmill.activity.home.help.media.HomeAppAdapter;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.update.thirdapp.main.HomeAndRunAppUtils;
import com.run.treadmill.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class BaseRunClick {
    public static void click(BaseRunActivity activity, View view) {
        if (view.getId() == R.id.btn_start_stop_skip &&
                activity.runMedia.isShowing()) {
            activity.runMedia.hideMediaPopWin();
        }
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
                clickMedia(activity);
                break;
            default:
                break;
        }
    }

    private List<Integer> iconList;
    private String[] pkgName;
    private String mediaPkgName = "";

    public void onCreate() {
        iconList = new ArrayList<>();
        pkgName = HomeAndRunAppUtils.getPkgNames();

        int[] drawable = HomeAndRunAppUtils.getRunDrawables();
        for (int id : drawable) {
            iconList.add(id);
        }
    }

    private static void clickMedia(BaseRunActivity activity) {
        Logger.i("R.id.iv_run_media");
        // 1.隐藏中间图表
        activity.runGraph.hide();

        // 2.显示app列表
        final String[] pkgName = HomeAndRunAppUtils.getPkgNames();
        int[] drawable = HomeAndRunAppUtils.getHomeDrawables();
        String[] apkViewNames = HomeAndRunAppUtils.getViewNames();

        HomeAppAdapter appAdapter = new HomeAppAdapter(activity, drawable);
        appAdapter.setNames(apkViewNames);
        appAdapter.setOnItemClick(position -> {
            BuzzerManager.getInstance().buzzerRingOnce();
            // activity.isGoMedia = true;
            // enterThirdApk(runMode, pkgName[position]);
            // activity.rl_main.setVisibility(View.GONE);

            String mediaPkName = pkgName[position];
            Logger.i("点击了 " + mediaPkName + "   " + mediaPkName);
        });

        RecyclerView rv_media_app = activity.findViewById(R.id.rv_media_app);

        GridLayoutManager glm1 = new GridLayoutManager(activity, 2);
        glm1.setOrientation(RecyclerView.HORIZONTAL);
        rv_media_app.setLayoutManager(glm1);
        // 跟显示滑动条有关
        rv_media_app.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = 0;
                outRect.bottom = 0;
                outRect.top = 0;
                outRect.right = 0;
            }
        });
        rv_media_app.setAdapter(appAdapter);

        activity.run_media_application.setVisibility(View.VISIBLE);

    }
}
