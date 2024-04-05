package com.run.treadmill.activity.floatWindow.runTop;

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
import com.run.treadmill.activity.floatWindow.FloatWindowManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.WifiBTStateManager;
import com.run.treadmill.util.StringUtil;

public abstract class BaseRunTopFloat {
    private Context mContext;

    private WindowManager mWindowManager;
    private FloatWindowManager mFloatWindowManager;

    private WindowManager.LayoutParams wmParams;
    private RelativeLayout mFloatWindow;

    private TextView tv_incline, tv_time, tv_distance, tv_calories, tv_pulse, tv_mets, tv_speed;
    private ImageView img_voice, img_pulse, img_wifi, img_bt;

    private TextView txt_running_incline_param;

    private Animation pulseAnimation;

    protected BaseRunTopFloat(Context context, WindowManager windowManager) {
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
        wmParams.windowAnimations = 0;

        return mWindow;
    }

    public void startFloat(FloatWindowManager floatWindowManager) {
        this.mFloatWindowManager = floatWindowManager;

        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mFloatWindow = createFloatWindow(dm.widthPixels, mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_180_x));
        floatWindowManager.addView(mFloatWindow, wmParams);

        // pulseAnimation = AnimationUtils.loadAnimation(mContext, R.anim.heart_rate);
        // pulseAnimation.setInterpolator(new LinearInterpolator());

        init();
        if (ErrorManager.getInstance().isHasInclineError()) {
            mFloatWindowManager.inclineError();
        }
    }

    private void init() {
        // txt_running_incline_param = (TextView) mFloatWindow.findViewById(R.id.txt_running_incline_param);
        /*tv_incline = (TextView) mFloatWindow.findViewById(R.id.tv_incline);
        tv_time = (TextView) mFloatWindow.findViewById(R.id.tv_time);
        tv_distance = (TextView) mFloatWindow.findViewById(R.id.tv_distance);
        tv_calories = (TextView) mFloatWindow.findViewById(R.id.tv_calories);
        tv_pulse = (TextView) mFloatWindow.findViewById(R.id.tv_pulse);
        tv_mets = (TextView) mFloatWindow.findViewById(R.id.tv_mets);
        tv_speed = (TextView) mFloatWindow.findViewById(R.id.tv_speed);
        img_pulse = (ImageView) mFloatWindow.findViewById(R.id.img_pulse);
        img_voice = (ImageView) mFloatWindow.findViewById(R.id.img_voice);
        img_wifi = (ImageView) mFloatWindow.findViewById(R.id.img_wifi);
        img_bt = (ImageView) mFloatWindow.findViewById(R.id.img_bt);*/

        /*tv_incline.addTextChangedListener(new BaseRunTopFloat.InclineTextWatcher());
        tv_speed.addTextChangedListener(new BaseRunTopFloat.SpeedTextWatcher());
*/
       /* if (mFloatWindowManager.mRunningParam.isNormal()) {
            tv_speed.setText(mFloatWindowManager.getSpeedValue(String.valueOf(0.0f)));
            tv_incline.setText(StringUtil.valueAndUnit("0", mContext.getString(R.string.string_unit_percent), mFloatWindowManager.runParamUnitTextSize));
        } else {
            tv_speed.setText(mFloatWindowManager.getSpeedValue(String.valueOf(mFloatWindowManager.mRunningParam.getCurrSpeed())));
            tv_incline.setText(StringUtil.valueAndUnit(String.valueOf((int) mFloatWindowManager.mRunningParam.getCurrIncline()), mContext.getString(R.string.string_unit_percent), mFloatWindowManager.runParamUnitTextSize));
        }*/

       /* img_voice.setOnClickListener(v ->
                mFloatWindowManager.showOrHideVoiceFloatWindow()
        );*/
        setRunParam();
    }

    void setData() {
        setRunParam();
    }

    private void setRunParam() {
      /*  tv_time.setText(String.valueOf(mFloatWindowManager.mRunningParam.getShowTime()));
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

        refreshWifiAndBt();*/
    }

    private void refreshWifiAndBt() {
        WifiBTStateManager.setBTWifiStatus(img_wifi, img_bt, mContext);
    }

    public void setSpeedValue(SpannableString speedValue) {
        // tv_speed.setText(speedValue);
    }

    public void setInclineValue(SpannableString inclineValue) {
        // tv_incline.setText(inclineValue);
    }

    void setInclineError(SpannableString inclineValue) {
        // tv_incline.setText(inclineValue);
        // txt_running_incline_param.setTextColor(mContext.getResources().getColor(R.color.red, null));
    }

    public String getInclineStr() {
        return "xxx";
    }

    public void stopFloat() {
        mFloatWindowManager.removeView(mFloatWindow);
    }

    public void showOrHideFloatWindow(boolean isShow) {
        if (isShow) {
            mFloatWindow.setVisibility(View.GONE);
        } else {
            mFloatWindow.setVisibility(View.VISIBLE);
        }
    }

    public void setVoiceEnable(boolean isEnable) {
        // img_voice.setEnabled(isEnable);
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

    public void enterPauseState() {

    }
}