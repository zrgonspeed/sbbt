package com.run.treadmill.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ImageSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/20
 */
public class StringUtil {

    /**
     * 去掉尾部的单位，提取数字
     *
     * @param value
     * @return
     */
    public static String removeUnit(String value) {
        String temp = value;
        //小数
        String regExPoint = "(\\d+\\.\\d+)";
        //整数
        String regExInt = "(\\d+)";
        Pattern p = Pattern.compile(regExPoint);
        Matcher m = p.matcher(value);
        //m.find用来判断该字符串中是否含有与"(\\d+\\.\\d+)"相匹配的子串
        if (m.find()) {
            //如果有相匹配的,则判断是否为null操作
            //group()中的参数：0表示匹配整个正则，1表示匹配第一个括号的正则,2表示匹配第二个正则,在这只有一个括号,即1和0是一样的
            value = m.group(1) == null ? "" : m.group(1);
        } else {
            //如果匹配不到小数，就进行整数匹配
            p = Pattern.compile(regExInt);
            m = p.matcher(value);
            if (m.find()) {
                //如果有整数相匹配
                value = m.group(1) == null ? "" : m.group(1);
            } else {
                //如果没有小数和整数相匹配,即字符串中没有整数和小数，就设为空
                value = "";
            }
        }
        if (temp.contains("-")) {
            return "-" + value;
        }
        return value;
    }

    /**
     * 获取设置textview不同大小的SpannableString
     *
     * @param str
     * @param size
     * @param start
     * @param end
     * @param flags
     * @return
     */
    public static SpannableString getBigAndSmall(String str, int size, int start, int end, int flags) {
        SpannableString sp = new SpannableString(str);
        sp.setSpan(new AbsoluteSizeSpan(size, true), start, end, flags);
        return sp;
    }

    /**
     * 设置 数值+单位 格式的字串
     *
     * @param value 数值
     * @param unit  单位
     * @param size  大小
     * @return
     */
    public static SpannableString valueAndUnit(String value, String unit, int size) {
        String str = value + " " + unit;
        return getBigAndSmall(str, size, str.length() - unit.length(), str.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }

    /**
     * 设置  字串 + 图片 格式
     *
     * @param context
     * @param str
     * @param resId
     * @return
     */
    public static SpannableString valueAndIcon(Context context, String str, int resId) {
        //波斯语
        boolean isLeft = context.getResources().getConfiguration().locale.getLanguage().endsWith("ir");
        String res;
        if (isLeft) {
            res = "   " + str;
        } else {
            res = str + "   ";
        }
        SpannableString sp = new SpannableString(res);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        Bitmap newBitmap = alterSizeBitmap(bitmap, 100, 65);

        if (newBitmap != null) {
            ImageSpan imageSpan = new ImageSpan(context, newBitmap);
            if (!isLeft) {
                sp.setSpan(imageSpan, res.length() - 1, res.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            } else {
                sp.setSpan(imageSpan, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return sp;
    }

    /**
     * 设置bitmap宽高，返回新的bitmap
     *
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap alterSizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        //计算压缩的比率
        float scaleWidth = ((float) newWidth) / bitmap.getWidth();
        float scaleHeight = ((float) newHeight) / bitmap.getHeight();
        //获取想要缩放的matrix
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        //获取新的bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}