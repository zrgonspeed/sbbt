package com.run.treadmill.util;

import com.run.treadmill.common.InitParam;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 公英制单位划算,浮点型的取舍和自动进1
 */
public class UnitUtil {
    /**
     * 速度,距离的单位转换倍率1mil=1.609km
     */
    public static final float k1 = 1.6093f;

    /**
     * 重量单位转换: 1Kg=2.2046lb
     */
    private static final float k2 = 2.2046f;

    /**
     * 长度单位转换: 1cm=0.0328ft
     */
    private static final float k3 = 0.0328f;

    /**
     * 长度单位转换: 1cm=0.3937in
     */
    private static final float k4 = 0.3937f;

    /**
     * 长度单位转换: 1km=3280.8398ft
     */
    private static final float k5 = 3280.8398f;

    /**
     * 长度单位转换: 1mile=5280ft
     */
    private static final float k6 = 5280f;

    /**
     * 长度单位转换: 1m=3.2808ft
     */
    private static final float k7 = 3.2808f;

    /**
     * 长度单位转换: 1ft=12in
     */
    private static final float k8 = 12.000f;

    private UnitUtil() {

    }

    public static float getKmToMile(float km) {
        return (float) (Math.round((km / k1) * 1000) / 1000.0);
    }

    public static float getMileToKm(float mile) {
        return (float) (Math.round((mile * k1) * 1000) / 1000.0);
    }

    /**
     * 对边界值进行强制转换为对应的边界值
     *
     * @param kg
     * @return
     */
    public static float getKgToLb1(float kg) {
        if (kg == InitParam.DEFAULT_WEIGHT_METRIC) {
            return InitParam.DEFAULT_WEIGHT_IMPERIAL;
        }
        if (kg == InitParam.MIN_WEIGHT_METRIC) {
            return InitParam.MIN_WEIGHT_IMPERIAL;
        }
        if (kg == InitParam.MAX_WEIGHT_METRIC) {
            return InitParam.MAX_WEIGHT_IMPERIAL;
        }
        return (float) (Math.round((kg * k2) * 1000) / 1000.0);
    }

    public static float getLbToKg1(float lb) {
        if (lb == InitParam.DEFAULT_WEIGHT_IMPERIAL) {
            return InitParam.DEFAULT_WEIGHT_METRIC;
        }
        if (lb == InitParam.MIN_WEIGHT_IMPERIAL) {
            return InitParam.MIN_WEIGHT_METRIC;
        }
        if (lb == InitParam.MAX_WEIGHT_IMPERIAL) {
            return InitParam.MAX_WEIGHT_METRIC;
        }
        return (float) (Math.round((lb / k2) * 1000) / 1000.0);
    }

    public static float getCmToFt(float cm) {
        if (cm == 180) {
            return 6f;
        }
        return (float) (Math.round((cm * k3) * 1000) / 1000.0);
    }

    public static float getFtToCm(float ft) {
        if (ft == 6f) {
            return 180f;
        }
        return (float) (Math.round((ft / k3) * 1000) / 1000.0);
    }

    public static float getCmToIn(float cm) {
        return (float) (Math.round((cm * k4) * 1000) / 1000.0);
    }

    public static float getInToCm(float in) {
        return (float) (Math.round((in / k4) * 1000) / 1000.0);
    }

    public static float getKmToFt(float km) {
        return (float) (Math.round((km * k5) * 1000) / 1000.0);
    }

    public static float getFtToKm(float ft) {
        return (float) (Math.round((ft / k5) * 1000) / 1000.0);
    }

    public static float getMileToFt(float mile) {
        return (float) (Math.round((mile * k6) * 1000) / 1000.0);
    }

    public static float getFtToMile(float ft) {
        return (float) (Math.round((ft / k6) * 1000) / 1000.0);
    }

    public static float getMeterToFt(float meter) {
        return (float) (Math.round((meter * k7) * 1000) / 1000.0);
    }

    public static float getFtToMeter(float ft) {
        return (float) (Math.round((ft / k7) * 1000) / 1000.0);
    }

    public static float getFtToIn(float ft) {
        return (float) (Math.round((ft * k8) * 1000) / 1000.0);
    }

    public static float getInToFt(float in) {
        return (float) (Math.round((in / k8) * 1000) / 1000.0);
    }

    /**
     * @param km
     * @return 返回1位小数
     */
    public static float getKmToMileByFloat1(float km) {
        return (float) (Math.round((km / k1) * 10) / 10.0);
    }

    /**
     * @param km
     * @return 返回2位小数
     */
    public static float getKmToMileByFloat2(float km) {
        return (float) (Math.round((km / k1) * 100f) / 100.0);
    }

    /**
     * @param km
     * @return 返回4位小数
     */
    public static float getKmToMileByFloat4(float km) {
        return (float) (Math.round((km / k1) * 10000f) / 10000.0);
    }

    /**
     * @param mile
     * @return 返回1位小数
     */
    public static float getMileToKmByFloat1(float mile) {
        return (float) (Math.round((mile * k1 * 1000) / 1000f * 10) / 10.0);
    }

    /**
     * @param mile
     * @return 返回2位小数
     */
    public static float getMileToKmByFloat2(float mile) {
        return (float) (Math.round((mile * 1600) / 1000f * 100) / 100.0);
    }

    /**
     * @param mile
     * @return 返回4位小数
     */
    public static float getMileToKmByFloat4(float mile) {
        return (float) (Math.round((mile * k1) * 10000f) / 10000.0);
    }

    /**
     * @param cm
     * @return
     */
    public static float getCmToFtByFloat1(int cm) {
        return (float) (Math.round((cm * k3) * 10) / 10.0);
    }

    /**
     * @param cm
     * @return
     */
    public static float getCmToFtByFloat2(int cm) {
        return (float) (Math.round((cm * k3) * 100) / 100.0);
    }

    /**
     * @param ft
     * @return
     */
    public static float getFtToCmByFloat1(int ft) {
        return (float) (Math.round((ft / k3) * 10) / 10.0);
    }

    /**
     * @param ft
     * @return
     */
    public static float getFtToCmByFloat2(int ft) {
        return (float) (Math.round((ft / k3) * 100) / 100.0);
    }

    /**
     * @param cm
     * @return
     */
    public static float getCmToInByFloat1(int cm) {
        return (float) (Math.round((cm * k4) * 10) / 10.0);
    }

    /**
     * @param cm
     * @return
     */
    public static float getCmToInByFloat2(int cm) {
        return (float) (Math.round((cm * k4) * 100) / 100.0);
    }

    /**
     * @param in
     * @return
     */
    public static float getInToCmByFloat1(int in) {
        return (float) (Math.round((in / k4) * 10) / 10.0);
    }

    /**
     * @param in
     * @return
     */
    public static float getInToCmByFloat2(int in) {
        return (float) (Math.round((in / k4) * 100) / 100.0);
    }


    /**
     * @param km
     * @return
     */
    public static float getKmToFtByFloat1(float km) {
        return (float) (Math.round((km * k5) * 10) / 10.0);
    }

    /**
     * @param km
     * @return
     */
    public static float getKmToFtByFloat2(float km) {
        return (float) (Math.round((km * k5) * 100) / 100.0);
    }

    /**
     * @param ft
     * @return
     */
    public static float getFtToKmByFloat1(float ft) {
        return (float) (Math.round((ft / k5) * 10) / 10.0);
    }

    /**
     * @param ft
     * @return
     */
    public static float getFtToKmByFloat2(float ft) {
        return (float) (Math.round((ft / k5) * 100) / 100.0);
    }

    /**
     * @param mile
     * @return
     */
    public static float getMileToFtByFloat1(float mile) {
        return (float) (Math.round((mile * k6) * 10) / 10.0);
    }

    /**
     * @param mile
     * @return
     */
    public static float getMileToFtByFloat2(float mile) {
        return (float) (Math.round((mile * k6) * 100) / 100.0);
    }

    /**
     * @param ft
     * @return
     */
    public static float getFtToMileByFloat1(float ft) {
        return (float) (Math.round((ft / k6) * 10) / 10.0);
    }

    /**
     * @param ft
     * @return
     */
    public static float getFtToMileByFloat2(float ft) {
        return (float) (Math.round((ft / k6) * 100) / 100.0);
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
     * 取整数,小数点后面的完全放弃
     *
     * @param value
     * @return
     */
    public static int getFloatToIntClear(float value) {
        return (int) value;
    }


    /**
     * 取多少位小数,后面的完全放弃
     *
     * @param value
     * @return
     */
    public static float getFloatClearToPoint(float value, int point) {
        return new BigDecimal(value).setScale(point, BigDecimal.ROUND_FLOOR).floatValue();
    }

    /**
     * 取整数,小数点后面非0则自动进1
     *
     * @param value
     * @return
     */
    public static int getFloatToIntUp(float value) {
        return (int) (Math.ceil(value));
    }

    /**
     * 取多少位小数,(四舍五入)
     *
     * @param value
     * @param point
     * @return
     */
    public static float getFloatToPoint(float value, int point) {
        return (float) ((Math.round(value * Math.pow(10, point))) / Math.pow(10, point));
    }

    private static final DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);

    public static float getFloatBy1f(float value) {
        return getFloatToPoint(value, 1);
    }

    public static float getFloatBy2f(float value) {
        return getFloatToPoint(value, 2);
    }

    public static float getFloatBy3f(float value) {
        return getFloatToPoint(value, 3);
    }

    public static float getFloatBy4f(float value) {
        return getFloatToPoint(value, 4);
    }

    /**
     * double转float
     *
     * @param vlaue
     * @return
     */
    public static double getDoubleByFloat(float vlaue) {
        return Double.parseDouble(String.valueOf(vlaue));
    }

    /**
     * 设置保留小数（不足补零）
     *
     * @param value
     * @param point
     * @return
     */
    public static String getPoint(float value, int point) {
        StringBuilder defaultPoint = new StringBuilder("0.");
        if (point <= 0) {
            defaultPoint.deleteCharAt(defaultPoint.length() - 1);
        }
        while (point > 0) {
            defaultPoint.append("0");
            point--;
        }
        df.applyLocalizedPattern(defaultPoint.toString());
        return df.format(value);
    }

    /**
     * f1 - f2
     *
     * @param f1
     * @param f2
     * @return
     */
    public static float subtractFloat(float f1, float f2) {
        return new BigDecimal(Float.toString(f1)).subtract(new BigDecimal(Float.toString(f2))).floatValue();
    }

    /**
     * f1 + f2
     *
     * @param f1
     * @param f2
     * @return
     */
    public static float addFloat(float f1, float f2) {
        return new BigDecimal(Float.toString(f1)).add(new BigDecimal(Float.toString(f2))).floatValue();
    }

    /**
     * f1 * f2
     *
     * @param f1
     * @param f2
     * @return
     */
    public static float mulFloat(float f1, float f2) {
        return new BigDecimal(Float.toString(f1)).multiply(new BigDecimal(Float.toString(f2))).floatValue();
    }
}
