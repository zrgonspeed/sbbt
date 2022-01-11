package com.run.treadmill.widget;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.run.treadmill.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Description 自定义view 弹出窗
 * @Author GaleLiu
 * @Time 2019/01/25
 */
public class ViewDialog extends Dialog {
    /**
     * 安全key
     */
    public static final int LEVEL_SAFE = 1000;
    /**
     * 通信超时
     */
    public static final int LEVEL_TIME_OUT = 1001;
    /**
     * 警告
     */
    public static final int LEVEL_WARN = 1002;
    /**
     * 普通
     */
    public static final int LEVEL_COMMON = 1003;

    private int mDialogLevel = LEVEL_COMMON;

    public void setmDialogLevel(int mDialogLevel) {
        this.mDialogLevel = mDialogLevel;
    }

    public int getmDialogLevel() {
        return mDialogLevel;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @IntDef({LEVEL_SAFE, LEVEL_TIME_OUT, LEVEL_WARN, LEVEL_COMMON})
    public @interface Level {
    }

    public ViewDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {
        private View mLayout;
        private RelativeLayout parent;

        private ViewDialog mDialog;

        private boolean mIsCanceledOnTouchOutside;

        public Builder(Context context) {
            mDialog = new ViewDialog(context, R.style.AlertDialogStyle);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            parent = (RelativeLayout) inflater.inflate(R.layout.layout_dialog, null);
            mDialog.addContentView(parent, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        /**
         * 设置dialog的布局
         *
         * @param view
         * @return
         */
        public Builder setView(View view) {
            mLayout = view;
            return this;
        }

        /**
         * 是否能手动关闭
         *
         * @param c
         * @return
         */
        public Builder setCanceledOnTouchOutside(boolean c) {
            this.mIsCanceledOnTouchOutside = c;
            return this;
        }

        public ViewDialog create() {
            mDialog.setContentView(mLayout);
            mDialog.setCancelable(mIsCanceledOnTouchOutside);
            mDialog.setCanceledOnTouchOutside(mIsCanceledOnTouchOutside);
            return mDialog;
        }
    }
}
