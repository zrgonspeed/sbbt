package com.fitShow;

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
        if (buffer.length() > 0) {
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
}
