package com.run.treadmill.widget.adjust;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.run.treadmill.R;
import com.run.treadmill.util.Logger;

public class AdjustInclineView extends RelativeLayout {
    public AdjustInclineView(Context context) {
        super(context);
    }

    public AdjustInclineView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View myView = inflate(getContext(), R.layout.adjust_incline, this);
        View iv_run_add = myView.findViewById(R.id.iv_run_add);
        iv_run_add.setOnClickListener((v) -> {
            Logger.i("adjust_incline");
        });
    }

    public AdjustInclineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdjustInclineView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
