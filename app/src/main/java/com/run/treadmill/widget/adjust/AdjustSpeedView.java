package com.run.treadmill.widget.adjust;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.run.treadmill.R;
import com.run.treadmill.util.Logger;

public class AdjustSpeedView extends RelativeLayout {
    public AdjustSpeedView(Context context) {
        super(context);
    }

    public AdjustSpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View myView = inflate(getContext(), R.layout.adjust_speed, this);
        View iv_run_add = myView.findViewById(R.id.iv_run_add);
        iv_run_add.setOnClickListener((v) -> {
            Logger.i("adjust_speed");
        });
    }

    public AdjustSpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdjustSpeedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
