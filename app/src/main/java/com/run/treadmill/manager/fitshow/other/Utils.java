package com.run.treadmill.manager.fitshow.other;

public class Utils {
    /**
     * 异或校验
     *
     * @param data
     * @param len
     * @return
     */
    public static byte calc(byte[] data, int len) {
        byte result = 0x00;
        for (int i = 1; i < len - 2; i++) {
            result ^= data[i];
        }
        return result;
    }
}
