package com.fitShow.treadmill;

/**
 * waHoo数据组装与校验
 */
public final class FsTreadmillDataHandler {

    /**
     * 获取组装后的数据
     *
     * @param pSrc 源数据(不含校验位)
     * @param len  源数据长度
     * @return 组装好的数据
     */
    protected static byte[] getBuildUpData(byte[] pSrc, int len) {
        //这个方法里面额外补充计算校验位
        pSrc[len - 2] = calc(pSrc, len);
        return pSrc;
    }

    /**
     * 异或校验
     *
     * @param data
     * @param len
     * @return
     */
    protected static byte calc(byte[] data, int len) {
        byte result = 0x00;
        for (int i = 1; i < len - 2; i++) {
            result ^= data[i];
        }
        return result;
    }
}
