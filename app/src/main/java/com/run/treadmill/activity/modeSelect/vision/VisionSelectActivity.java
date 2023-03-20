package com.run.treadmill.activity.modeSelect.vision;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.run.treadmill.R;
import com.run.treadmill.activity.modeSelect.BaseSelectActivity;
import com.run.treadmill.activity.runMode.vision.VisionActivity;
import com.run.treadmill.adapter.MediaSelectPagerAdapter;
import com.run.treadmill.adapter.MovieAdapter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.UserInfoManager;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.StringUtil;
import com.run.treadmill.widget.calculator.BaseCalculator;
import com.run.treadmill.widget.calculator.CalculatorCallBack;
import com.run.treadmill.widget.calculator.CalculatorOfSelectMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/21
 */
@CreatePresenter(VisionSelectPresenter.class)
public class VisionSelectActivity extends BaseSelectActivity<VisionSelectView, VisionSelectPresenter> implements VisionSelectView, MovieAdapter.OnItemClickListener, CalculatorCallBack {

    @BindView(R.id.rl_main)
    public RelativeLayout rl_main;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.img_target_movie)
    ImageView img_target_movie;
    @BindView(R.id.rl_sd_error)
    RelativeLayout rl_sd_error;

    @BindView(R.id.rl_vision_p2)
    RelativeLayout rl_vision_p2;

    @BindView(R.id.rl_vision_p2_path2)
    RelativeLayout rl_vision_p2_path2;
    @BindView(R.id.tv_title)
    public TextView tv_title;
    @BindView(R.id.vp_movie)
    ViewPager vp_movie;
    @BindView(R.id.btn_vision_page)
    ImageView btn_vision_page;
    private Context mContext;
    private List<MovieAdapter.Movie> movies;

    /**
     * 当前影片下标
     */
    private int curInx;
    /**
     * 当前影片时长
     */
    private int duration;
    private View page1, page2;
    private RecyclerView page1RV, page2RV;
    private int vpPosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        tv_title.setText(StringUtil.valueAndIcon(this, getString(R.string.string_mode_vision), R.drawable.img_program_virtual_icon));

        movies = new ArrayList<>();

        TypedArray ta = getResources().obtainTypedArray(R.array.moviesId);
        TypedArray ta1 = getResources().obtainTypedArray(R.array.showMoviesId);
        String[] moviesNameArray = getResources().getStringArray(R.array.moviesName);
        MovieAdapter.Movie movie;
        for (int i = 0; i < ta.length(); i++) {
            movie = new MovieAdapter.Movie();
            movie.setMovieImgId(ta.getResourceId(i, 0));
            movie.setShowMovieImgId(ta1.getResourceId(i, 0));
            movie.setMovieName(moviesNameArray[i]);
            movies.add(movie);
        }
        ta.recycle();
        ta1.recycle();

        UserDB userInfo = UserInfoManager.getInstance().getUserInfo(CTConstant.VISION);
        tv_time.setText(String.valueOf(userInfo.getTime()));

        rl_vision_p2.setVisibility(View.GONE);

        LayoutInflater mLayoutInflater = getLayoutInflater();
        page1 = mLayoutInflater.inflate(R.layout.item_vision_vp, null);
        page2 = mLayoutInflater.inflate(R.layout.item_vision_vp, null);
        vp_movie.addView(page1);

        page1RV = page1.findViewById(R.id.rv_item_movie);
        GridLayoutManager glm1 = new GridLayoutManager(this, 2);
        glm1.setOrientation(GridLayoutManager.VERTICAL);
        page1RV.setLayoutManager(glm1);
        MovieAdapter adapter1 = new MovieAdapter(movies.size() < 8 ? movies.subList(0, 4) : movies.subList(0, 4));
        page1RV.setAdapter(adapter1);
        adapter1.addItemClickListener(this);

        ArrayList<View> mViews = new ArrayList<>();
        mViews.add(page1);

        // if (movies.size() == 4) {
        //     vp_movie.addView(page2);
        //     page2RV = page2.findViewById(R.id.rv_item_movie);
        //     GridLayoutManager glm2 = new GridLayoutManager(this, 2);
        //     glm2.setOrientation(GridLayoutManager.VERTICAL);
        //     page2RV.setLayoutManager(glm2);
        //     MovieAdapter adapter2 = new MovieAdapter(movies.subList(4, movies.size()));
        //     page2RV.setAdapter(adapter2);
        //     adapter2.addItemClickListener(this);
        //     mViews.add(page2);
        //     btn_vision_page.setVisibility(View.VISIBLE);
        // }

        vp_movie.setAdapter(new MediaSelectPagerAdapter(mViews));
        vp_movie.setOffscreenPageLimit(1);//页数
        vp_movie.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                vpPosition = position;
                // if (position == 0) {
                //     btn_vision_page.setImageResource(R.drawable.img_home_media_page_1);
                // } else {
                //     btn_vision_page.setImageResource(R.drawable.img_home_media_page_2);
                // }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected boolean isCanStart() {
        return rl_vision_p2.getVisibility() == View.VISIBLE && rl_vision_p2_path2.getVisibility() == View.VISIBLE;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_vision;
    }

    @OnClick({R.id.btn_start, R.id.btn_back, R.id.rl_sd_error, R.id.tv_time})
    public void click(View view) {
        BuzzerManager.getInstance().buzzerRingOnce();
        switch (view.getId()) {
            case R.id.tv_time:
                if (mCalcBuilder == null) {
                    mCalcBuilder = new BaseCalculator.Builder(new CalculatorOfSelectMode(this));
                }
                rl_vision_p2_path2.setVisibility(View.GONE);
                mCalcBuilder.reset()
                        .editType(CTConstant.TYPE_TIME)
                        .editTypeName(R.string.string_select_time)
                        .callBack(this)
                        .mainView(rl_main)
                        .setXAndY(getResources().getDimensionPixelSize(R.dimen.dp_px_1020_x), getResources().getDimensionPixelSize(R.dimen.dp_px_210_y))
                        .startPopWindow();
                break;
            case R.id.rl_sd_error:
                rl_sd_error.setVisibility(View.GONE);
                break;
            case R.id.btn_back:
                if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
                    mCalcBuilder.stopPopWin();
                }
                vp_movie.setVisibility(View.VISIBLE);
                // btn_vision_page.setVisibility(View.VISIBLE);

                rl_vision_p2.setVisibility(View.GONE);
                rl_vision_p2_path2.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_start:
                if (isOnclickStart) {
                    return;
                }
                isOnclickStart = true;
                getPresenter().setRunParam(Integer.valueOf(tv_time.getText().toString()));

                Intent intent = new Intent(this, VisionActivity.class);
                intent.putExtra(CTConstant.VR_PATH_INX, curInx);
                intent.putExtra(CTConstant.VR_PATH_DURATION, duration);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        BuzzerManager.getInstance().buzzerRingOnce();
        curInx = vpPosition == 1 ? position + 4 : position;

        img_target_movie.setImageResource(movies.get(curInx).getShowMovieImgId());
        String curPath = FileUtil.getStoragePath(mContext, "SD") + CTConstant.vrVideoPath[curInx];
        if (curPath.isEmpty() || !FileUtil.isCheckExist(curPath)) {
            rl_sd_error.setVisibility(View.VISIBLE);
            Logger.d("pathVideo", curPath + "");
            return;
        }
        duration = getPresenter().getMovieDuration(mContext, curPath);
        if (duration <= 0) {
            rl_sd_error.setVisibility(View.VISIBLE);
            Logger.d("pathVideo", duration + "");
            return;
        }
        vp_movie.setVisibility(View.GONE);
        // btn_vision_page.setVisibility(View.GONE);
        rl_vision_p2.setVisibility(View.VISIBLE);
    }

    @Override
    public void enterCallBack(int type, String value) {
        if (type == CTConstant.TYPE_TIME) {
            tv_time.setText(value);
        }
        rl_vision_p2_path2.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCalculatorDismiss() {
        rl_vision_p2_path2.setVisibility(View.VISIBLE);

    }

    @Override
    protected void clickBack() {
        if (rl_vision_p2.getVisibility() == View.VISIBLE) {
            // 处于详情页时
            if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
                mCalcBuilder.stopPopWin();
            }
            vp_movie.setVisibility(View.VISIBLE);
            rl_vision_p2.setVisibility(View.GONE);
            rl_vision_p2_path2.setVisibility(View.VISIBLE);
        } else {
            goHome(null);
        }
    }
}