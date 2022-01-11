package com.fitShow.treadmill;

/**
 * 公英制单位划算,浮点型的取舍和自动进1
 */
public class UnitUtil {

    private UnitUtil() {

    }

    /**
     * 取整数,四舍五入
     *
     * @param value
     * @return
     */
    public static int getFloatToInt(float value) {
        return (Math.round(value));
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序。
     *
     * @param ary    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToIntLitter(byte[] ary, int offset) {
        return (int) ((ary[offset] & 0xFF)
                | ((ary[offset + 1] << 8) & 0xFF00)
                | ((ary[offset + 2] << 16) & 0xFF0000)
                | ((ary[offset + 3] << 24) & 0xFF000000));
    }


    public static String byteArrayToHexString(byte[] b, int offSet, int endSet) {
        StringBuffer buffer = new StringBuffer();
        for (int i = offSet; i < endSet; ++i) {
            buffer.append(toHexString(b[i]) + " ");
        }
        return buffer.toString();
    }

    public static String byteArrayToHexString(byte[] b, int length) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; ++i) {
            buffer.append("0x" + toHexString(b[i]) + " ");
        }
        return buffer.toString();
    }

    public static String toHexString(byte b) {
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            return "0" + s;
        } else {
            return s;
        }
    }

    //byte数组转换十六进制
    public static String byteArrayToHexStrNo0x(byte[] b, int offset, int length) {
        StringBuffer buffer = new StringBuffer();
        for (int i = length + offset - 1; i >= offset; i--) {
            buffer.append(toHexString(b[i]) + "");
        }
        return buffer.toString();
    }
}
