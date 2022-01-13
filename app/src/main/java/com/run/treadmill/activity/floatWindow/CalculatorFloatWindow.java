package com.run.treadmill.activity.floatWindow;import android.annotation.SuppressLint;import android.content.Context;import android.graphics.PixelFormat;import android.util.DisplayMetrics;import android.view.Gravity;import android.view.LayoutInflater;import android.view.View;import android.view.WindowManager;import android.widget.EditText;import android.widget.RelativeLayout;import android.widget.TextView;import androidx.annotation.NonNull;import com.run.treadmill.R;import com.run.treadmill.common.CTConstant;import com.run.treadmill.common.InitParam;import com.run.treadmill.manager.BuzzerManager;import com.run.treadmill.manager.SpManager;import com.run.treadmill.widget.calculator.CalculatorCallBack;public class CalculatorFloatWindow implements View.OnClickListener {    private Context mContext;    private TextView txt_calc_cap;    private EditText edit_calc_editText;    private CalculatorCallBack calculatorCallBack;    private int editType = -1;    private boolean isShowing = false;    /**     * 是否公制     */    protected boolean isMetric = false;    /**     * 点击显示数字按键版的TextView     */    private TextView involvedView = null;    private TextView txt_calc_max;    /**     * 浮点小数位     */    private int floatPoint = 0;    private WindowManager mWindowManager;    private WindowManager.LayoutParams wmParams;    private RelativeLayout mFloatWindow = null;    private FloatWindowManager mFloatWindowManager;    public CalculatorFloatWindow(Context context, WindowManager windowManager) {        this.mContext = context;        this.mWindowManager = windowManager;    }    public void startFloat(FloatWindowManager floatWindowManager) {        mFloatWindowManager = floatWindowManager;        DisplayMetrics dm = new DisplayMetrics();        mWindowManager.getDefaultDisplay().getMetrics(dm);        isMetric = SpManager.getIsMetric();        mFloatWindow = createFloatWindow(mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_750_x), mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_525_y));        floatWindowManager.addView(mFloatWindow, wmParams);        init();        showOrHideFloatWindow(true);    }    private RelativeLayout createFloatWindow(int w, int h) {        View view = LayoutInflater.from(mContext).inflate(R.layout.calculator_layout, null);        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));        RelativeLayout mWindow = (RelativeLayout) view;        wmParams = new WindowManager.LayoutParams();        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;        wmParams.format = PixelFormat.RGBA_8888;        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;        wmParams.gravity = Gravity.LEFT | Gravity.TOP;        wmParams.x = mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_420_x);        wmParams.y = mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_135_y);        wmParams.width = w;        wmParams.height = h;        wmParams.windowAnimations = android.R.style.Animation_Translucent;        return mWindow;    }    private void init() {        mFloatWindow.findViewById(R.id.btn_calc_0).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_1).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_2).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_3).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_4).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_5).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_6).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_7).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_8).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_9).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_del).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_close).setOnClickListener(this);        mFloatWindow.findViewById(R.id.btn_calc_enter).setOnClickListener(this);        mFloatWindow.findViewById(R.id.txt_calc_cap).setOnClickListener(this);        edit_calc_editText = (EditText) mFloatWindow.findViewById(R.id.edit_calc_editText);        txt_calc_cap = (TextView) mFloatWindow.findViewById(R.id.txt_calc_cap);        txt_calc_max = (TextView) mFloatWindow.findViewById(R.id.txt_calc_max);    }    public void showOrHideFloatWindow(boolean isShow) {        if (isShow) {            mFloatWindow.setVisibility(View.GONE);            isShowing = false;            if (calculatorCallBack != null) {                calculatorCallBack.onCalculatorDismiss();            }        } else {            mFloatWindow.setVisibility(View.VISIBLE);            isShowing = true;        }    }    public void showOrHideFloatWindow() {        if (mFloatWindow.getVisibility() == View.VISIBLE) {            mFloatWindow.setVisibility(View.GONE);            isShowing = false;        } else {            mFloatWindow.setVisibility(View.VISIBLE);            isShowing = true;        }    }    public void stopFloat() {        mFloatWindowManager.removeView(mFloatWindow);    }    public boolean isShowing() {        return isShowing;    }    public void reset() {        editType = -1;        floatPoint = 0;        txt_calc_cap.setText("");        edit_calc_editText.setText("");    }    /**     * 设置当前输入类型的名称     *     * @param stringID string字符串的id     */    public void setEditTypeName(int stringID) {        txt_calc_cap.setText(stringID);    }    private void setCalcMax(float max) {        txt_calc_max.setText(R.string.string_max);        if (editType == CTConstant.TYPE_INCLINE) {            txt_calc_max.append(" " + ((int) max));        } else {            txt_calc_max.append(" " + max);        }    }    /**     * 设置输入类型     *     * @param type 比如时间,年龄,扬升,速度等     */    public void setEditType(@CTConstant.EditType int type) {        editType = type;        if (type == CTConstant.TYPE_SPEED) {            setCalcMax(SpManager.getMaxSpeed(isMetric));        } else if (type == CTConstant.TYPE_INCLINE) {            setCalcMax(SpManager.getMaxIncline());        } else {            txt_calc_max.setText("");        }    }    public int getEditType() {        return editType;    }    public void setFloatPoint(int point) {        floatPoint = point;    }    /**     * 按下Enter后的结果回调     *     * @param callBack 输出结果回调     */    public void setCalculatorCallBack(@NonNull CalculatorCallBack callBack) {        calculatorCallBack = callBack;    }    @SuppressLint("WrongConstant")    @Override    public void onClick(View view) {        BuzzerManager.getInstance().buzzerRingOnce();        switch (view.getId()) {            case R.id.btn_calc_close:                showOrHideFloatWindow(true);                calculatorCallBack.onCalculatorDismiss();                break;            case R.id.btn_calc_del:                if (edit_calc_editText.getText().toString().trim().length() > 0) {                    deleteEditValue(getEditSelection());                }                break;            case R.id.btn_calc_enter:                if (edit_calc_editText.getText().toString().isEmpty()) {                    break;                }                try {                    String res = checkOutEnter(edit_calc_editText.getText().toString());                    if (involvedView != null) {                        involvedView.setText(res);                    }                    if (calculatorCallBack != null) {                        calculatorCallBack.enterCallBack(editType, res);                    }                } catch (Exception e) {                    e.printStackTrace();                }                showOrHideFloatWindow(true);                break;            case R.id.btn_calc_0:                btnNumClick("0");                break;            case R.id.btn_calc_1:                btnNumClick("1");                break;            case R.id.btn_calc_2:                btnNumClick("2");                break;            case R.id.btn_calc_3:                btnNumClick("3");                break;            case R.id.btn_calc_4:                btnNumClick("4");                break;            case R.id.btn_calc_5:                btnNumClick("5");                break;            case R.id.btn_calc_6:                btnNumClick("6");                break;            case R.id.btn_calc_7:                btnNumClick("7");                break;            case R.id.btn_calc_8:                btnNumClick("8");                break;            case R.id.btn_calc_9:                btnNumClick("9");                break;            default:                break;        }    }    private void btnNumClick(String num) {        addEditValue(num);        edit_calc_editText.setSelection(edit_calc_editText.getText().length());    }    private void addEditValue(String addValue) {        String res = edit_calc_editText.getText().toString().trim();        //达到最大长度并且小数点位于倒数第一        if (res.length() == 5 && res.indexOf(".") == 3) {            return;        }        try {            if (floatPoint == 0) {                res = res + addValue;                res = Integer.valueOf(res).toString();            } else {                res = floatAdd(res, addValue);            }            edit_calc_editText.setText(res);        } catch (Exception e) {            e.printStackTrace();        }    }    private String floatAdd(String src, String addValue) {        if (src.equals("")) {            src = src + addValue;        } else {            String split = "\\.";            String[] ss = src.split(split);            if (ss.length == 1) {                src = ss[0] + addValue;            } else {                src = ss[0] + ss[1] + addValue;            }        }        int tg = Integer.valueOf(src);        float f = (float) (tg / Math.pow(10, floatPoint));        return String.valueOf(f);    }    /**     * 获取光标当前位置     *     * @return 当前输入的光标的位置     */    private int getEditSelection() {        return edit_calc_editText.getSelectionStart();    }    /**     * 删除光标位置的前一位     *     * @param index 当前光标位置     */    private void deleteEditValue(int index) {        if (floatPoint == 0 && (index - 1) >= 0) {            edit_calc_editText.getText().delete((index - 1), index);            return;        }        int res = (int) (Float.valueOf(edit_calc_editText.getText().toString()) * Math.pow(10, floatPoint));        edit_calc_editText.setText(String.valueOf(res / 10 / Math.pow(10, floatPoint)));    }    private String checkOutEnter(String str) {//        float value = (float) (Float.valueOf(str) / Math.pow(10,getFloatPoint()));        float value = Float.valueOf(str);        switch (getEditType()) {            default:                break;            case CTConstant.TYPE_SPEED:                if (value >= SpManager.getMaxSpeed(SpManager.getIsMetric())) {                    value = SpManager.getMaxSpeed(SpManager.getIsMetric());                } else if (value <= SpManager.getMinSpeed(SpManager.getIsMetric())) {                    value = SpManager.getMinSpeed(SpManager.getIsMetric());                }                break;            case CTConstant.TYPE_INCLINE:                if (value >= SpManager.getMaxIncline()) {                    value = SpManager.getMaxIncline();                } else if (value <= 0) {                    value = 0f;                }                break;            case CTConstant.TYPE_FACTORY_RPM:                if (InitParam.MAX_RPM <= value) {                    value = InitParam.MAX_RPM;                } else if (InitParam.MIN_RPM >= value) {                    value = InitParam.MIN_RPM;                }                break;				        }        return String.valueOf(value);    }}