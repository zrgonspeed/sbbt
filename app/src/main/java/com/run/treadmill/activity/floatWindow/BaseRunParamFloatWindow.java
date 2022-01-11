package com.run.treadmill.activity.floatWindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.WifiBTStateManager;
import com.run.treadmill.util.StringUtil;

public abstract class BaseRunParamFloatWindow {
    private Context mContext;

    private WindowManager mWindowManager;
    private FloatWindowManager mFloatWindowManager;

    private WindowManager.LayoutParams wmParams;
    private RelativeLayout mFloatWindow;

    private TextView tv_incline, tv_time, tv_distance, tv_calories, tv_pulse, tv_mets, tv_speed;
    private ImageView img_voice, img_pulse, img_wifi, img_bt;

    private TextView txt_running_incline_param;

    private Animation pulseAnimation;

    protected BaseRunParamFloatWindow(Context context, WindowManager windowManager) {
        this.mContext = context;
        this.mWindowManager = windowManager;
    }

    /**
     * <br>根据运动模式有可能出现界面参数不一样的情况,需要返回对应的布局(注意,不能改变基础布局的默认ID,只能新增)</br>
     */
    public abstract int layoutXml();

    private RelativeLayout createFloatWindow(int w, int h) {
        View view = LayoutInflater.from(mContext).inflate(layoutXml(), null);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        RelativeLayout mWindow = (RelativeLayout) view;
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        wmParams.gravity = Gravity.START | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = w;
        wmParams.height = h;
//        wmParams.windowAnimations = android.R.style.Animation;

        return mWindow;
    }

    void startFloat(FloatWindowManager floatWindowManager) {
        this.mFloatWindowManager = floatWindowManager;

        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mFloatWindow = createFloatWindow(dm.widthPixels, mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_131_y));
        floatWindowManager.addView(mFloatWindow, wmParams);

        pulseAnimation = AnimationUtils.loadAnimation(mContext, R.anim.heart_rate);
        pulseAnimation.setInterpolator(new LinearInterpolator());

        init();
        if (ErrorManager.getInstance().isHasInclineError()) {
            mFloatWindowManager.inclineError();
        }
    }

    /**
     * <br>根据运动模式有可能出现界面参数不一样的情况,这个时候需要在该模式添加额外处理</br>
     * <br>但是对于布局参数必须要写在BaseRunParamFloatWindow类里面</br>
     */
    public abstract void initSelf();

    private void init() {
        txt_running_incline_param = (TextView) mFloatWindow.findViewById(R.id.txt_running_incline_param);
        tv_incline = (TextView) mFloatWindow.findViewById(R.id.tv_incline);
        tv_time = (TextView) mFloatWindow.findViewById(R.id.tv_time);
        tv_distance = (TextView) mFloatWindow.findViewById(R.id.tv_distance);
        tv_calories = (TextView) mFloatWindow.findViewById(R.id.tv_calories);
        tv_pulse = (TextView) mFloatWindow.findViewById(R.id.tv_pulse);
        tv_mets = (TextView) mFloatWindow.findViewById(R.id.tv_mets);
        tv_speed = (TextView) mFloatWindow.findViewById(R.id.tv_speed);
        img_pulse = (ImageView) mFloatWindow.findViewById(R.id.img_pulse);
        img_voice = (ImageView) mFloatWindow.findViewById(R.id.img_voice);
        img_wifi = (ImageView) mFloatWindow.findViewById(R.id.img_wifi);
        img_bt = (ImageView) mFloatWindow.findViewById(R.id.img_bt);
        initSelf();

        tv_incline.addTextChangedListener(new BaseRunParamFloatWindow.InclineTextWatcher());
        tv_speed.addTextChangedListener(new BaseRunParamFloatWindow.SpeedTextWatcher());

        if (mFloatWindowManager.mRunningParam.runStatus == CTConstant.RUN_STATUS_NORMAL) {
            tv_speed.setText(mFloatWindowManager.getSpeedValue("0"));
            tv_incline.setText(StringUtil.valueAndUnit("0", "%", mFloatWindowManager.runParamUnitTextSize));
        } else {
            tv_speed.setText(mFloatWindowManager.getSpeedValue(String.valueOf(mFloatWindowManager.mRunningParam.getCurrSpeed())));
            tv_incline.setText(StringUtil.valueAndUnit(String.valueOf((int) mFloatWindowManager.mRunningParam.getCurrIncline()), mContext.getString(R.string.string_unit_percent), mFloatWindowManager.runParamUnitTextSize));
        }

        img_voice.setOnClickListener(v ->
                mFloatWindowManager.showOrHideVoiceFloatWindow()
        );
        setRunParam();
    }

    void setData() {
        setRunParam();
    }

    private void setRunParam() {
        tv_time.setText(String.valueOf(mFloatWindowManager.mRunningParam.getShowTime()));
        tv_distance.setText(getDistenceValue(mFloatWindowManager.mRunningParam.getShowDistance()));
        tv_calories.setText(StringUtil.valueAndUnit(mFloatWindowManager.mRunningParam.getShowCalories(), mContext.getString(R.string.string_unit_kcal), mFloatWindowManager.runParamUnitTextSize));
        tv_pulse.setText(mFloatWindowManager.mRunningParam.getShowPulse());
        tv_mets.setText(mFloatWindowManager.mRunningParam.getShowMets());

        if (Integer.valueOf(mFloatWindowManager.mRunningParam.getShowPulse()) > 0) {
            if (img_pulse.getAnimation() == null) {
                img_pulse.startAnimation(pulseAnimation);
            }
        } else if (img_pulse.getAnimation() != null && img_pulse.getAnimation().hasStarted()) {
            img_pulse.clearAnimation();
        }

        refreshWifiAndBt();
    }

    private void refreshWifiAndBt() {
        WifiBTStateManager.setBTWifiStatus(img_wifi, img_bt, mContext);
    }

    void setSpeedValue(SpannableString speedValue) {
        tv_speed.setText(speedValue);
    }

    void setInclineValue(SpannableString inclineValue) {
        tv_incline.setText(inclineValue);
    }

    void setInclineError(SpannableString inclineValue) {
        tv_incline.setText(inclineValue);
        txt_running_incline_param.setTextColor(mContext.getResources().getColor(R.color.red, null));
    }

    String getInclineStr() {
        return tv_incline.getText().toString();
    }

    void stopFloat() {
        mFloatWindowManager.removeView(mFloatWindow);
    }

    void showOrHideFloatWindow(boolean isShow) {
        if (isShow) {
            mFloatWindow.setVisibility(View.GONE);
        } else {
            mFloatWindow.setVisibility(View.VISIBLE);
        }
    }

    void setVoiceEnable(boolean isEnable) {
        img_voice.setEnabled(isEnable);
    }

    /**
     * 获取带单位的距离值
     *
     * @param distance
     * @return
     */
    private SpannableString getDistenceValue(String distance) {
        return StringUtil.valueAndUnit(distance,
                mFloatWindowManager.isMetric ? mContext.getString(R.string.string_unit_km) : mContext.getString(R.string.string_unit_mile),
                mFloatWindowManager.runParamUnitTextSize);
    }

    private class InclineTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mFloatWindowManager.afterInclineChanged(Float.valueOf(StringUtil.removeUnit(s.toString())));
        }
    }

    private class SpeedTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mFloatWindowManager.afterSpeedChanged(Float.valueOf(StringUtil.removeUnit(s.toString())));
        }
    }
}
