package com.chuhui.btcontrol.util;

import android.util.Log;

public class ConvertData {
    private static StringBuffer buffer = new StringBuffer();

    public static byte intLowToByte(int ary) {
        byte value;
        value = (byte) (ary & 0xFF);
        return value;
    }

    public static int byteToInt(byte ary) {
        return ((ary & 0xFF) | 0x00000000);
    }

    public static short bytesToShortLiterEnd(byte[] ary, int offset) {
        short value;
        value = (short) ((ary[offset] & 0xFF)
                | ((ary[offset + 1] << 8) & 0xFF00));
        return value;
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i = begin; i < begin + count; i++) bs[i - begin] = src[i];
        return bs;
    }

    public static byte[] shortToBytes(short value) {
        byte[] byte_src = new byte[2];
        byte_src[1] = (byte) ((value & 0xFF00) >> 8);
        byte_src[0] = (byte) ((value & 0x00FF));
        return byte_src;
    }


    public static String byteArrayToHexString(byte[] b, int length) {
        if(buffer.length() > 0){
            buffer.setLength(0);
        }
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

    /**
     * byte数组中取int数值，本方法适用于(高位在前，低位在后)的顺序。2个字节
     * @param ary
     * @param offset
     * @return
     */
    public static int bytesToShortBigEnd(byte[] ary, int offset) {
        int value;
        value = (short) ((ary[offset + 1] & 0x0FF)
                | ((ary[offset] << 8) & 0x0FF00));
        return value;
    }

    /**
     * 倡佑 2个字节 数据转整数，自己缩小或者放大（高位在前，低位在后）
     * @param ary
     * @param offset
     * @return
     */
    public static int cyByteToInt(byte[] ary,int offset){
        //low
        int low = ary[offset + 1] & 0xff;
        //hi
        int hi = ary[offset] & 0xff;
        return low + 100 * hi;
    }

    /**
     * 倡佑 整数转byte 数组 (2位)（高位在前，低位在后）
     * @param value 不超过4位数
     * @return
     */
    public static byte[] cyIntToByte(int value){
        byte[] data = new byte[2];
        data[1] = (byte) ((value % 100) & 0xFF);
        data[0] = (byte) ((value / 100) & 0xFF);
        return data;
    }

    /**
     * 公制转英制
     * @param value
     * @return
     */
    public static float getKmToMile1Float(float value){
        return (float) (Math.round((value / 1.6093f) * 10) / 10.0);
    }

    /**
     * 字符串转换为16进制字符串
     * @param s
     * @return
     */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**
     * 16进制表示的字符串转换为字节数组
     * @param s
     * @return
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

    public static byte[] convertToASCII(String string) {
        char[] ch = string.toCharArray();
        byte[] tmp = new byte[ch.length];
        for (int i = 0; i < ch.length; i++) {
            tmp[i] = (byte) Integer.valueOf(ch[i]).intValue();
        }
        return tmp;
    }

    /**
     * 去掉byte[]中填充的0 转为String
     * @param buffer
     * @return
     */
    public static String bytesToAsciiHasZero(byte[] buffer) {
        try {
            int length = 0;
            for (byte b:buffer) {
                if (b == 0) {
                    break;
                }
                length++;
            }
            return new String(buffer, 0, length, "UTF-8");
        } catch (Exception e) {
            Log.e("sss", e.getMessage());
            return "";
        }
    }


    public static String bytesToAscii(byte[] bytes, int offset, int dateLen) {
        if ((bytes == null) || (bytes.length == 0) || (offset < 0) || (dateLen <= 0)) {
            return null;
        }
        if ((offset >= bytes.length) || (bytes.length - offset < dateLen)) {
            return null;
        }

        String asciiStr = null;
        byte[] data = new byte[dateLen];
        System.arraycopy(bytes, offset, data, 0, dateLen);
        try {
            asciiStr = new String(data, "ISO8859-1");
        } catch (Exception e) {
        }
        return asciiStr;
    }

    public static String bytesToAscii(byte[] bytes, int dateLen) {
        return bytesToAscii(bytes, 0, dateLen);
    }

    public static String bytesToAscii(byte[] bytes) {
        return bytesToAscii(bytes, 0, bytes.length);
    }

}