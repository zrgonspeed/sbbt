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
        GpIoUtils.setScreen_1();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @OnClick({R.id.bt_guest})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.bt_guest:
                Logger.i("click");
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
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