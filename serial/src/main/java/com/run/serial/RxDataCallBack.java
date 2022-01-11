package com.run.serial;

public interface RxDataCallBack {
    /**
     * <br>当下发的数据返回为成功时触发</br>
     * <br>当接收到的返回为重发类型时,会自动执行SerialTxData.getInstance().reMoveQueuePackage();</br>
     *
     * @param data
     * @param len
     */
    void onSucceed(byte[] data, int len);

    /**
     * <br>当下发的数据返回为非成功时触发</br>
     * <br>需要自行决定在从发多少次后移除重发,最大次数不超过40次</br>
     *
     * @param count 当前命令失败的次数
     */
    void onFail(byte[] data, int len, int count);

    /**
     * 串口通信超时
     */
    void onTimeOut();
}
