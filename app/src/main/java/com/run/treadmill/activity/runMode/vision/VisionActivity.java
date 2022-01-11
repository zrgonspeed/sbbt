package com.run.treadmill.activity.runMode.vision;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.SurfaceView;
import android.view.View;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.summary.SummaryActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.MsgWhat;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.util.UnitUtil;
import com.run.treadmill.widget.VideoPlayerSelf;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/21
 */
@CreatePresenter(VisionPresenter.class)
public class VisionActivity extends BaseRunActivity<VisionView, VisionPresenter> implements VisionView, VideoPlayerSelf.OnTimeCallBack {
    private int curInx;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRunningParam.runStatus = CTConstant.RUN_STATUS_PREPARE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        curInx = getIntent().getIntExtra(CTConstant.VR_PATH_INX, -1);
        int duration = getIntent().getIntExtra(CTConstant.VR_PATH_DURATION, -1);
        if (curInx == -1 || curInx >= CTConstant.vrVideoPath.length) {
            tv_tip.setVisibility(View.GONE);
            img_tip.setImageResource(R.drawable.img_pop_sd_error);
            rl_tip.setVisibility(View.VISIBLE);
            return;
        }
        String path = CTConstant.vrVideoPath[curInx];

        if (path.isEmpty()) {
            tv_tip.setVisibility(View.GONE);
            img_tip.setImageResource(R.drawable.img_pop_sd_error);
            rl_tip.setVisibility(View.VISIBLE);
            return;
        }

        SurfaceView surfaceView = new SurfaceView(this);

        mVideoPlayerSelf = new VideoPlayerSelf(this, surfaceView, path);
        mVideoPlayerSelf.setMinMaxSpeed(isMetric ? minSpeed : UnitUtil.getMileToKmByFloat1(minSpeed), isMetric ? maxSpeed : UnitUtil.getMileToKmByFloat1(maxSpeed));
        mVideoPlayerSelf.setOnTimeCallBack(UnitUtil.getFloatToIntUp(duration / 1000f / 30f), this);

        btn_incline_down.setEnabled(false);
        btn_incline_up.setEnabled(false);
        btn_incline_roller.setEnabled(false);


        rl_main.addView(surfaceView, 0);
        surfaceView.setOnClickListener(v -> {
            if (rl_top.getVisibility() == View.VISIBLE) {
                rl_top.setVisibility(View.GONE);
                rl_bottom.setVisibility(View.GONE);
            } else {
                rl_top.setVisibility(View.VISIBLE);
                rl_bottom.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void enterCoolDown() {
        super.enterCoolDown();
        if (rl_top.getVisibility() == View.GONE) {
            rl_top.setVisibility(View.VISIBLE);
            rl_bottom.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mRunningParam.setCallback(this);
        if (mRunningParam.runStatus != CTConstant.RUN_STATUS_PREPARE) {
            btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
        }
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP) {
            showPopTip();
        }
    }

    @Override
    public void dataCallback() {
        super.dataCallback();
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_COOL_DOWN && mRunningParam.getCoolDownTime() == 0) {
            btn_start_stop_skip.performClick();
        }
    }

    @Override
    public void click(View view) {
        super.click(view);
        switch (view.getId()) {
            case R.id.btn_speed_up:
                longClickBuzzer(btn_speed_up);
                getPresenter().setSpeedValue(1, 0, false);
                break;
            case R.id.btn_speed_down:
                longClickBuzzer(btn_speed_down);
                getPresenter().setSpeedValue(-1, 0, false);
                break;
            default:
                break;
        }
    }

    @Override
    public void finishRunning() {
        super.finishRunning();
        startActivity(new Intent(this, SummaryActivity.class));
        finish();
    }

    @Override
    public void afterPrepare() {
        btn_incline_roller.setEnabled(false);
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_PREPARE) {
            mRunningParam.runStatus = CTConstant.RUN_STATUS_RUNNING;
            getPresenter().initFirstIncline(curInx);
            mRunningParam.setLcCurStageNum(0);
            if (mVideoPlayerSelf != null) {
                mVideoPlayerSelf.setSpeedCtrl(isMetric ? mRunningParam.getCurrSpeed() : UnitUtil.getMileToKmByFloat1(mRunningParam.getCurrSpeed()));
            }
            mRunningParam.startRefreshData();
        }
        if (mRunningParam.runStatus == CTConstant.RUN_STATUS_CONTINUE) {
            mRunningParam.runStatus = CTConstant.RUN_STATUS_RUNNING;
            mRunningParam.notifyRefreshData();
        }
        ControlManager.getInstance().startRun();
        mVideoPlayerSelf.videoPlayerStart();
    }

    @Override
    public void onTime(int timePosition) {
        getPresenter().setInclneByTimePosition(timePosition, curInx);
    }

    @Override
    public void onSpeedChange(float speed) {
        tv_speed.setText(getSpeedValue(String.valueOf(speed)));
        if (mVideoPlayerSelf != null) {
            mVideoPlayerSelf.setSpeedCtrl(isMetric ? speed : UnitUtil.getMileToKmByFloat1(speed));
        }
    }

    @Override
    public void onInclineChange(float incline) {
        tv_incline.setText(StringUtil.valueAndUnit(String.valueOf((int) incline), getString(R.string.string_unit_percent), runParamUnitTextSize));
    }

    @Override
    public void afterInclineChanged(float incline) {
    }

    @Override
    public void afterSpeedChanged(float speed) {
        if (mCalcBuilder != null && mCalcBuilder.isPopShowing()
                || mRunningParam.runStatus == CTConstant.RUN_STATUS_WARM_UP
                || mRunningParam.runStatus == CTConstant.RUN_STATUS_COOL_DOWN) {
            return;
        }
        if (speed <= minSpeed) {
            if (btn_speed_down.isEnabled()) {
                btn_speed_down.setEnabled(false);
            }
            if (!btn_speed_up.isEnabled()) {
                btn_speed_up.setEnabled(true);
            }
        } else if (speed >= maxSpeed) {
            if (!btn_speed_down.isEnabled()) {
                btn_speed_down.setEnabled(true);
            }
            if (btn_speed_up.isEnabled()) {
                btn_speed_up.setEnabled(false);
            }
        } else {
            if (!btn_speed_down.isEnabled()) {
                btn_speed_down.setEnabled(true);
            }
            if (!btn_speed_up.isEnabled()) {
                btn_speed_up.setEnabled(true);
            }
        }
    }

    @Override
    public void hideTips() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideoPlayerSelf.videoPlayerStartPause();
        mVideoPlayerSelf.onRelease();
    }

    @Override
    protected void runCmdKeyValue(int keyValue) {
        switch (keyValue) {
            case SerialKeyValue.START_CLICK:
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP
                        && btn_pause_continue.isEnabled()) {
                    btn_pause_continue.performClick();
                }
                break;
            case SerialKeyValue.STOP_CLICK:
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_COOL_DOWN
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    break;
                }
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_STOP
                        && btn_pause_quit.isEnabled()) {
                    btn_pause_quit.performClick();
                    break;
                }
                if (mRunningParam.runStatus == CTConstant.RUN_STATUS_RUNNING
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    break;
                }
                break;
            case SerialKeyValue.STOP_CLICK_LONG_2:

                break;
            case SerialKeyValue.SPEED_UP_CLICK:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_2:
                if (btn_speed_up.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(1, 0, false);
                }
                break;
            case SerialKeyValue.SPEED_DOWN_CLICK:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_2:
                if (btn_speed_down.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(-1, 0, false);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_10_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_14_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK:
                if (btn_speed_up.isEnabled() || btn_speed_down.isEnabled()) {
                    BuzzerManager.getInstance().buzzerRingOnce();
                    myHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;
            default:
                break;
        }
    }
}