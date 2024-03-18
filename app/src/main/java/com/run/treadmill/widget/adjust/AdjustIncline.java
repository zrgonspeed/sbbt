package com.run.treadmill.widget.adjust;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;

public class AdjustIncline extends RelativeLayout {

    private TextView tv_adjust_value;
    private View iv_run_add;
    private View iv_run_dec;

    public AdjustIncline(Context context) {
        super(context);
    }

    public AdjustIncline(Context context, AttributeSet attrs) {
        super(context, attrs);

        View myView = inflate(getContext(), R.layout.adjust_incline, this);
        iv_run_add = myView.findViewById(R.id.iv_run_add);
        iv_run_dec = myView.findViewById(R.id.iv_run_dec);
        tv_adjust_value = myView.findViewById(R.id.tv_adjust_value);
    }

    public AdjustIncline(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdjustIncline(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        tv_adjust_value.addTextChangedListener(watcher);
    }

    public String getIncline() {
        return tv_adjust_value.getText().toString();
    }

    public void setTextColor(int color) {
        tv_adjust_value.setTextColor(color);
    }

    public void setIncline(CharSequence value) {
        tv_adjust_value.setText(value.toString().trim());
    }

    public void setOnClickAddDec(OnClickListener addClick, OnClickListener decClick) {
        iv_run_add.setOnClickListener(addClick);
        iv_run_dec.setOnClickListener(decClick);
    }
}