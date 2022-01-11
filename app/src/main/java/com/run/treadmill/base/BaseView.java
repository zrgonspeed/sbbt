package com.run.treadmill.base;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/05/29
 */
public interface BaseView {

    /**
     * 其他错误(包括扬升错误)
     *
     * @param errCode
     */
    void showError(int errCode);

    /**
     * 安全key出错
     */
    void safeError();

    /**
     * 连接超时(专门针对A33或T3与转接板的,不是转接板与下控)
     */
    void commOutError();

    /**
     * 隐藏提示
     */
    void hideTips();

    /**
     * 按键值返回
     *
     * @param keyValue 按键值
     */
    void cmdKeyValue(int keyValue);

    /**
     * 跑带和扬升状态
     *
     * @param beltStatus    跑带状态
     * @param inclineStatus 扬升状态
     * @param curInclineAd  当前扬升ad
     */
    void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd);
}