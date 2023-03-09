package com.chuhui.btcontrol;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/03/26
 */
public interface BtCallBack {

    void onRequestConnect(@BtHelper.BTType int btType);

    void onLastConnect(@BtHelper.BTType int btType);

    void onDataCallback(CbData data);
}