package com.run.treadmill.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.run.treadmill.R;
import com.run.treadmill.activity.home.HomeActivity;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.base.factory.CreatePresenter;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;

import butterknife.BindView;
import butterknife.OnClick;

@CreatePresenter(LoginPresenter.class)
public class LoginActivity extends BaseActivity<LoginView, LoginPresenter> implements LoginView {

    @BindView(R.id.rl_login_1)
    RelativeLayout rl_login_1;

    @BindView(R.id.bt_guest)
    Button bt_guest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        GpIoUtils.setOpenScreen();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnClicking = false;
    }

    @Override
    public void hideTips() {

    }

    @Override
    public void cmdKeyValue(int keyValue) {

    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {

    }

    private void init() {
        initLogin1();
    }

    /**
     * 扫描 账号密码登录
     */
    private void initLogin1() {
        // 第1页

    }

    public boolean isOnClicking = true;

    @OnClick({R.id.bt_guest,
            R.id.bt_sign_in,
            R.id.bt_sign_up,
    })
    public void click(View view) {
        if (isOnClicking) {
            return;
        }
        if (view.getId() == R.id.bt_guest) {
            // 这个按钮不准多次点击事件
            isOnClicking = true;
        }
        switch (view.getId()) {
            case R.id.bt_guest:
                Logger.i("click");
                bt_guest.postDelayed(() -> {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                }, 200);
                break;
        }
    }


    @Override
    public void loginSuccess() {
        // 登陆成功下一页
        rl_login_1.setVisibility(View.GONE);
        // rl_login_2.setVisibility(View.VISIBLE);
    }

    @Override
    public void loginFail() {
        // 密码错误
/*        rl_login_tip.setVisibility(View.VISIBLE);
        rl_login_tip.setOnClickListener((view) -> {
            // 点击弹框消失
            rl_login_tip.setVisibility(View.GONE);
        });*/
    }
}