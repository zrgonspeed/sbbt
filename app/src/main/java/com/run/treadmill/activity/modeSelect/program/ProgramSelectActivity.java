package com.run.treadmill.activity.modeSelect.program;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.activity.modeSelect.BaseSelectActivity;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.activity.runMode.program.ProgramActivity;
import com.run.treadmill.adapter.ProgramModeAdapter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.UserInfoManager;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.widget.HistogramListView;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfSelectMode;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/10/26
 */
@CreatePresenter(ProgramSelectPresenter.class)
public class ProgramSelectActivity extends BaseSelectActivity<ProgramSelectView, ProgramSelectPresenter> implements ProgramSelectView, CalculatorCallBack, RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.rl_one)
    RelativeLayout rl_one;
    @BindView(R.id.rg_info)
    RadioGroup rg_info;
    @BindView(R.id.rb_age)
    RadioButton rb_age;
    @BindView(R.id.rb_weight)
    RadioButton rb_weight;
    @BindView(R.id.txt_weight_unit)
    TextView txt_weight_unit;
    @BindView(R.id.rb_time)
    RadioButton rb_time;

    @BindView(R.id.img_gender_select)
    ImageView img_gender_select;
    @BindView(R.id.img_gender_draw)
    public ImageView img_gender_draw;
    @BindView(R.id.rg_gender_info)
    public RadioGroup rg_gender_info;
    @BindView(R.id.tv_next)
    public TextView tv_next;
    @BindView(R.id.btn_next_or_back)
    ImageView btn_next_or_back;

    @BindView(R.id.rl_two)
    RelativeLayout rl_two;
    @BindView(R.id.gridView)
    GridView gridView;
    @BindView(R.id.img_unit)
    ImageView img_unit;

    @BindView(R.id.lineChartView)
    HistogramListView lineChartView;
    @BindView(R.id.btn_line_chart_incline)
    TextView btn_line_chart_incline;
    @BindView(R.id.btn_line_chart_speed)
    TextView btn_line_chart_speed;

    @BindView(R.id.tv_title)
    public TextView tv_title;

    private ProgramModeAdapter pModeAdapter;
    public boolean isLineChartIncline = true;
    private String pModeName = "P1";
    private int lastPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().getPModeDate(lastPosition);
        settingLineChart(false);
    }

    @Override
    protected boolean isCanStart() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_program;
    }

    private void setInfo() {
        tv_title.setText(StringUtil.valueAndIcon(this, getString(R.string.string_mode_program), R.drawable.img_program_interval_icon));
        mCalcBuilder = new BaseCalculator.Builder(new CalculatorOfSelectMode(this));
        //拉取数据
        UserDB userInfo = UserInfoManager.getInstance().getUserInfo(CTConstant.PROGRAM);
        rb_age.setText(String.valueOf(userInfo.getAge()));
        rb_weight.setText(String.valueOf(userInfo.getWeight()));
        rb_time.setText(String.valueOf(userInfo.getTime()));
        txt_weight_unit.setText(getString(isMetric ? R.string.string_unit_kg : R.string.string_unit_lb));
        rg_gender_info.setOnCheckedChangeListener(this);
        rg_gender_info.check(rg_gender_info.getChildAt(userInfo.getGender()).getId());

        pModeAdapter = new ProgramModeAdapter(getApplicationContext(), getPresenter().getPModeImg());
        pModeAdapter.getDrawableSelects()[0] = true;
        gridView.setAdapter(pModeAdapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == lastPosition) {
                return;
            }

            BuzzerManager.getInstance().buzzerRingOnce();

            if (lastPosition >= 0) {
                pModeAdapter.getDrawableSelects()[lastPosition] = false;
            }
            pModeAdapter.getDrawableSelects()[position] = true;
            lastPosition = position;
            pModeAdapter.notifyDataSetChanged();

            pModeName = "P" + (position + 1);
            lineChartView.setModeName(pModeName);
            getPresenter().getPModeDate(position);
        });
        lineChartView.setModeName(pModeName);
    }

    @Override
    public void selectPModeChangeView(float[] incline, float[] speed) {
        if (isLineChartIncline) {
            lineChartView.setValueArray(incline);
        } else {
            lineChartView.setValueArray(speed);
        }
        RunningParam.getInstance().mInclineArray = incline;
        RunningParam.getInstance().mSpeedArray = speed;
        lineChartView.postInvalidate();
    }

    public void settingLineChart(boolean showIncline) {
        isLineChartIncline = showIncline;
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
        img_unit.setImageResource(isLineChartIncline ? R.drawable.img_sportmode_profile_incline_calibration_1 : (isMetric ? R.drawable.img_sportmode_profile_speed_calibration_km_1 : R.drawable.img_sportmode_profile_speed_calibration_mile_1));
        lineChartView.setMaxValue(isLineChartIncline ? InitParam.MAX_INCLINE_MAX : (isMetric ? InitParam.MAX_SPEED_MAX_METRIC : InitParam.MAX_SPEED_MAX_IMPERIAL));

        getPresenter().changeHistogramListView();
    }

    @OnClick({R.id.rb_age, R.id.rb_weight, R.id.rb_time,
            R.id.btn_line_chart_incline, R.id.btn_line_chart_speed,
            R.id.btn_next_or_back, R.id.btn_start})
    public void click(View view) {
        BuzzerManager.getInstance().buzzerRingOnce();
        switch (view.getId()) {
            case R.id.rb_age:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_AGE)
                        .editTypeName(R.string.string_select_age)
                        .callBack(this)
                        .mainView(rl_one)
                        .setXAndY(calcX, calcY)
                        .startPopWindow();
                break;
            case R.id.rb_weight:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_WEIGHT)
                        .editTypeName(R.string.string_select_weight)
                        .callBack(this)
                        .mainView(rl_one)
                        .setXAndY(calcX, calcY)
                        .startPopWindow();
                break;
            case R.id.rb_time:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_TIME)
                        .editTypeName(R.string.string_select_time)
                        .callBack(this)
                        .mainView(rl_one)
                        .setXAndY(calcX, calcY)
                        .startPopWindow();
                break;
            case R.id.btn_line_chart_incline:
                if (isLineChartIncline) {
                    break;
                }
                settingLineChart(true);
                break;
            case R.id.btn_line_chart_speed:
                if (!isLineChartIncline) {
                    break;
                }
                settingLineChart(false);
                break;
            case R.id.btn_next_or_back:
                if (mCalcBuilder.isPopShowing()) {
                    mCalcBuilder.stopPopWin();
                }
                if (rl_one.getVisibility() == View.VISIBLE) {
                    rl_one.setVisibility(View.GONE);
                    rl_two.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.VISIBLE);
                    lineChartView.postInvalidate();
                    btn_next_or_back.setImageResource(R.drawable.btn_factory_back);
                    tv_next.setText(getString(R.string.string_select_back_hint));
                } else {
                    rl_one.setVisibility(View.VISIBLE);
                    rl_two.setVisibility(View.GONE);
                    gridView.setVisibility(View.GONE);
                    btn_next_or_back.setImageResource(R.drawable.btn_next);
                    tv_next.setText(getString(R.string.string_select_next_hint));
                }
                break;
            case R.id.btn_start:
                if (isOnclickStart) {
                    return;
                }
                isOnclickStart = true;
                getPresenter().setUpRunningParam(
                        Integer.valueOf(rb_age.getText().toString()),
                        Integer.valueOf(rb_weight.getText().toString()),
                        Integer.valueOf(rb_time.getText().toString()) * 60,
                        rg_gender_info.getCheckedRadioButtonId() == R.id.rb_male ? InitParam.DEFAULT_GENDER_MALE : InitParam.DEFAULT_GENDER_FEMALE
                );
                Intent intent = new Intent(ProgramSelectActivity.this, ProgramActivity.class);
                intent.putExtra(CTConstant.PROGRAM_MODE_NAME, pModeName);
                startActivity(intent);
                finish();
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
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        BuzzerManager.getInstance().buzzerRingOnce();

        switch (checkedId) {
            default:
                break;
            case R.id.rb_male:
                img_gender_select.setImageResource(R.drawable.btn_male_2);
                img_gender_draw.setImageResource(R.drawable.img_gender_draw_1);
                break;
            case R.id.rb_female:
                img_gender_select.setImageResource(R.drawable.btn_female_2);
                img_gender_draw.setImageResource(R.drawable.img_gender_draw_2);
                break;
        }
    }
}