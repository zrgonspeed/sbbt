package com.run.treadmill.bluetooth.window;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.run.treadmill.R;

public class BleLoading extends ProgressBar {
    private boolean animating;

    public BleLoading(Context context) {
        super(context);
    }

    public BleLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BleLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BleLoading(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean isAnimating() {
        return animating;
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }

    public void start() {
        setAnimating(true);
        setIndeterminateDrawable(getContext().getDrawable(R.drawable.progressbar_circle));
        setProgressDrawable(getContext().getDrawable(R.drawable.progressbar_circle));
    }

    public void stop() {
        setAnimating(false);
        setIndeterminateDrawable(getContext().getDrawable(R.drawable.btn_setting_bluetooth_connect));
        setProgressDrawable(getContext().getDrawable(R.drawable.btn_setting_bluetooth_connect));
    }
}
