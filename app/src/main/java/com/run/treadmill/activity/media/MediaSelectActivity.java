package com.run.treadmill.activity.media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.run.treadmill.R;
import com.run.treadmill.activity.SafeKeyTimer;
import com.run.treadmill.activity.runMode.quickStart.QuickStartActivity;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.AppDebug;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.base.factory.CreatePresenter;
import com.run.treadmill.manager.BuzzerManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.thirdapp.main.HomeAndRunAppUtils;
import com.run.treadmill.util.FileUtil;

import java.util.ArrayList;

import butterknife.BindView;

@CreatePresenter(MediaSelectPresenter.class)
public class MediaSelectActivity extends BaseActivity<MediaSelectView, MediaSelectPresenter> implements MediaSelectView, View.OnClickListener {


    @BindView(R.id.btn_back)
    ImageView btn_back;

    @BindView(R.id.btn_logo)
    ImageView btn_logo;
    @BindView(R.id.vp_select_media)
    ViewPager vp_movie;
    @BindView(R.id.btn_vision_page)
    ImageView btn_vision_page;

    private String mediaPkName = null;

    private boolean isOpenGSMode = false;
    private int curMinAD = 0;

    private boolean isCanStart;
    private View page1, page2;
    private RecyclerView page1RV, page2RV;
    private int vpPosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_media;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSpManager();
    }

    @Override
    public void hideTips() {

    }

    @Override
    public void cmdKeyValue(int keyValue) {
        switch (keyValue) {
            case SerialKeyValue.BACK_KEY_CLICK:
                btn_back.performClick();
                break;
            case SerialKeyValue.HOME_KEY_CLICK:
                BuzzerManager.getInstance().buzzerRingOnce();
                btn_back.performClick();
                break;
        }
    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {
        if (!SafeKeyTimer.getInstance().getIsSafe()) {
            if (isCanStart) {
                isCanStart = false;
            }
            return;
        }
        if (beltStatus != 0) {
            if (isCanStart) {
                isCanStart = false;
            }
            return;
        }

        //有扬升错误，不管扬升状态
        if (ErrorManager.getInstance().isHasInclineError()) {
            if (!isCanStart) {
                isCanStart = true;
            }
            return;
        }

        //扬升状态为0，扬升ad在最小ad的+/- 15内
        if (inclineStatus == 0) {
            //   if (checkADValueIsInSafe(curInclineAd)) {
            if (!isCanStart) {
                isCanStart = true;
            }
            return;
            //    }
        }

        if (isCanStart) {
            isCanStart = false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            BuzzerManager.getInstance().buzzerRingOnce();
            finish();
        }
    }

    private void init() {
        Context context = MediaSelectActivity.this;
        final String[] pkgName = HomeAndRunAppUtils.getPkgNames();
        int[] drawable = HomeAndRunAppUtils.getHomeDrawables();
        // Logger.i("pkgName == " + Arrays.toString(pkgName));
        btn_back.setOnClickListener(this);

        FileUtil.setLogoIcon(this, btn_logo);

        LayoutInflater mLayoutInflater = getLayoutInflater();
        page1 = mLayoutInflater.inflate(R.layout.item_media_vp, null);
        page2 = mLayoutInflater.inflate(R.layout.item_media_vp, null);
        vp_movie.addView(page1);

        page1RV = (RecyclerView) page1.findViewById(R.id.rv_item_movie);
        GridLayoutManager glm1 = new GridLayoutManager(this, 7);
        glm1.setOrientation(GridLayoutManager.VERTICAL);
        page1RV.setLayoutManager(glm1);
        int[] drawable1 = new int[drawable.length];
        System.arraycopy(drawable, 0, drawable1, 0, drawable1.length);
        MediaSelectAppAdapter adapter1 = new MediaSelectAppAdapter(context, drawable1);
        page1RV.setAdapter(adapter1);
        adapter1.setOnItemClick(position -> {
            if (!isCanStart && !AppDebug.disableSerial) {
                return;
            }
            mediaPkName = pkgName[position];
            enterThirdApp();
        });

        ArrayList<View> mViews = new ArrayList<>();
        mViews.add(page1);

        vp_movie.addView(page2);
        page2RV = (RecyclerView) page2.findViewById(R.id.rv_item_movie);
        GridLayoutManager glm2 = new GridLayoutManager(this, 7);
        glm2.setOrientation(GridLayoutManager.VERTICAL);
        page2RV.setLayoutManager(glm2);
        int[] drawable2 = new int[drawable.length - drawable1.length];
        System.arraycopy(drawable, drawable1.length, drawable2, 0, drawable2.length);
        MediaSelectAppAdapter adapter2 = new MediaSelectAppAdapter(context, drawable2);
        page2RV.setAdapter(adapter2);
        adapter2.setOnItemClick(position -> {
            if (!isCanStart && !AppDebug.disableSerial) {
                return;
            }
            mediaPkName = pkgName[drawable1.length + position];
            enterThirdApp();
        });

        vp_movie.setAdapter(new MediaSelectPagerAdapter(mViews));
        vp_movie.setOffscreenPageLimit(1);
        vp_movie.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                vpPosition = position;
                if (position == 0) {
                    btn_vision_page.setImageResource(R.drawable.img_home_media_page_1);
                } else {
                    btn_vision_page.setImageResource(R.drawable.img_home_media_page_2);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void loadSpManager() {
        isOpenGSMode = SpManager.getGSMode();
        curMinAD = SpManager.getMinAd();
    }

    private void enterThirdApp() {
        BuzzerManager.getInstance().buzzerRingOnce();
        getPresenter().setUpRunningParam(isMetric);
        //进入QuickStart模式再进入第三方媒体
        Intent intent = new Intent(MediaSelectActivity.this, QuickStartActivity.class);
        intent.putExtra(CTConstant.IS_MEDIA, true);
        intent.putExtra(CTConstant.PK_NAME, mediaPkName);
        startActivity(intent);
        finish();
    }
}