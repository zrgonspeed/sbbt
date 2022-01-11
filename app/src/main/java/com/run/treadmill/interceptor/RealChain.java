package com.run.treadmill.interceptor;

import android.os.Message;

import com.run.treadmill.util.DataTypeConversion;

import java.util.List;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/09/12
 */
public class RealChain implements SerialInterceptor.Chain {
    /**
     * 需要处理的拦截器集合
     */
    private List<SerialInterceptor> mInterceptors;
    /**
     * 需要处理的拦截器下标
     */
    private int inx;
    /**
     * 数据
     */
    private byte[] mData;
    /**
     * 是否休眠状态
     */
    private boolean inOnSleep;

    public RealChain(List<SerialInterceptor> interceptors) {
        this.mInterceptors = interceptors;
    }

    public RealChain(List<SerialInterceptor> interceptors, int index, byte[] data, boolean inOnSleep) {
        this.mInterceptors = interceptors;
        this.inx = index;
        this.mData = data;
        this.inOnSleep = inOnSleep;
    }

    public void changeData(int index, byte[] data, boolean inOnSleep) {
        this.inx = index;
        this.mData = data;
        this.inOnSleep = inOnSleep;
    }

    @Override
    public Message procced(byte[] data, boolean inOnSleep) {
        if (mInterceptors.size() < inx) {
            return null;
        }
        //生成下一个责任链
        RealChain nextChain = new RealChain(mInterceptors, inx + 1, mData, inOnSleep);
        //执行当前任务并传入下一个责任链
        return mInterceptors.get(inx).intercept(nextChain);
    }

    byte[] getmData() {
        return mData;
    }

    boolean isInOnSleep() {
        return inOnSleep;
    }

    /**
     * 获取date 数据中第offSet 长度为len  的结果
     *
     * @param date
     * @param offSet
     * @param len
     * @return
     */
    int resolveDate(byte[] date, int offSet, int len) {
        int result;
        if (len == 3) {
            //3个字节 ,暂时不知道如何处理
            result = 0;
        } else if (len == 2) {
            result = DataTypeConversion.bytesToShortLiterEnd(date, offSet);
        } else if (len == 1) {
            result = DataTypeConversion.byteToInt(date[offSet]);
        } else {
            result = 0;
        }
        return result;
    }
}