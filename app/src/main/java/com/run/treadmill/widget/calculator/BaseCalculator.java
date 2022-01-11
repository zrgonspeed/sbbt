package com.run.treadmill.widget.calculator;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.util.UnitUtil;

public abstract class BaseCalculator implements View.OnClickListener {
    private Context context;
    private PopupWindow popupWindow = null;

//    private ImageView img_calc_cap;

    private TextView txt_calc_cap, txt_calc_max;

    private EditText edit_calc_editText;

    private CalculatorCallBack calculatorCallBack;
    private int editType = -1;

    /**
     * 点击显示数字按键版的TextView
     */
    private TextView involvedView = null;

    /**
     * 浮点小数位
     */
    private int floatPoint = 0;

    /**
     * 是否公制
     */
    protected boolean isMetric = false;

    protected BaseCalculator(Context context) {
        this.context = context;
        onCreate();
    }

    private void onCreate() {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.calculator_layout, null);
        view.findViewById(R.id.btn_calc_0).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_1).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_2).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_3).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_4).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_5).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_6).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_7).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_8).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_9).setOnClickListener(this);

        view.findViewById(R.id.btn_calc_del).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_close).setOnClickListener(this);
        view.findViewById(R.id.btn_calc_enter).setOnClickListener(this);

        view.findViewById(R.id.txt_calc_cap).setOnClickListener(this);

        edit_calc_editText = (EditText) view.findViewById(R.id.edit_calc_editText);
        txt_calc_cap = (TextView) view.findViewById(R.id.txt_calc_cap);
        txt_calc_max = (TextView) view.findViewById(R.id.txt_calc_max);

        initScreen();
        isMetric = SpManager.getIsMetric();
        popupWindow = new PopupWindow(view, context.getResources().getDimensionPixelSize(R.dimen.dp_px_520_x), context.getResources().getDimensionPixelSize(R.dimen.dp_px_370_y));
    }

    private void initScreen() {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(outMetrics);
        }
    }

    @Override
    public void onClick(View view) {
        BuzzerManager.getInstance().buzzerRingOnce();
        switch (view.getId()) {
            case R.id.btn_calc_close:
                stopPopWindow();
                break;
            case R.id.btn_calc_del:
                if (edit_calc_editText.getText().toString().trim().length() > 0) {
                    deleteEditValue(getEditSelection());
                }
                break;
            case R.id.btn_calc_enter:
                if (edit_calc_editText.getText().toString().isEmpty()) {
                    break;
                }
                try {
                    String res = checkOutEnter(edit_calc_editText.getText().toString());
                    if (editType == CTConstant.TYPE_SETTING_LOCK_RESET) {
                        if (res.length() != 4) {
                            return;
                        }
                    }
                    if (involvedView != null) {
                        involvedView.setText(res);
                    }
                    if (calculatorCallBack != null) {
                        calculatorCallBack.enterCallBack(editType, res);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopPopWindow();
                break;
            case R.id.btn_calc_0:
                btnNumClick("0");
                break;
            case R.id.btn_calc_1:
                btnNumClick("1");
                break;
            case R.id.btn_calc_2:
                btnNumClick("2");
                break;
            case R.id.btn_calc_3:
                btnNumClick("3");
                break;
            case R.id.btn_calc_4:
                btnNumClick("4");
                break;
            case R.id.btn_calc_5:
                btnNumClick("5");
                break;
            case R.id.btn_calc_6:
                btnNumClick("6");
                break;
            case R.id.btn_calc_7:
                btnNumClick("7");
                break;
            case R.id.btn_calc_8:
                btnNumClick("8");
                break;
            case R.id.btn_calc_9:
                btnNumClick("9");
                break;
            default:
                break;
        }

    }

    private void btnNumClick(String num) {
        addEditValue(num);
        edit_calc_editText.setSelection(edit_calc_editText.getText().length());
    }

    private void addEditValue(String addValue) {
        String res = edit_calc_editText.getText().toString().trim();
        //达到最大长度并且小数点位于倒数第一
        if (res.length() == 5 && res.indexOf(".") == 3) {
            return;
        }
        try {
            if (floatPoint == 0) {
                res = res + addValue;
                if (editType != CTConstant.TYPE_SETTING_LOCK && editType != CTConstant.TYPE_SETTING_LOCK_RESET) {
                    res = Integer.valueOf(res).toString();
                }
            } else {
                res = floatAdd(res, addValue);
            }
            edit_calc_editText.setText(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String floatAdd(String src, String addValue) {
        if (src.equals("")) {
            src = src + addValue;
        } else {
            String split = "\\.";
            String[] ss = src.split(split);
            if (ss.length == 1) {
                src = ss[0] + addValue;
            } else {
                src = ss[0] + ss[1] + addValue;
            }
        }
        int tg = Integer.valueOf(src);
        float f = (float) (tg / Math.pow(10, floatPoint));
        return UnitUtil.getPoint(f, floatPoint);
    }

    /**
     * 获取光标当前位置
     *
     * @return 当前输入的光标的位置
     */
    private int getEditSelection() {
        return edit_calc_editText.getSelectionStart();
    }

    /**
     * 删除光标位置的前一位
     *
     * @param index 当前光标位置
     */
    private void deleteEditValue(int index) {
        if (floatPoint == 0 && (index - 1) >= 0) {
            edit_calc_editText.getText().delete((index - 1), index);
            return;
        }

        int res = (int) (Float.valueOf(edit_calc_editText.getText().toString()) * Math.pow(10, floatPoint));
        edit_calc_editText.setText(String.valueOf(res / 10 / Math.pow(10, floatPoint)));
    }

    /**
     * 按下Enter后 对输入框的数值与输入类型(setEditType(int type))的极值比较
     *
     * @return 得出最后需要的结果
     */
    abstract String checkOutEnter(String str);

    /**
     * 按下Enter后的结果回调
     *
     * @param callBack 输出结果回调
     */
    public void setCalculatorCallBack(@NonNull CalculatorCallBack callBack) {
        calculatorCallBack = callBack;
    }

    /**
     * 设置输入类型
     *
     * @param type 比如时间,年龄,扬升,速度等
     */
    public void setEditType(@CTConstant.EditType int type) {
        editType = type;
        if (type == CTConstant.TYPE_SPEED) {
            setCalcMax(SpManager.getMaxSpeed(isMetric));
        } else if (type == CTConstant.TYPE_INCLINE) {
            setCalcMax(SpManager.getMaxIncline());
        } else {
            txt_calc_max.setText("");
        }
    }

    private void setCalcMax(float max) {
        txt_calc_max.setText(R.string.string_max);

        if (editType == CTConstant.TYPE_INCLINE) {
            txt_calc_max.append(" " + (int) max);
        } else {
            txt_calc_max.append(" " + max);
        }
    }

    public int getEditType() {
        return editType;
    }

    /**
     * 设置多少位小数点
     *
     * @param point int类型,必须>=0
     */
    public void setFloatPoint(int point) {
        if (point >= 0) {
            floatPoint = point;
        }
    }

    public int getFloatPoint() {
        return floatPoint;
    }

    /**
     * 设置当前输入类型的名称
     *
     * @param stringID string字符串的id
     */
    public void setEditTypeName(int stringID) {
        if (stringID == 0) {
            txt_calc_cap.setText("");
            return;
        }
        txt_calc_cap.setText(stringID);
    }

    public void setInvolvedView(@NonNull TextView view) {
        involvedView = view;
        involvedView.setSelected(true);
    }

    /**
     * 设置公英制
     *
     * @param metric 公英制
     */
    private void setMetric(boolean metric) {
        this.isMetric = metric;
    }

    public TextView getInvolvedView() {
        return involvedView;
    }

    /**
     * 设置最大长度
     *
     * @param length
     */
    private void setMaxLength(int length) {
        edit_calc_editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
    }

    public void startPopWindow(View view, int x, int y) {
        if (popupWindow != null && !popupWindow.isShowing()) {
            popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
        }
    }

    public void stopPopWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            //清掉输入框的内容
            reset();
            popupWindow.dismiss();
            calculatorCallBack.onCalculatorDismiss();
        }
    }

    private void reset() {
        edit_calc_editText.setText("");
        floatPoint = 0;
        involvedView = null;
    }

    public static class Builder {
        private BaseCalculator mBaseCalculator;
        private @CTConstant.EditType
        int mEditType;
        private TextView mInvolvedView;
        private int mEditTypeNameId;
        private int mPoint;
        private int mLength;
        private View mView;
        private int x;
        private int y;
        private CalculatorCallBack mCallBack;

        public Builder(BaseCalculator calculator) {
            this.mBaseCalculator = calculator;
        }

        public Builder editType(@CTConstant.EditType int type) {
            this.mEditType = type;
            return this;
        }

        public Builder involvedView(@NonNull TextView view) {
            this.mInvolvedView = view;
            return this;
        }

        public Builder editTypeName(int stringID) {
            this.mEditTypeNameId = stringID;
            return this;
        }

        public Builder floatPoint(int point) {
            this.mPoint = point;
            return this;
        }

        public Builder maxLength(int length) {
            this.mLength = length;
            return this;
        }

        public Builder callBack(CalculatorCallBack callBack) {
            this.mCallBack = callBack;
            return this;
        }

        public Builder mainView(View view) {
            this.mView = view;
            return this;
        }

        public Builder setXAndY(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        /**
         * 需要调用此方法重置之前设置的值
         *
         * @return
         */
        public Builder reset() {
            mInvolvedView = null;
            mPoint = 0;
            mLength = 5;
            mEditTypeNameId = 0;
            return this;
        }

        /**
         * 设置公英制
         *
         * @param metric 公英制
         */
        public void setMetric(boolean metric) {
            mBaseCalculator.isMetric = metric;
        }

        public void startPopWindow() {
            //按照顺序设置
            mBaseCalculator.setEditType(mEditType);
            if (mInvolvedView != null) {
                mBaseCalculator.setInvolvedView(mInvolvedView);
            }
            mBaseCalculator.setEditTypeName(mEditTypeNameId);
            mBaseCalculator.setFloatPoint(mPoint);
            mBaseCalculator.setMaxLength(mLength);
            if (mCallBack != null) {
                mBaseCalculator.setCalculatorCallBack(mCallBack);
            }
            mBaseCalculator.startPopWindow(mView, x, y);
        }

        /**
         * 数字键盘是否显示
         *
         * @return
         */
        public boolean isPopShowing() {
            if (mBaseCalculator != null && mBaseCalculator.popupWindow != null) {
                return mBaseCalculator.popupWindow.isShowing();
            }
            return false;
        }

        /**
         * 关闭 popWin
         */
        public void stopPopWin() {
            if (mBaseCalculator != null && mBaseCalculator.popupWindow != null) {
                mBaseCalculator.stopPopWindow();
            }
        }
    }
}
