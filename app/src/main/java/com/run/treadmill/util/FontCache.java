package com.run.treadmill.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * @Description 字体缓存类，减少对assets文件夹的访问数
 * @Author GaleLiu
 * @Time 2019/01/18
 */
public class FontCache {

    private static HashMap<String, Typeface> fontCache = new HashMap<>();

    public static Typeface getTypeface(Context context, String fontName) {
        Typeface typeface = fontCache.get(fontName);
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), fontName);
                fontCache.put(fontName, typeface);
            } catch (Exception e) {
                throw new RuntimeException("字体不存在！>>>【" + fontName + "】");
            }
        }
        return typeface;
    }
}