package com.run.treadmill.widget.adjust;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.sp.SpManager;

public class AdjustSpeed extends RelativeLayout {
    private TextView tv_adjust_value;
    private View iv_run_up;
    private View iv_run_down;

    public AdjustSpeed(Context context) {
        super(context);
    }

    public AdjustSpeed(Context context, AttributeSet attrs) {
        super(context, attrs);

        View myView = inflate(getContext(), R.layout.adjust_speed, this);
        iv_run_up = myView.findViewById(R.id.iv_run_up);
        iv_run_down = myView.findViewById(R.id.iv_run_down);
        tv_adjust_value = myView.findViewById(R.id.tv_adjust_value);
    }

    public AdjustSpeed(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdjustSpeed(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        tv_adjust_value.addTextChangedListener(watcher);
    }

    public String getSpeed() {
        return tv_adjust_value.getText().toString();
    }

    public void setTextColor(int color) {
        tv_adjust_value.setTextColor(color);
    }

    public void setSpeed(CharSequence value) {
        tv_adjust_value.setText(value.toString().trim());
    }

    public void setOnClickAddDec(OnClickListener addClick, OnClickListener decClick) {
        iv_run_up.setOnClickListener(addClick);
        iv_run_down.setOnClickListener(decClick);
    }

    public void afterSpeedChanged(float speed) {
        float minSpeed = SpManager.getMinSpeed(SpManager.getIsMetric());
        float maxSpeed = SpManager.getMaxSpeed(SpManager.getIsMetric());

        if (speed <= minSpeed) {
            setDownFalse();
            setUpTrue();
        } else if (speed >= maxSpeed) {
            setDownTrue();
            setUpFalse();
        } else {
            setDownTrue();
            setUpTrue();
        }
    }

    private void setDownTrue() {
        if (!iv_run_down.isEnabled()) {
            iv_run_down.setEnabled(true);
        }
    }

    private void setDownFalse() {
        if (iv_run_down.isEnabled()) {
            iv_run_down.setEnabled(false);
        }
    }

    private void setUpTrue() {
        if (!iv_run_up.isEnabled()) {
            iv_run_up.setEnabled(true);
        }
    }

    private void setUpFalse() {
        if (iv_run_up.isEnabled()) {
            iv_run_up.setEnabled(false);
        }
    }
}
