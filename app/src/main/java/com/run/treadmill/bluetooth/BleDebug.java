package com.run.treadmill.bluetooth;

public class BleDebug {
    public static final boolean debug = false;
    public static final boolean disableSerial = false;
}
/**

 這樣測就明顯了：
 插耳機的時候關閉聲音，移除耳機，把音量調到最大，再插回耳機，音量條是在最大的位置的，但耳機沒有聲音。
 因為前面插耳機的時候是關閉了聲音的，所以再插回去也還是沒有聲音了。

 */
