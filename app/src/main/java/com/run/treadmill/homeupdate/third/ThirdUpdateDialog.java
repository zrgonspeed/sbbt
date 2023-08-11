package com.run.treadmill.homeupdate.third;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.run.treadmill.R;
import com.run.treadmill.manager.BuzzerManager;

public class ThirdUpdateDialog implements OnClickListener {
    private final String TAG = "ThirdUpdateDialog";

    private Context mContext = null;
    private Dialog mDialog = null;
    private RelativeLayout layout_reset_dialog;
    private ImageView btn_reset_type = null;
    private ImageView btn_pop_confirm = null;
    private ImageView btn_pop_isee = null;
    private View mView = null;
    private boolean isShow = false;

    private OnDiagClick mOnDiagClick;

    public ThirdUpdateDialog(Context context, String tips) {
        mContext = context;
    }

    public ThirdUpdateDialog(Context context) {
        mContext = context;
        initDialog();
    }

    public void initDialog() {
        try {
            mView = LayoutInflater.from(mContext).inflate(R.layout.activity_dialog_third, null);

            layout_reset_dialog = (RelativeLayout) mView.findViewById(R.id.layout_reset_dialog);
            btn_reset_type = (ImageView) mView.findViewById(R.id.btn_reset_type);

            btn_pop_confirm = (ImageView) mView.findViewById(R.id.btn_pop_confirm);
            btn_pop_isee = (ImageView) mView.findViewById(R.id.btn_pop_isee);

            OnKeyListener keyListener = (dialog, keyCode, event) -> {
                return false;
            };

            mDialog = new AlertDialog.Builder(mContext).create();
            mDialog.setOnKeyListener(keyListener);
            mDialog.hide();
            mDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);

            layout_reset_dialog.setOnClickListener(this);
            btn_pop_confirm.setOnClickListener(this);
            btn_pop_isee.setOnClickListener(this);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void showToPopUpDialog(int res) {
        btn_reset_type = (ImageView) mView.findViewById(R.id.btn_reset_type);
        if (-1 != res) {
            btn_reset_type.setImageResource(res);
        }
        if (null != this.mDialog) {
            this.mDialog.show();
            WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mDialog.getWindow().setAttributes(params);
            mDialog.setContentView(mView);
            isShow = true;
        }
    }

    /**
     * @throws
     */
    public void destoryToPopUpDialog() {
        if (null != this.mDialog) {
            this.mDialog.dismiss();
            this.mDialog = null;
            isShow = false;
        }
    }

    public void hidePopUpDialog() {
        if (null != this.mDialog) {
            this.mDialog.hide();
            isShow = false;
        }
    }

    public boolean isShowPopUpDialog() {
        if (null != this.mDialog) {
            return isShow;
            /*return this.mDialog.isShowing();*/
        }
        return false;
    }

    public void setOnDiagClick(OnDiagClick onClick) {
        this.mOnDiagClick = onClick;
    }

    public interface OnDiagClick {
        void onYesClick();
        void onNoClick();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pop_confirm:
                BuzzerManager.getInstance().buzzerRingOnce();
                hidePopUpDialog();
                Log.d(TAG, "btn_pop_confirm");
                mOnDiagClick.onYesClick();
                break;
            case R.id.btn_pop_isee:
                BuzzerManager.getInstance().buzzerRingOnce();
                Log.d(TAG, "btn_pop_isee");
                hidePopUpDialog();
                mOnDiagClick.onNoClick();
                break;
        }
    }
}
