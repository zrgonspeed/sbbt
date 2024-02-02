package com.run.treadmill.activity.login;

import com.run.treadmill.base.BaseView;

public interface LoginView extends BaseView {
    void loginSuccess();

    void loginFail();
}