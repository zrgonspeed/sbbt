package com.run.treadmill.activity.summary;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.activity.CustomTimer;
import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.adapter.SummaryPagerAdapter;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.TimeStringUtil;
import com.run.treadmill.util.UnitUtil;
import com.run.treadmill.widget.LineGraphicView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/20
 */
@CreatePresenter(SummaryPresenter.class)
public class SummaryActivity extends BaseActivity<SummaryView, SummaryPresenter> implements SummaryView, CustomTimer.TimerCallBack {

    @BindView(R.id.vp_summary)
    public ViewPager vp_summary;
    @BindView(R.id.img_indicator)
    public ImageView img_indicator;

    @BindView(R.id.btn_logo)
    ImageView btn_logo;

    private TextView tv_time;
    private TextView tv_distance;
    private TextView tv_calories;
    private TextView tv_unit_time, tv_unit_dis, tv_unit_speed;
    private LinearLayout ly_score;
    private TextView tv_grade, tv_vo2;
    private TextView tv_avg_speed;
    private TextView tv_avg_incline;
    private TextView tv_avg_mets;
    private TextView tv_avg_pulse;

    private TextView tv_max_speed, tv_min_speed, tv_max_incline, tv_min_incline, tv_max_pulse, tv_min_pulse, tv_unit_max_speed, tv_unit_min_speed;

    private LineGraphicView lgv_speed, lgv_incline, lgv_pulse;

    private List<View> views;

    private int[] indicator = {
            R.drawable.img_sportmode_summary_page_1, R.drawable.img_sportmode_summary_page_2,
            R.drawable.img_sportmode_summary_page_3, R.drawable.img_sportmode_summary_page_4,
    };
    private CustomTimer waiteTimer;
    private String waiteTimerTag = "WaiteTimer";
    private final long waiteTime = 3 * 60 * 1000;

    private RunningParam mRunningParam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRunningParam = RunningParam.getInstance();

        views = new ArrayList<>();
        views.add(View.inflate(this, R.layout.view_page_summary_result, null));
        views.add(View.inflate(this, R.layout.view_page_summary_speed, null));
        views.add(View.inflate(this, R.layout.view_page_summary_incline, null));
        views.add(View.inflate(this, R.layout.view_page_summary_pulse, null));
        vp_summary.setAdapter(new SummaryPagerAdapter(views));
        vp_summary.setOffscreenPageLimit(4);
        vp_summary.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                img_indicator.setImageResource(indicator[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean needVo2 = getIntent().getBooleanExtra(CTConstant.NEED_VO2, false);
        if (needVo2) {
            getPresenter().countVo2();
        }

    }

    public void goHome(View view) {
        BuzzerManager.getInstance().buzzerRingOnce();
        if (waiteTimer != null) {
            waiteTimer.closeTimer();
            waiteTimer = null;
        }
        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_summary;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waiteTimer != null) {
            waiteTimer.closeTimer();
            waiteTimer = null;
        }
    }

    @Override
    public void hideTips() {

    }

    @Override
    public void cmdKeyValue(int keyValue) {
        switch (keyValue) {
            case SerialKeyValue.HOME_KEY_CLICK:
            case SerialKeyValue.STOP_CLICK:
                BuzzerManager.getInstance().buzzerRingOnce();
                if (waiteTimer != null) {
                    waiteTimer.closeTimer();
                    waiteTimer = null;
                }
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            waiteTimer.closeTimer();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            waiteTimer.startTimer(waiteTime, this);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {

    }

    @Override
    public void timerComply(long lastTime, String tag) {
        if (tag.equals(waiteTimerTag)) {
            finish();
        }
    }

    @Override
    public void showVo2(int grade, float score) {
        ly_score.setVisibility(View.VISIBLE);
        tv_grade.setText(grade);
        tv_vo2.setText(String.valueOf(score));
    }

    private void init() {
        FileUtil.setLogoIcon(this, btn_logo);

        tv_time = (TextView) views.get(0).findViewById(R.id.tv_time);
        tv_unit_time = (TextView) views.get(0).findViewById(R.id.tv_unit_time);
        tv_distance = (TextView) views.get(0).findViewById(R.id.tv_distance);
        tv_unit_dis = (TextView) views.get(0).findViewById(R.id.tv_unit_dis);
        tv_calories = (TextView) views.get(0).findViewById(R.id.tv_calories);
        ly_score = (LinearLayout) views.get(0).findViewById(R.id.ly_score);
        tv_grade = (TextView) views.get(0).findViewById(R.id.tv_grade);
        tv_vo2 = (TextView) views.get(0).findViewById(R.id.tv_vo2);
        tv_avg_speed = (TextView) views.get(0).findViewById(R.id.tv_avg_speed);
        tv_unit_speed = (TextView) views.get(0).findViewById(R.id.tv_unit_speed);
        tv_avg_incline = (TextView) views.get(0).findViewById(R.id.tv_avg_incline);
        tv_avg_mets = (TextView) views.get(0).findViewById(R.id.tv_avg_mets);
        tv_avg_pulse = (TextView) views.get(0).findViewById(R.id.tv_avg_pulse);

        tv_time.setText(TimeStringUtil.getsecToHrMinOrMinSec(mRunningParam.alreadyRunTime, "%02d:%02d", "%02d:%02d"));
        tv_distance.setText(String.valueOf(UnitUtil.getFloatBy1f(mRunningParam.alreadyRunDistance)));
        tv_calories.setText(String.valueOf((int) mRunningParam.alreadyRunCalories));
        tv_avg_speed.setText(String.valueOf(mRunningParam.getAvgSpeed()));
        tv_avg_incline.setText(String.valueOf((int) mRunningParam.getAvgIncline()));
        tv_avg_mets.setText(String.valueOf(mRunningParam.getAvgMets()));
        tv_avg_pulse.setText(String.valueOf((int) mRunningParam.getAvgPulse()));

        tv_unit_time.setText(getString(mRunningParam.alreadyRunTime > (60 * 60) ? R.string.string_unit_min : R.string.string_unit_sec));
        tv_unit_dis.setText(getString(isMetric ? R.string.string_unit_km : R.string.string_unit_mile));
        tv_unit_speed.setText(getString(isMetric ? R.string.string_unit_kmh : R.string.string_unit_mileh));

        tv_max_speed = (TextView) views.get(1).findViewById(R.id.tv_max_speed);
        tv_min_speed = (TextView) views.get(1).findViewById(R.id.tv_min_speed);
        tv_unit_max_speed = (TextView) views.get(1).findViewById(R.id.tv_unit_max_speed);
        tv_unit_min_speed = (TextView) views.get(1).findViewById(R.id.tv_unit_min_speed);
        lgv_speed = (LineGraphicView) views.get(1).findViewById(R.id.lgv_speed);
        tv_max_speed.setText(String.valueOf(mRunningParam.getMaxSpeed()));
        tv_min_speed.setText(String.valueOf(mRunningParam.getMinSpeed()));
        tv_unit_max_speed.setText(getString(isMetric ? R.string.string_unit_kmh : R.string.string_unit_mileh));
        tv_unit_min_speed.setText(getString(isMetric ? R.string.string_unit_kmh : R.string.string_unit_mileh));
        lgv_speed.setColor(getColor(R.color.lightBlue));
        lgv_speed.setData(getReplacePointTap(mRunningParam.getSpeedList(), mRunningParam.getMaxSpeedInx(), mRunningParam.getMinSpeedInx()),
                getPointTap(mRunningParam.getTimeList()),
                30, 2, mRunningParam.alreadyRunTime);

        tv_max_incline = (TextView) views.get(2).findViewById(R.id.tv_max_incline);
        tv_min_incline = (TextView) views.get(2).findViewById(R.id.tv_min_incline);
        lgv_incline = (LineGraphicView) views.get(2).findViewById(R.id.lgv_incline);
        tv_max_incline.setText(String.valueOf((int) mRunningParam.getMaxIncline()));
        tv_min_incline.setText(String.valueOf((int) mRunningParam.getMinIncline()));
        lgv_incline.setColor(getColor(R.color.deepGreen));
        lgv_incline.setData(getReplacePointTap(mRunningParam.getInclineList(), mRunningParam.getMaxInclineInx(), mRunningParam.getMinInclineInx()),
                getPointTap(mRunningParam.getTimeList()),
                20, 2, mRunningParam.alreadyRunTime);

        tv_max_pulse = (TextView) views.get(3).findViewById(R.id.tv_max_pulse);
        tv_min_pulse = (TextView) views.get(3).findViewById(R.id.tv_min_pulse);
        lgv_pulse = (LineGraphicView) views.get(3).findViewById(R.id.lgv_pulse);
        tv_max_pulse.setText(String.valueOf(mRunningParam.getMaxPulse()));
        tv_min_pulse.setText(String.valueOf(mRunningParam.getMinPulse()));
        lgv_pulse.setColor(getColor(R.color.pink));
        lgv_pulse.setData(getReplacePointTap(mRunningParam.getPulseList(), mRunningParam.getMaxPulseInx(), mRunningParam.getMinPulseInx()),
                getPointTap(mRunningParam.getTimeList()),
                250, 2, mRunningParam.alreadyRunTime);

        waiteTimer = new CustomTimer();
        waiteTimer.setTag(waiteTimerTag);
        waiteTimer.startTimer(waiteTime, this);
        vp_summary.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    waiteTimer.closeTimer();
                } else if (state == ViewPager.SCROLL_STATE_SETTLING) {//手指离开屏幕
                    waiteTimer.startTimer(waiteTime, SummaryActivity.this);
                }
            }
        });

    }

    /**
     * 取120个数
     * 用于替换数组中最大最小值，保证最大最小值能存在数组中（只能替换一个，如果多个最大最小则该做法不满足）
     *
     * @param param
     * @param maxValueInx
     * @param minValueInx
     * @return
     */
    private ArrayList<Double> getReplacePointTap(ArrayList<Double> param, int maxValueInx, int minValueInx) {
        if (param.size() < 120) {
            return param;
        }
        ArrayList<Double> nNewParam = new ArrayList<>();
        int maxInx = -1;
        int minInx = -1;
        if (maxValueInx != -1) {
            maxInx = maxValueInx * 120 / param.size();
        }
        if (minValueInx != -1) {
            minInx = minValueInx * 120 / param.size();
        }
        //最大最小刚好是同一个下标会被覆盖
        if (maxInx != -1 && minInx != -1) {
            if (maxInx == minInx) {
                if (maxValueInx > minValueInx) {
                    if (maxInx >= 120) {
                        minInx -= 1;
                    } else {
                        maxInx += 1;
                    }
                } else if (maxValueInx < minValueInx) {
                    if (maxInx >= 120) {
                        maxInx -= 1;
                    } else {
                        minInx += 1;
                    }
                }
            }
        }

        for (int i = 0; i < 120; i++) {
            if (maxInx != -1 && i == maxInx) {
                nNewParam.add(param.get(maxValueInx));
            } else if (minInx != -1 && i == minInx) {
                nNewParam.add(param.get(minValueInx));
            } else {
                nNewParam.add(param.get((int) Math.round(param.size() / 120.0 * i)));
            }
        }
        return nNewParam;
    }

    //取120个数
    private ArrayList<Double> getPointTap(ArrayList<Double> param) {
        if (param.size() < 120) {
            return param;
        }
        ArrayList<Double> nNewParam = new ArrayList<>();

        for (int i = 0; i < 120; i++) {
            nNewParam.add(param.get((int) Math.round(param.size() / 120.0 * i)));
        }
        return nNewParam;
    }
}