package com.run.treadmill.bluetooth.window;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.util.Logger;

public class BleConnectButton extends androidx.appcompat.widget.AppCompatButton {
    private final String TAG = this.getClass().getSimpleName();
    private int status = 0;

    public BleConnectButton(@NonNull Context context) {
        super(context);
        Logger.d(TAG, "BleConnectButton(1)");
    }

    /**
     * 回调
     *
     * @param context
     * @param attrs
     */
    public BleConnectButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
//        Logger.e(TAG, "BleConnectButton(2)");
    }

    public BleConnectButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Logger.d(TAG, "BleConnectButton(3)");
    }

    @Override
    public boolean callOnClick() {
        Logger.e(TAG, "callOnClick()");
        return super.callOnClick();
    }

    @Override
    public boolean performClick() {
        Logger.e(TAG, "performClick()");
        return super.performClick();
    }

    public void setConnect() {
        status = 1;
        setText(R.string.workout_ble_connect_state_1);
        setTextColor(getContext().getColor(R.color.ble_bt_connect_1));
        setBackground(getContext().getDrawable(R.drawable.ble_btn_bg_1));
    }

    public void setConnecting() {
        status = 2;

        setText(R.string.workout_ble_connect_state_4);
        setTextColor(getContext().getColor(R.color.ble_bt_connect_1));
        setBackground(getContext().getDrawable(R.drawable.ble_btn_bg_1));
    }

    public void setDisconnect() {
        status = 3;

        setText(R.string.workout_ble_connect_state_2);
        setTextColor(getContext().getColor(R.color.ble_bt_connect_2));
        setBackground(getContext().getDrawable(R.drawable.ble_btn_bg_2));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            switch (status) {
                case 1:
                    setConnect();
                    break;
                case 2:
                    setConnecting();
                    break;
                case 3:
                    setDisconnect();
                    break;
            }
        } else {
//            setText(R.string.workout_ble_connect_state_1);
            setTextColor(getContext().getColor(R.color.ble_bt_connect_3));
            setBackground(getContext().getDrawable(R.drawable.ble_btn_bg_3));
        }
    }
}
