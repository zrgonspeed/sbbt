package com.run.treadmill.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.run.treadmill.R;
import com.run.treadmill.util.Logger;

public class AdjustView extends RelativeLayout {
    public AdjustView(Context context) {
        super(context);
    }

    public AdjustView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View myView = inflate(getContext(), R.layout.run_float_adjust, this);
        View iv_run_add = myView.findViewById(R.id.iv_run_add);
        iv_run_add.setOnClickListener((v) -> {
            Logger.i("sssssssss");
        });
    }

    public AdjustView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdjustView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
