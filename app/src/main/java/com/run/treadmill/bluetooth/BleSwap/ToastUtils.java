package com.run.treadmill.bluetooth.BleSwap;

import android.content.Context;
import android.widget.Toast;

import com.run.treadmill.R;

public class ToastUtils {
    private static Context context;
    // Toast对象
    private static Toast toast;
    // 文字显示的颜色 <color name="white">#FFFFFFFF</color>
    private static final int messageColor = R.color.color_white;

    /**
     * 在Application中初始化ToastUtils.init(this)
     *
     * @param context
     */
    public static void init(Context context) {
        ToastUtils.context = context.getApplicationContext();
    }

    /**
     * 发送Toast,默认LENGTH_SHORT
     *
     * @param resId
     */
    public static void show(int resId, int duration) {
        showToast(context, context.getString(resId), duration);//Toast.LENGTH_SHORT
    }

    public static void show(String str, int duration) {
        showToast(context, str, duration);
    }

    private static void showToast(Context context, String massage, int duration) {
        cancel();
        // 设置显示文字的颜色
        /*SpannableString spannableString = new SpannableString(massage);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(context, messageColor));
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
        if (toast == null) {
            //toast = Toast.makeText(context, spannableString, duration);
            toast = Toast.makeText(context, massage, duration);
        } else {
            //toast.setText(spannableString);
            toast.setText(massage);
            toast.setDuration(duration);
        }
        // 设置显示的背景
        //View view = toast.getView();
        //view.setBackgroundResource(R.drawable.toast_frame_style);
        // 设置Toast要显示的位置，水平居中并在底部，X轴偏移0个单位，Y轴偏移200个单位，
        //toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 200);
        toast.show();
    }

    /**
     * 在UI界面隐藏或者销毁前取消Toast显示
     */
    public static void cancel() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
    }

    /*
    <?xml version="1.0" encoding="utf-8"?>
    <shape xmlns:android="http://schemas.android.com/apk/res/android"
           android:shape="rectangle">
        <corners android:radius="1000dp"/>
        <solid android:color="@color/colorPrimaryDark"/>
        <stroke
            android:width="0.5dp"
            android:color="@color/colorAccent"/>
        <padding
            android:top="10dp"
            android:bottom="10dp"
            android:left="10dp"
            android:right="10dp"/>
    </shape>*/

}
