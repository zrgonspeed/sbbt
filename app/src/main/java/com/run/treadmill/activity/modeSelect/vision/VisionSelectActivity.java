package com.run.treadmill.activity.modeSelect.vision;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.treadmill.R;
import com.run.treadmill.activity.modeSelect.BaseSelectActivity;
import com.run.treadmill.activity.runMode.vision.VisionActivity;
import com.run.treadmill.adapter.MovieAdapter;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.db.UserDB;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.UserInfoManager;
import com.run.treadmill.util.FileUtil;
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
    @BindView(R.id.rv_movie)
    RecyclerView rv_movie;
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

    private List<MovieAdapter.Movie> movies;

    /**
     * 当前影片下标
     */
    private int curInx;
    /**
     * 当前影片时长
     */
    private int duration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tv_title.setText(StringUtil.valueAndIcon(this, getString(R.string.string_mode_vision), R.drawable.img_program_virtual_icon));

        GridLayoutManager glm = new GridLayoutManager(this, 2);
        glm.setOrientation(GridLayoutManager.VERTICAL);
        rv_movie.setLayoutManager(glm);
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
        MovieAdapter adapter = new MovieAdapter(movies);
        rv_movie.setAdapter(adapter);
        adapter.addItemClickListener(this);

        UserDB userInfo = UserInfoManager.getInstance().getUserInfo(CTConstant.VISION);
        tv_time.setText(String.valueOf(userInfo.getTime()));

        rl_vision_p2.setVisibility(View.GONE);
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
                        .setXAndY(getResources().getDimensionPixelSize(R.dimen.dp_px_666_x), getResources().getDimensionPixelSize(R.dimen.dp_px_111_y))
                        .startPopWindow();
                break;
            case R.id.rl_sd_error:
                rl_sd_error.setVisibility(View.GONE);
                break;
            case R.id.btn_back:
                if (mCalcBuilder != null && mCalcBuilder.isPopShowing()) {
                    mCalcBuilder.stopPopWin();
                }
                rv_movie.setVisibility(View.VISIBLE);

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
        img_target_movie.setImageResource(movies.get(position).getShowMovieImgId());
        String curPath = CTConstant.vrVideoPath[position];
        if (curPath.isEmpty() || !FileUtil.isCheckExist(curPath)) {
            rl_sd_error.setVisibility(View.VISIBLE);
            return;
        }
        duration = getPresenter().getMovieDuration(curPath);
        if (duration <= 0) {
            rl_sd_error.setVisibility(View.VISIBLE);
            return;
        }
        curInx = position;
        rv_movie.setVisibility(View.GONE);
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
}