package com.run.treadmill.activity.runMode.vision;

import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.activity.runMode.BaseRunActivity;
import com.run.treadmill.activity.summary.SummaryActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.util.MsgWhat;
import com.run.treadmill.base.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.mcu.control.ControlManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.clicktime.ButtonUtilsVision;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.KeyUtils;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.util.UnitUtil;
import com.run.treadmill.widget.VideoPlayerSelf;

import butterknife.BindView;


@CreatePresenter(VisionPresenter.class)
public class VisionActivity extends BaseRunActivity<VisionView, VisionPresenter> implements VisionView, VideoPlayerSelf.OnTimeCallBack {
    private int curInx;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRunningParam.setToPrepare();

        btn_media = findViewById(R.id.btn_media);
        btn_line_chart_incline = (TextView) findViewById(R.id.btn_line_chart_incline);
        btn_line_chart_speed = (TextView) findViewById(R.id.btn_line_chart_speed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        btn_media.setVisibility(View.GONE);
//        img_unit.setVisibility(View.GONE);
//        lineChartView.setVisibility(View.GONE);

        curInx = getIntent().getIntExtra(CTConstant.VR_PATH_INX, -1);
        int duration = getIntent().getIntExtra(CTConstant.VR_PATH_DURATION, -1);
        if (curInx == -1 || curInx >= CTConstant.vrVideoPath.length) {
            tv_tip.setVisibility(View.GONE);
            img_tip.setImageResource(R.drawable.img_pop_sd_error);
            rl_tip.setVisibility(View.VISIBLE);
            return;
        }
        String path = FileUtil.getStoragePath(this, "SD") + CTConstant.vrVideoPath[curInx];

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
        rl_chart_view.setVisibility(View.GONE);
        rl_main.addView(surfaceView, 0);
        surfaceView.setOnClickListener(v -> {
            if (!ButtonUtilsVision.canResponse()) {
                return;
            }

            if (rl_top.getVisibility() == View.VISIBLE) {
                rl_top.setVisibility(View.GONE);
                rl_bottom.setVisibility(View.GONE);
            } else {
                rl_top.setVisibility(View.VISIBLE);
                rl_bottom.setVisibility(View.VISIBLE);
            }
        });

        iv_running_bottom.setClickable(true);
        iv_running_top.setClickable(true);
    }

    @BindView(R.id.iv_running_bottom)
    public ImageView iv_running_bottom;
    @BindView(R.id.iv_running_top)
    public ImageView iv_running_top;

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
        if (!mRunningParam.isPrepare()) {
            btn_start_stop_skip.setImageResource(R.drawable.btn_sportmode_stop);
        }
        if (mRunningParam.isStopStatus()) {
            showPopTip();
        }
    }

    @Override
    public void dataCallback() {
        super.dataCallback();
        if (mRunningParam.isCoolDownStatus() && mRunningParam.getCoolDownTime() == 0) {
            btn_start_stop_skip.performClick();
        }
    }

    @Override
    public void click(View view) {
        super.click(view);
        switch (view.getId()) {
            case R.id.btn_incline_up:
                BuzzerManager.getInstance().buzzerRingOnce();
                speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                getPresenter().setInclineValue(1, 0, false);
                break;
            case R.id.btn_incline_down:
                BuzzerManager.getInstance().buzzerRingOnce();
                speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                getPresenter().setInclineValue(-1, 0, false);
                break;
            case R.id.btn_speed_up:
                BuzzerManager.getInstance().buzzerRingOnce();
                getPresenter().setSpeedValue(1, 0, false);
                break;
            case R.id.btn_speed_down:
                BuzzerManager.getInstance().buzzerRingOnce();
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
        if (mRunningParam.isPrepare()) {
            mRunningParam.setToRunning();
            //getPresenter().initFirstIncline(curInx);
            mRunningParam.setLcCurStageNum(0);
            if (mVideoPlayerSelf != null) {
                mVideoPlayerSelf.setSpeedCtrl(isMetric ? mRunningParam.getCurrSpeed() : UnitUtil.getMileToKmByFloat1(mRunningParam.getCurrSpeed()));
            }
            mRunningParam.startRefreshData();
        }
        if (mRunningParam.isContinue()) {
            mRunningParam.setToRunning();
            mRunningParam.notifyRefreshData();
        }
        ControlManager.getInstance().startRun();
        mVideoPlayerSelf.videoPlayerStart();
    }

    @Override
    public void onTime(int timePosition) {
        // getPresenter().setInclneByTimePosition(timePosition, curInx);
    }

    @Override
    public void onSpeedChange(float speed) {
        setSpeed(getSpeedValue(String.valueOf(speed)));
        if (mVideoPlayerSelf != null) {
            mVideoPlayerSelf.setSpeedCtrl(isMetric ? speed : UnitUtil.getMileToKmByFloat1(speed));
        }
    }

    @Override
    public void onInclineChange(float incline) {
        setIncline(StringUtil.valueAndUnit(String.valueOf((int) incline), getString(R.string.string_unit_percent), runParamUnitTextSize));
    }

    @Override
    public void afterInclineChanged(float incline) {
        if (isCalcDialogShowing()
                || mRunningParam.isWarmStatus()
                || mRunningParam.isCoolDownStatus()) {
            return;
        }
        if (incline <= 0) {
            if (btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(false);
            }
            if (!btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(true);
            }
        } else if (incline >= maxIncline) {
            if (!btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(true);
            }
            if (btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(false);
            }
        } else {
            if (!btn_incline_down.isEnabled()) {
                btn_incline_down.setEnabled(true);
            }
            if (!btn_incline_up.isEnabled()) {
                btn_incline_up.setEnabled(true);
            }
        }
    }

    @Override
    public void afterSpeedChanged(float speed) {
        if (isCalcDialogShowing()
                || mRunningParam.isWarmStatus()
                || mRunningParam.isCoolDownStatus()) {
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
    public void showPopTip() {
        if (mRunningParam.isStopStatus()) {
            // getPresenter().setSpeedValue(0, minSpeed, false);
            // getPresenter().setInclineValue(0, 0, false);
        }
        super.showPopTip();
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
    public void runCmdKeyValue(int keyValue) {
        if (KeyUtils.isInclineKeyAndHasInclineError(keyValue)) {
            return;
        }
        if (KeyUtils.isStopSetSpeed(keyValue) || KeyUtils.isStopSetIncline(keyValue)) {
            return;
        }
        switch (keyValue) {
            case SerialKeyValue.HAND_START_CLICK:
            case SerialKeyValue.START_CLICK:
                if (mRunningParam.isStopStatus()
                        && btn_pause_continue.isEnabled()) {
                    btn_pause_continue.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                }
                break;
            case SerialKeyValue.HAND_STOP_CLICK:
            case SerialKeyValue.STOP_CLICK:
                if (mRunningParam.isCoolDownStatus()
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                if (mRunningParam.isStopStatus()
                        && btn_pause_quit.isEnabled()) {
                    btn_pause_quit.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                if (mRunningParam.isRunning()
                        && btn_start_stop_skip.isEnabled()) {
                    btn_start_stop_skip.performClick();
                    BuzzerManager.getInstance().buzzerRingOnce();
                    break;
                }
                break;
            case SerialKeyValue.STOP_CLICK_LONG_2:

                break;
            case SerialKeyValue.INCLINE_UP_CLICK:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_UP_CLICK_LONG_2:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_2:
                if (btn_incline_up.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(1, 0, false);
                }
                break;
            case SerialKeyValue.INCLINE_DOWN_CLICK:
            case SerialKeyValue.INCLINE_DOWN_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_DOWN_CLICK_LONG_2:
            case SerialKeyValue.INCLINE_DOWN_HAND_CLICK:
            case SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_1:
            case SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_2:
                if (btn_incline_down.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(-1, 0, false);
                }
                break;
            case SerialKeyValue.SPEED_UP_CLICK:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_CLICK_LONG_2:
            case SerialKeyValue.SPEED_UP_HAND_CLICK:
            case SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_1:
            case SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_2:
                if (btn_speed_up.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(1, 0, false);
                }
                break;
            case SerialKeyValue.SPEED_DOWN_CLICK:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_CLICK_LONG_2:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_1:
            case SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_2:
                if (btn_speed_down.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(-1, 0, false);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_2_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_INCLINE_12_CLICK:

                if (btn_incline_up.isEnabled() || btn_incline_down.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_INCLINE);
                    getPresenter().setInclineValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_4_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_6_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_8_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_12_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_15_CLICK:
            case SerialKeyValue.QUICK_KEY_EVENT_SPEED_16_CLICK:
                if (btn_speed_up.isEnabled() || btn_speed_down.isEnabled()) {
                    speedInclineClickHandler.sendEmptyMessage(MsgWhat.MSG_CLICK_SPEED);
                    getPresenter().setSpeedValue(0, SerialKeyValue.getKeyRepresentValue(keyValue), false);
                }
                break;
            default:
                break;
        }
    }
}