package com.run.treadmill.interceptor;

import android.os.Message;

/**
 * @Description 这是处理serial数据的责任链
 * @Author GaleLiu
 * @Time 2019/09/12
 */
public interface SerialInterceptor {

    /**
     * 负责处理实际的处理
     *
     * @param chain
     * @return 返回一个msg
     */
    Message intercept(Chain chain);

    interface Chain {

        /**
         * 负责构建责任链
         *
         * @param data
         * @param inOnSleep 是否休眠
         * @return 返回一个msg
         */
        Message procced(byte[] data, boolean inOnSleep);
    }
}