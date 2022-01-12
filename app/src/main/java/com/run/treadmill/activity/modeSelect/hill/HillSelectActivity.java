package com.run.treadmill.activity.modeSelect.hill;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.activity.modeSelect.BaseSelectActivity;
import com.run.treadmill.activity.runMode.hill.HillActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.UserInfoManager;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfSelectMode;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/21
 */
@CreatePresenter(HillSelectPresenter.class)
public class HillSelectActivity extends BaseSelectActivity<HillSelectView, HillSelectPresenter> implements HillSelectView, RadioGroup.OnCheckedChangeListener, CalculatorCallBack {

    @BindView(R.id.rl_main)
    public RelativeLayout rl_main;
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
    @BindView(R.id.img_gender_select)
    public ImageView img_gender_select;
    @BindView(R.id.img_gender_draw)
    public ImageView img_gender_draw;
    @BindView(R.id.rg_gender_info)
    public RadioGroup rg_gender_info;
    @BindView(R.id.tv_title)
    public TextView tv_title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected boolean isCanStart() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_hill;
    }

    @OnClick({R.id.rb_age, R.id.rb_weight, R.id.rb_time, R.id.btn_start})
    public void click(View view) {
        BuzzerManager.getInstance().buzzerRingOnce();
        switch (view.getId()) {
            default:
                break;
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
            case R.id.btn_start:
                if (isOnclickStart) {
                    return;
                }
                isOnclickStart = true;
                getPresenter().setUpRunningParam(
                        Integer.valueOf(rb_age.getText().toString()),
                        Integer.valueOf(rb_weight.getText().toString()),
                        Integer.valueOf(rb_time.getText().toString()) * 60,
                        rg_gender_info.getCheckedRadioButtonId() == R.id.rb_male ? InitParam.DEFAULT_GENDER_MALE : InitParam.DEFAULT_GENDER_FEMALE,
                        isMetric
                );
                startActivity(new Intent(this, HillActivity.class));
                finish();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
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

    private void init() {
        tv_title.setText(StringUtil.valueAndIcon(this, getString(R.string.string_mode_hill), R.drawable.img_program_hill_icon));

        mCalcBuilder = new BaseCalculator.Builder(new CalculatorOfSelectMode(this));

        UserDB userInfo = UserInfoManager.getInstance().getUserInfo(CTConstant.HILL);
        rb_age.setText(String.valueOf(userInfo.getAge()));
        rb_weight.setText(String.valueOf(userInfo.getWeight()));
        rb_time.setText(String.valueOf(userInfo.getTime()));

        txt_weight_unit.setText(getString(isMetric ? R.string.string_unit_kg : R.string.string_unit_lb));

        rg_gender_info.setOnCheckedChangeListener(this);
        rg_gender_info.check(rg_gender_info.getChildAt(userInfo.getGender()).getId());
    }
}