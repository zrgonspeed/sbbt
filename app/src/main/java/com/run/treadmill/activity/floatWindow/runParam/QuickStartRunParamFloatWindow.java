package com.run.treadmill.activity.floatWindow.runParam;

import android.content.Context;
import android.view.WindowManager;

import com.run.treadmill.R;
import com.run.treadmill.activity.floatWindow.BaseRunParamFloatWindow;

public class QuickStartRunParamFloatWindow extends BaseRunParamFloatWindow {

    public QuickStartRunParamFloatWindow(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    public int layoutXml() {
        return R.layout.float_window_running_param;
    }

    @Override
    public void initSelf() {

    }
}
