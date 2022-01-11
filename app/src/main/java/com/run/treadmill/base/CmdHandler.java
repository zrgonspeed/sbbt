package com.run.treadmill.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/03
 */
public class CmdHandler extends Handler {
    private BasePresenter mPresenter;

    CmdHandler(Looper looper, BasePresenter presenter) {
        super(looper);
        this.mPresenter = presenter;
    }

    @Override
    public void handleMessage(Message msg) {
        if (mPresenter == null) {
            return;
        }
        if (mPresenter.getView() == null) {
            return;
        }
        mPresenter.handleCmdMsg(msg);
    }
}