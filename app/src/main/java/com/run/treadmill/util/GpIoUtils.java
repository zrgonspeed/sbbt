package com.run.treadmill.util;

import com.softwinner.Gpio;

public class GpIoUtils {

    public static int HARDWARE_T3 = 0;
    public static int HARDWARE_A33 = 1;
    public static int HARDWARE_A133 = 2;


    public static int IO_STATE_1 = 1;
    public static int IO_STATE_0 = 0;
    public static int IO_STATE_NOT_EXIT = -1;


    public static final int VOICE_FROM_SYSTEM = 0;
    public static final int VOICE_FROM_OUTSIDE = 1;
    public static final int VOICE_FROM_HDMI_IN = 3;
    public static final int VOICE_FROM_OTHER = 4;

    /**
     * 控制屏幕开关
     */
    private static char GROUP_SCREEN = 'L';
    private static int GROUP_SCREEN_NUM = 8;

    /**
     * 蜂鸣器开关(AC00460/461有用到)
     */
    private static char GROUP_BUZZER = 'B';
    private static int GROUP_BUZZER_NUM = 4;

    /**
     * 耳机拔出,插入状态
     */
    private static char GROUP_EAR_PHONE = 'C';
    private static int GROUP_EAR_PHONE_NUM = 18;

    /**
     * 外接音源状态 1
     */
    private static char GROUP_OUTSIDE_SOUND = 'B';
    private static int GROUP_OUTSIDE_SOUND_NUM = 3;

    /**
     * 外接音源状态 2
     */
    private static char GROUP_OUTSIDE_SOUND_2 = 'B';
    private static int GROUP_OUTSIDE_SOUND_NUM_2 = 4;

    /**
     * 喇叭开关
     */
    private static char GROUP_LOUDSPEAKER = 'E';
    private static int GROUP_LOUDSPEAKER_NUM = 15;

    private static char GROUP_SYSTEM_SOUND_A = 'B';
    private static int GROUP_SYSTEM_SOUND_A_NUM = 5;

    private static char GROUP_SYSTEM_SOUND_B = 'B';
    private static int GROUP_SYSTEM_SOUND_B_NUM = 6;


    private GpIoUtils() {

    }

    /**
     * 初始化GpIo控制类型
     *
     * @param deviceType 0->T3,1->A33,2->A133
     */
    public synchronized static void init(int deviceType) {
        Gpio.loadLibrary();
        if (deviceType == HARDWARE_A133) {
            setA133_IO();
        }
    }

    private static void setA133_IO() {
        GROUP_SCREEN = 'B';
        GROUP_SCREEN_NUM = 8;

        GROUP_BUZZER = 'B';
        GROUP_BUZZER_NUM = 4;

        GROUP_EAR_PHONE = 'C';
        GROUP_EAR_PHONE_NUM = 18;

        GROUP_OUTSIDE_SOUND = 'B';
        GROUP_OUTSIDE_SOUND_NUM = 3;

        GROUP_SYSTEM_SOUND_A = 'B';
        GROUP_SYSTEM_SOUND_A_NUM = 5;

        GROUP_SYSTEM_SOUND_B = 'B';
        GROUP_SYSTEM_SOUND_B_NUM = 6;
    }

    /**
     * @param group 组名称
     * @param num   组内编号
     * @param value 0,1
     */
    public synchronized static void writeGPIO(char group, int num, int value) {
        Gpio.writeGpio(group, num, value);
    }

    public synchronized static int readGPIO(char group, int num) {
        return Gpio.readGpio(group, num);
    }

    /**
     * 手动控制IO的蜂鸣器
     *
     * @param io_state 0,1
     */
    public static void IOBuzzer(int io_state) {
        writeGPIO(GROUP_BUZZER, GROUP_BUZZER_NUM, io_state);
    }

    /**
     * 手动控制IO的蜂鸣器,设置 状态为 1
     */
    public static void IOBuzzer_1() {
        IOBuzzer(IO_STATE_1);
    }

    /**
     * 手动控制IO的蜂鸣器,设置 状态为 0
     */
    public static void IOBuzzer_0() {
        IOBuzzer(IO_STATE_0);
    }

    /**
     * 读取当前屏幕状态
     */
    public static int checkScreenState() {
        return readGPIO(GROUP_SCREEN, GROUP_SCREEN_NUM);
    }


    /**
     * 屏幕状态 设置
     *
     * @param io_state 0,1
     */
    public static void setScreen(int io_state) {
        writeGPIO(GROUP_SCREEN, GROUP_SCREEN_NUM, io_state);
    }

    /**
     * 屏幕状态设为 1
     */
    public static void setScreen_1() {
        setScreen(IO_STATE_1);
    }

    /**
     * 屏幕状态设为 0
     */
    public static void setScreen_0() {
        setScreen(IO_STATE_0);
    }

    /**
     * 检测是否插入外接耳机
     *
     * @return 0是接入，1是没有接入(根据硬件设计有所改变)
     */
    public static int checkEarPhoneState() {
        return readGPIO(GROUP_EAR_PHONE, GROUP_EAR_PHONE_NUM);
    }

    /**
     * 检测外部音源接入
     *
     * @return 0是接入，1是没有接入(根据硬件设计有所改变)
     */
    public static int checkOutSideSoundState() {
        return readGPIO(GROUP_OUTSIDE_SOUND, GROUP_OUTSIDE_SOUND_NUM);
    }

    public static int checkOutSideSoundState_B3() {
        return readGPIO(GROUP_OUTSIDE_SOUND, GROUP_OUTSIDE_SOUND_NUM);
    }

    public static int checkOutSideSoundState_B4() {
        return readGPIO(GROUP_OUTSIDE_SOUND_2, GROUP_OUTSIDE_SOUND_NUM_2);
    }

    /**
     * 外接喇叭开关状态
     *
     * @return 0是打开.1是关闭(根据硬件设计有所改变)
     */
    public static int checkLoudspeakerState() {
        return readGPIO(GROUP_LOUDSPEAKER, GROUP_LOUDSPEAKER_NUM);
    }

    /**
     * 外接喇叭状态
     *
     * @param io_state 0,1
     */
    public static void setLoudspeaker(int io_state) {
        writeGPIO(GROUP_LOUDSPEAKER, GROUP_LOUDSPEAKER_NUM, io_state);
    }

    /**
     * 外接喇叭状态设为 1
     */
    public static void setLoudspeaker_1() {
        setLoudspeaker(IO_STATE_1);
    }

    /**
     * 外接喇叭状态设为 0
     */
    public static void setLoudspeaker_0() {
        setLoudspeaker(IO_STATE_0);
    }

    public static void setSystemSound_A(int io_state) {
        writeGPIO(GROUP_SYSTEM_SOUND_A, GROUP_SYSTEM_SOUND_A_NUM, io_state);
    }

    public static void setSystemSound_A_1() {
        setSystemSound_A(IO_STATE_1);
    }

    public static void setSystemSound_A_0() {
        setSystemSound_A(IO_STATE_0);
    }

    public static void setSystemSound_B(int io_state) {
        writeGPIO(GROUP_SYSTEM_SOUND_B, GROUP_SYSTEM_SOUND_B_NUM, io_state);
    }

    public static void setSystemSound_B_1() {
        setSystemSound_B(IO_STATE_1);
    }

    public static void setSystemSound_B_0() {
        setSystemSound_B(IO_STATE_0);
    }

    public static int checkSystemVoice_A() {
        return readGPIO(GROUP_SYSTEM_SOUND_A, GROUP_SYSTEM_SOUND_A_NUM);
    }

    public static int checkSystemVoice_B() {
        return readGPIO(GROUP_SYSTEM_SOUND_B, GROUP_SYSTEM_SOUND_B_NUM);
    }

    public static int checkSystemVoiceFrom() {
        int sys_A = checkSystemVoice_A();
        int sys_B = checkSystemVoice_B();
        if (sys_A == GpIoUtils.IO_STATE_0) {

            if (sys_B == GpIoUtils.IO_STATE_0) {
                return VOICE_FROM_HDMI_IN;
            } else if (sys_B == GpIoUtils.IO_STATE_1) {
                return VOICE_FROM_OTHER;
            }

        } else if (sys_A == GpIoUtils.IO_STATE_1) {

            if (sys_B == GpIoUtils.IO_STATE_0) {
                return VOICE_FROM_OUTSIDE;
            } else if (sys_B == GpIoUtils.IO_STATE_1) {
                return VOICE_FROM_SYSTEM;
            }

        }

        return -1;
    }
}
