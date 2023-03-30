package com.run.treadmill.activity.modeSelect.userprogram;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.activity.ActionModeCallbackInterceptor;
import com.run.treadmill.activity.modeSelect.BaseSelectActivity;
import com.run.treadmill.activity.runMode.userProgram.UserProgramActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.util.Logger;
import com.run.treadmill.widget.HistogramListView;
import com.run.treadmill.widget.MyYaxisViewManager;
import com.run.treadmill.widget.YaxisView;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfSelectMode;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/31
 */
@CreatePresenter(UserProgramSelectPresenter.class)
public class UserProgramSelectActivity extends BaseSelectActivity<UserProgramSelectView, UserProgramSelectPresenter> implements UserProgramSelectView, RadioGroup.OnCheckedChangeListener, CalculatorCallBack {

    @BindView(R.id.rl_main)
    public RelativeLayout rl_main;
    @BindView(R.id.rl_one)
    public RelativeLayout rl_one;
    @BindView(R.id.rl_two)
    public RelativeLayout rl_two;
    @BindView(R.id.et_name)
    public EditText et_name;
    @BindView(R.id.rg_user)
    public RadioGroup rg_user;
    @BindView(R.id.rg_info)
    public RadioGroup rg_info;
    @BindView(R.id.rb_age)
    public RadioButton rb_age;
    @BindView(R.id.rb_weight)
    public RadioButton rb_weight;
    @BindView(R.id.txt_weight_unit)
    public TextView txt_weight_unit;
    @BindView(R.id.rb_time)
    public RadioButton rb_time;
    @BindView(R.id.btn_next_or_back)
    public ImageView btn_next_or_back;
    @BindView(R.id.img_gender_select)
    public ImageView img_gender_select;
    @BindView(R.id.img_gender_draw)
    public ImageView img_gender_draw;
    @BindView(R.id.rg_gender_info)
    public RadioGroup rg_gender_info;
    @BindView(R.id.rb_male)
    public RadioButton rb_male;
    @BindView(R.id.rb_female)
    public RadioButton rb_female;
    @BindView(R.id.tv_next)
    public TextView tv_next;
    @BindView(R.id.img_unit)
    public ImageView img_unit;
    @BindView(R.id.lineChartView)
    public HistogramListView lineChartView;
    @BindView(R.id.btn_line_chart_incline)
    public TextView btn_line_chart_incline;
    @BindView(R.id.btn_line_chart_speed)
    public TextView btn_line_chart_speed;

    /**
     * 当前选择的用户（1,2,3,4）
     */
    private int currInx = 1;
    /**
     * 当前用户
     */
    private UserDB currUser;
    /**
     * 当前是否是调整扬升
     */
    private boolean isLineChartIncline = true;
    private float[] inclineArray = new float[InitParam.TOTAL_RUN_STAGE_NUM], speedArray = new float[InitParam.TOTAL_RUN_STAGE_NUM];

    private InputMethodManager imm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private boolean firstBuzzer = false;

    @Override
    protected void onResume() {
        super.onResume();
        firstBuzzer = true;
        // rg_user.check(R.id.rb_user_1);
        ((RadioButton) (findViewById(R.id.rb_user_1))).setChecked(true);
        settingLineChart();
    }

    private void init() {

        getPresenter().initUserInfo();
        mCalcBuilder = new BaseCalculator.Builder(new CalculatorOfSelectMode(this));
        rl_main.setFocusable(true);
        rl_main.setFocusableInTouchMode(true);

        lineChartView.setMaxDrawValue(isLineChartIncline ? SpManager.getMaxIncline() : SpManager.getMaxSpeed(isMetric));
        lineChartView.setMinDrawValue(isLineChartIncline ? InitParam.DEFAULT_INCLINE : SpManager.getMinSpeed(isMetric));
        lineChartView.setClickAble(true);
        lineChartView.invalidate();

        txt_weight_unit.setText(getString(isMetric ? R.string.string_unit_kg : R.string.string_unit_lb));

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        et_name.setOnEditorActionListener((v, actionId, event) -> {
            et_name.clearFocus();
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            return true;
        });

        et_name.setOnTouchListener((v, event) -> {
            if (mCalcBuilder.isPopShowing()) {
                mCalcBuilder.stopPopWin();
            }
            return false;
        });

        rg_gender_info.setOnCheckedChangeListener(this);
        rg_user.setOnCheckedChangeListener(this);

        ActionModeCallbackInterceptor interceptor = new ActionModeCallbackInterceptor();

        et_name.setCustomSelectionActionModeCallback(interceptor);
        et_name.setCustomInsertionActionModeCallback(interceptor);
    }

    @Override
    protected boolean isCanStart() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_user_program;
    }

    @OnClick({R.id.btn_next_or_back, R.id.btn_start, R.id.rb_age, R.id.rb_weight, R.id.rb_time, R.id.btn_line_chart_incline, R.id.btn_line_chart_speed})
    public void click(View view) {
        BuzzerManager.getInstance().buzzerRingOnce();
        switch (view.getId()) {
            case R.id.rb_age:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_AGE)
                        .editTypeName(R.string.string_select_age)
                        .callBack(this)
                        .mainView(rl_main)
                        .setXAndY(calcX, calcY)
                        .startPopWindow();
                break;
            case R.id.rb_weight:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_WEIGHT)
                        .editTypeName(R.string.string_select_weight)
                        .callBack(this)
                        .mainView(rl_main)
                        .setXAndY(calcX, calcY)
                        .startPopWindow();
                break;
            case R.id.rb_time:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_TIME)
                        .editTypeName(R.string.string_select_time)
                        .callBack(this)
                        .mainView(rl_main)
                        .setXAndY(calcX, calcY)
                        .startPopWindow();
                break;
            case R.id.btn_line_chart_incline:
                if (isLineChartIncline) {
                    return;
                }
                isLineChartIncline = true;
                lineChartView.setType(HistogramListView.TYPE_INCLINE);
                System.arraycopy(lineChartView.getValueArray(), 0, speedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                refreshLine();
                settingLineChart();
                break;
            case R.id.btn_line_chart_speed:
                if (!isLineChartIncline) {
                    return;
                }
                isLineChartIncline = false;
                lineChartView.setType(HistogramListView.TYPE_SPEED);
                System.arraycopy(lineChartView.getValueArray(), 0, inclineArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                refreshLine();
                settingLineChart();
                break;
            case R.id.btn_next_or_back:
                if (mCalcBuilder.isPopShowing()) {
                    mCalcBuilder.stopPopWin();
                }
                if (rl_one.getVisibility() == View.VISIBLE) {
                    rl_one.setVisibility(View.GONE);
                    rl_two.setVisibility(View.VISIBLE);
                    btn_next_or_back.setImageResource(R.drawable.btn_factory_back);
                    tv_next.setText(getString(R.string.string_select_back_hint));
                } else {
                    rl_one.setVisibility(View.VISIBLE);
                    rl_two.setVisibility(View.GONE);
                    btn_next_or_back.setImageResource(R.drawable.btn_next);
                    tv_next.setText(getString(R.string.string_select_next_chart_hint));
                }
                break;
            case R.id.btn_start:
                if (isOnclickStart) {
                    return;
                }
                isOnclickStart = true;
                //直接按开始需要复制一份当前条形图的数据
                if (isLineChartIncline) {
                    System.arraycopy(lineChartView.getValueArray(), 0, inclineArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                } else {
                    System.arraycopy(lineChartView.getValueArray(), 0, speedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
                }
                currUser.setName(et_name.getText().toString());
                currUser.setAge(Integer.valueOf(rb_age.getText().toString()));
                currUser.setWeight(Integer.valueOf(rb_weight.getText().toString()));
                currUser.setTime(Integer.valueOf(rb_time.getText().toString()));

                getPresenter().saveOrUpdateUserInfo(currUser, inclineArray, speedArray);

                Intent intent = new Intent(UserProgramSelectActivity.this, UserProgramActivity.class);
//                String modeName = getApplicationContext().getString(R.string.string_mode_user_program_2) + currInx;
//                intent.putExtra(CTConstant.PROGRAM_MODE_NAME, modeName);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
        if (view.getId() != R.id.et_name && imm != null && imm.isActive()) {
            et_name.clearFocus();
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (!firstBuzzer) {
            BuzzerManager.getInstance().buzzerRingOnce();
        } else {
            firstBuzzer = false;
        }
        switch (checkedId) {
            case R.id.rb_user_1:
                currInx = 1;
                getPresenter().getUserInfo(currInx);
                break;
            case R.id.rb_user_2:
                currInx = 2;
                getPresenter().getUserInfo(currInx);
                break;
            case R.id.rb_user_3:
                currInx = 3;
                getPresenter().getUserInfo(currInx);
                break;
            case R.id.rb_user_4:
                currInx = 4;
                getPresenter().getUserInfo(currInx);
                break;
            case R.id.rb_user_5:
                currInx = 5;
                getPresenter().getUserInfo(currInx);
                break;
            case R.id.rb_male:
                img_gender_select.setImageResource(R.drawable.btn_male_2);
                img_gender_draw.setImageResource(R.drawable.img_gender_draw_1);
                currUser.setGender(InitParam.DEFAULT_GENDER_MALE);
                break;
            case R.id.rb_female:
                img_gender_select.setImageResource(R.drawable.btn_female_2);
                img_gender_draw.setImageResource(R.drawable.img_gender_draw_2);
                currUser.setGender(InitParam.DEFAULT_GENDER_FEMALE);
                break;
            default:
                break;
        }
    }

    @Override
    public void enterCallBack(int type, String value) {
        switch (type) {
            case CTConstant.TYPE_AGE:
                rb_age.setText(value);
                break;
            case CTConstant.TYPE_WEIGHT:
                rb_weight.setText(value);
                break;
            case CTConstant.TYPE_TIME:
                rb_time.setText(value);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCalculatorDismiss() {
        rg_info.clearCheck();
    }

    @Override
    public void setUserInfo(UserDB user) {
        currUser = user;
        et_name.setText(user.getName());
        rb_age.setText(String.valueOf(user.getAge()));
        rb_weight.setText(String.valueOf(user.getWeight()));
        rb_time.setText(String.valueOf(user.getTime()));
        if (user.getGender() == InitParam.DEFAULT_GENDER_MALE) {
            rb_male.performClick();
        } else {
            rb_female.performClick();
        }
    }

    @Override
    public void setUserCustomLineData(float[] inclines, float[] speeds) {
        System.arraycopy(inclines, 0, inclineArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);
        System.arraycopy(speeds, 0, speedArray, 0, InitParam.TOTAL_RUN_STAGE_NUM);

        refreshLine();
    }

    private void refreshLine() {
        lineChartView.setValueArray(isLineChartIncline ? inclineArray : speedArray);//30段的数据

        lineChartView.postInvalidate();
    }

    public void settingLineChart() {
        if (isLineChartIncline) {
            btn_line_chart_speed.setTextColor(getColor(R.color.gray));
            btn_line_chart_incline.setTextColor(getColor(R.color.running_text_orange));
            btn_line_chart_incline.setBackground(getDrawable(R.drawable.tx_fillet_max_bg));
            btn_line_chart_speed.setBackground(getDrawable(R.drawable.tx_fillet_small_bg));

            RelativeLayout.LayoutParams speedParams = (RelativeLayout.LayoutParams) btn_line_chart_speed.getLayoutParams();
            speedParams.width = getResources().getDimensionPixelSize(R.dimen.dp_px_400_x);
            speedParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.dp_px_1120_x));
            btn_line_chart_speed.setLayoutParams(speedParams);

            RelativeLayout.LayoutParams inclineParams = (RelativeLayout.LayoutParams) btn_line_chart_incline.getLayoutParams();
            inclineParams.width = getResources().getDimensionPixelSize(R.dimen.dp_px_720_x);
            btn_line_chart_incline.setLayoutParams(inclineParams);


        } else {
            btn_line_chart_speed.setTextColor(getColor(R.color.running_text_orange));
            btn_line_chart_incline.setTextColor(getColor(R.color.gray));
            btn_line_chart_incline.setBackground(getDrawable(R.drawable.tx_fillet_small_bg));
            btn_line_chart_speed.setBackground(getDrawable(R.drawable.tx_fillet_max_bg));

            RelativeLayout.LayoutParams speedParams = (RelativeLayout.LayoutParams) btn_line_chart_speed.getLayoutParams();
            speedParams.width = getResources().getDimensionPixelSize(R.dimen.dp_px_720_x);
            speedParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.dp_px_800_x));
            btn_line_chart_speed.setLayoutParams(speedParams);

            RelativeLayout.LayoutParams inclineParams = (RelativeLayout.LayoutParams) btn_line_chart_incline.getLayoutParams();
            inclineParams.width = getResources().getDimensionPixelSize(R.dimen.dp_px_400_x);
            btn_line_chart_incline.setLayoutParams(inclineParams);
        }

        MyYaxisViewManager.selectYaxis(isLineChartIncline, yv_unit);
        // img_unit.setImageResource(isLineChartIncline ? R.drawable.img_sportmode_profile_incline_calibration_1 : (isMetric ? R.drawable.img_sportmode_profile_speed_calibration_km_1 : R.drawable.img_sportmode_profile_speed_calibration_mile_1));
        lineChartView.setMaxValue(isLineChartIncline ? InitParam.MAX_INCLINE_MAX : (isMetric ? InitParam.MAX_SPEED_MAX_METRIC : InitParam.MAX_SPEED_MAX_IMPERIAL));

        lineChartView.setMaxDrawValue(isLineChartIncline ? SpManager.getMaxIncline() : SpManager.getMaxSpeed(isMetric));
        lineChartView.setMinDrawValue(isLineChartIncline ? InitParam.DEFAULT_INCLINE : SpManager.getMinSpeed(isMetric));
    }

    protected void clickBack() {
        if (rl_two.getVisibility() == View.VISIBLE) {
            rl_one.setVisibility(View.VISIBLE);
            rl_two.setVisibility(View.GONE);
            btn_next_or_back.setImageResource(R.drawable.btn_next);
            tv_next.setText(getString(R.string.string_select_next_chart_hint));
        } else {
            goHome(null);
        }
    }

    @BindView(R.id.yv_unit)
    public YaxisView yv_unit;
}