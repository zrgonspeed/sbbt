package com.run.treadmill.activity.modeSelect.goal;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.activity.modeSelect.BaseSelectActivity;
import com.run.treadmill.activity.runMode.goal.GoalActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfGoal;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/27
 */
@CreatePresenter(GoalSelectPresenter.class)
public class GoalSelectActivity extends BaseSelectActivity<GoalSelectView, GoalSelectPresenter> implements GoalSelectView {

    @BindView(R.id.rb_time)
    public TextView rb_time;
    @BindView(R.id.rb_distance)
    public RadioButton rb_distance;
    @BindView(R.id.rb_calories)
    public TextView rb_calories;
    @BindView(R.id.rl_one)
    public RelativeLayout rl_one;
    @BindView(R.id.rl_two)
    public RelativeLayout rl_two;
    @BindView(R.id.btn_selected)
    public TextView btn_selected;
    @BindView(R.id.tv_title)
    public TextView tv_title;

    private @CTConstant.EditType
    int targetType;
    private float targetValue = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tv_title.setText(StringUtil.valueAndIcon(this, getString(R.string.string_mode_goal), R.drawable.img_program_goal_icon));
        mCalcBuilder = new BaseCalculator.Builder(new CalculatorOfGoal(this));
        rb_distance.setBackground(getDrawable(isMetric ? R.drawable.btn_goal_distance_km : R.drawable.btn_goal_distance_mile));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_goal;
    }

    @Override
    protected boolean isCanStart() {
        return (targetValue != -1 && rl_one.getVisibility() == View.VISIBLE);
    }

    private void showSettingGoal() {
        rl_one.setVisibility(View.GONE);
        rl_two.setVisibility(View.VISIBLE);

        mCalcBuilder.callBack(new CalculatorCallBack() {
            @Override
            public void enterCallBack(int type, String value) {
                //这个界面的类型无重复，直接根据类型判断
                targetType = type;
                targetValue = Float.valueOf(value);
                if (type == CTConstant.TYPE_TIME) {
                    rb_time.setText(value);
                } else if (type == CTConstant.TYPE_DISTANCE) {
                    rb_distance.setText(value);
                } else if (type == CTConstant.TYPE_CALORIES) {
                    rb_calories.setText(value);
                }
            }

            @Override
            public void onCalculatorDismiss() {
                if (rl_two.getVisibility() == View.VISIBLE) {
                    rl_one.setVisibility(View.VISIBLE);
                    rl_two.setVisibility(View.GONE);
                }
            }
        }).setXAndY(getResources().getDimensionPixelSize(R.dimen.dp_px_657_x), getResources().getDimensionPixelSize(R.dimen.dp_px_215_y))
                .mainView(rl_two)
                .startPopWindow();
    }

    @OnClick({R.id.rb_time, R.id.rb_distance, R.id.rb_calories, R.id.btn_start})
    public void onClick(View view) {
        if (view.getId() != R.id.btn_start) {
            BuzzerManager.getInstance().buzzerRingOnce();
        }
        switch (view.getId()) {
            case R.id.btn_start:
                if (isOnclickStart) {
                    return;
                }
                isOnclickStart = true;
                getPresenter().setUpRunningParam(targetType, targetValue, isMetric);
                startActivity(new Intent(GoalSelectActivity.this, GoalActivity.class));
                finish();
                break;
            case R.id.rb_time:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_TIME)
                        .editTypeName(R.string.string_common_time)
                        .involvedView(btn_selected);
                btn_selected.setBackgroundResource(R.drawable.btn_goal_time_2);
                if (rb_time.getText().toString().equals("---")) {
                    rb_time.setText("20");
                }
                btn_selected.setText(rb_time.getText());
                targetType = CTConstant.TYPE_TIME;
                targetValue = Integer.valueOf(rb_time.getText().toString());
                showSettingGoal();
                rb_distance.setText("---");
                rb_calories.setText("---");
                break;
            case R.id.rb_distance:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_DISTANCE)
                        .editTypeName(R.string.string_common_distance)
                        .involvedView(btn_selected);
                btn_selected.setBackgroundResource(isMetric ? R.drawable.btn_goal_distance_km_2 : R.drawable.btn_goal_distance_mile_2);
                if (rb_distance.getText().toString().equals("---")) {
                    rb_distance.setText("5");
                }
                btn_selected.setText(rb_distance.getText());
                targetType = CTConstant.TYPE_DISTANCE;
                targetValue = Float.valueOf(rb_distance.getText().toString());
                showSettingGoal();
                rb_time.setText("---");
                rb_calories.setText("---");
                break;
            case R.id.rb_calories:
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_CALORIES)
                        .editTypeName(R.string.string_common_calories)
                        .involvedView(btn_selected);
                btn_selected.setBackgroundResource(R.drawable.btn_goal_calories_2);
                if (rb_calories.getText().toString().equals("---")) {
                    rb_calories.setText("200");
                }
                btn_selected.setText(rb_calories.getText());
                targetType = CTConstant.TYPE_CALORIES;
                targetValue = Integer.valueOf(rb_calories.getText().toString());
                showSettingGoal();
                rb_time.setText("---");
                rb_distance.setText("---");
                break;
            default:
                break;
        }
    }
}