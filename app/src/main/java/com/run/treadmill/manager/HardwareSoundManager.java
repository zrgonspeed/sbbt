package com.run.treadmill.manager;

import com.run.treadmill.util.GpIoUtils;

public class HardwareSoundManager implements Runnable {
    public final String TAG = "HardwareSoundManager";

    public static final int HARDWARE_A33_1 = 11;
    public static final int HARDWARE_A33_2 = 12;

    public static final int HARDWARE_A133_1 = 51;

    public static final int HARDWARE_T3_1 = 21;
    private static HardwareSoundManager ourInstance = null;


    private Thread mCheckThread = null;

    private BaseHardwareMission mission;

    private HardwareSoundManager() {

    }

    public static HardwareSoundManager getInstance() {
        if (null == ourInstance) {
            synchronized (HardwareSoundManager.class) {
                if (null == ourInstance) {
                    ourInstance = new HardwareSoundManager();
                }
            }
        }
        return ourInstance;
    }

    @Override
    public void run() {
        mission.loopMissionStart();
    }


    /**
     * 初始化硬件与声音控制,该方法与 GpIoUtils 的初始化相关联
     *
     * @param hardwareType 可以 根据自身要求 自行实现
     */
    public void init(int hardwareType) {
        if (mCheckThread == null && mission == null) {
            if (hardwareType == HARDWARE_T3_1) {
                mission = new Hardware_T3_1();

            } else if (hardwareType == HARDWARE_A33_1) {
                mission = new Hardware_A33_1();

            } else if (hardwareType == HARDWARE_A33_2) {
                mission = new Hardware_A33_2();

            } else if (hardwareType == HARDWARE_A133_1) {
                mission = new Hardware_A133_1();
            }
            if (mission != null) {
                mCheckThread = new Thread(this);
                mCheckThread.start();
            }
        }
    }

    /**
     * 播放输入声源,hdmi声源
     */
    public static void setVoiceFromOutSide() {
        if (ourInstance != null && ourInstance.mission != null) {
            ourInstance.mission.setVoiceFromOutSide();
            return;
        }
        GpIoUtils.setSystemSound_A_1();
        GpIoUtils.setSystemSound_B_0();
    }

    /**
     * 播放系统声音
     */
    public static void setVoiceFromSystem() {
        if (ourInstance != null && ourInstance.mission != null) {
            ourInstance.mission.setVoiceFromSystem();
            return;
        }
        GpIoUtils.setSystemSound_A_1();
        GpIoUtils.setSystemSound_B_1();
    }

    private interface HardwareMission {
        /**
         * 在线程中执行的循环任务
         */
        void loopMissionStart();


        /**
         * 播放外接音源的 gpio 操作
         */
        void setVoiceFromOutSide();

        /**
         * 播放系统音源的 gpio 操作
         */
        void setVoiceFromSystem();
    }

    private abstract class BaseHardwareMission implements HardwareMission {

        /**
         * 耳机接入状态
         */
        protected int earPhoneState = -1;

        /**
         * 喇叭开关状态
         */
        protected int loudspeakerState = -1;

        /**
         * 外部音源状态
         */
        protected int ioOutSideState = -1;

        /**
         * 上一次设置的 外部音源状态
         */
        protected int lastIoOutSideState = -1;

        /**
         * 上一次设置的 喇叭开关状态
         */
        protected int lastLoudspeakerState = -1;

        /**
         * 控制外接喇叭开关
         */
        protected synchronized void setLoudspeakerState(int io_state) {
            if (lastLoudspeakerState != loudspeakerState) {
                lastLoudspeakerState = loudspeakerState;
                GpIoUtils.setLoudspeaker(io_state);
            }
        }

        /**
         * 设置系统当前播放的声音源
         *
         * @param A_io_state A通道状态:0,1
         * @param B_io_state B通道状态:0,1
         */
        protected synchronized void setSystemVoiceFrom(int A_io_state, int B_io_state) {
            if (lastIoOutSideState != ioOutSideState) {
                lastIoOutSideState = ioOutSideState;
                GpIoUtils.setSystemSound_A(A_io_state);
                GpIoUtils.setSystemSound_B(B_io_state);
            }
        }
    }

    private class Hardware_T3_1 extends BaseHardwareMission {
        @Override
        public synchronized void loopMissionStart() {

        }

        @Override
        public void setVoiceFromOutSide() {
            GpIoUtils.setSystemSound_A_1();
            GpIoUtils.setSystemSound_B_0();
        }

        @Override
        public void setVoiceFromSystem() {
            GpIoUtils.setSystemSound_A_1();
            GpIoUtils.setSystemSound_B_1();
        }
    }


    /**
     * A33控制任务类型1: 根据耳机线的接入,控制喇叭声音
     */
    private class Hardware_A33_1 extends BaseHardwareMission {
        @Override
        public synchronized void loopMissionStart() {
            while (true) {
                try {
                    Thread.sleep(1000);

                    earPhoneState = GpIoUtils.checkEarPhoneState();
                    loudspeakerState = GpIoUtils.checkLoudspeakerState();

                    if (earPhoneState == GpIoUtils.IO_STATE_0) {
                        //TODO:耳机接入,关闭外接喇叭
                        setLoudspeakerState(GpIoUtils.IO_STATE_0);

                    } else if (earPhoneState == GpIoUtils.IO_STATE_1) {
                        //TODO:耳机拔出,打开外接喇叭
                        setLoudspeakerState(GpIoUtils.IO_STATE_1);
                    }
                } catch (Exception ignore) {

                }
            }
        }

        @Override
        public void setVoiceFromOutSide() {
            GpIoUtils.setSystemSound_A_1();
            GpIoUtils.setSystemSound_B_0();
        }

        @Override
        public void setVoiceFromSystem() {
            GpIoUtils.setSystemSound_A_1();
            GpIoUtils.setSystemSound_B_1();
        }
    }

    /**
     * A33控制任务类型2: 根据耳机线与外接音源的接入,控制当前喇叭开关与声音来源
     */
    private class Hardware_A33_2 extends BaseHardwareMission {
        @Override
        public synchronized void loopMissionStart() {
            while (true) {
                try {
                    Thread.sleep(1000);

                    earPhoneState = GpIoUtils.checkEarPhoneState();
                    ioOutSideState = GpIoUtils.checkOutSideSoundState();
                    if (ioOutSideState == GpIoUtils.IO_STATE_0) {
                        //TODO:外部音源接入,播放外部音源
                        setSystemVoiceFrom(GpIoUtils.IO_STATE_1, GpIoUtils.IO_STATE_0);

                        if (earPhoneState == GpIoUtils.IO_STATE_0) {
                            //TODO:耳机接入,关闭外接喇叭
                            setLoudspeakerState(GpIoUtils.IO_STATE_0);
                        } else if (earPhoneState == GpIoUtils.IO_STATE_1) {
                            //TODO:耳机拔出,打开外接喇叭
                            setLoudspeakerState(GpIoUtils.IO_STATE_1);
                        }
                    } else if (ioOutSideState == GpIoUtils.IO_STATE_1) {
                        //TODO:外部音源拔出或者没有接入,播放系统音源
                        setSystemVoiceFrom(GpIoUtils.IO_STATE_1, GpIoUtils.IO_STATE_1);

                        if (earPhoneState == GpIoUtils.IO_STATE_0) {
                            //TODO:耳机接入,关闭外接喇叭
                            setLoudspeakerState(GpIoUtils.IO_STATE_0);
                        } else if (earPhoneState == GpIoUtils.IO_STATE_1) {
                            //TODO:耳机拔出,打开外接喇叭
                            setLoudspeakerState(GpIoUtils.IO_STATE_1);
                        }
                    }
                } catch (Exception ignore) {

                }
            }

        }

        @Override
        public void setVoiceFromOutSide() {
            GpIoUtils.setSystemSound_A_1();
            GpIoUtils.setSystemSound_B_0();
        }

        @Override
        public void setVoiceFromSystem() {
            GpIoUtils.setSystemSound_A_1();
            GpIoUtils.setSystemSound_B_1();
        }
    }

    private class Hardware_A133_1 extends BaseHardwareMission {
        @Override
        public synchronized void loopMissionStart() {
            while (true) {
                try {
                    Thread.sleep(1000);

                    ioOutSideState = GpIoUtils.checkOutSideSoundState();
                    //TODO: 0是接入，1是没有接入(根据硬件设计有所改变)
                    if (ioOutSideState == GpIoUtils.IO_STATE_0) {
                        //TODO:外部音源接入,播放外部音源
                        setSystemVoiceFrom(GpIoUtils.IO_STATE_1, GpIoUtils.IO_STATE_0);
                    } else if (ioOutSideState == GpIoUtils.IO_STATE_1) {
                        //TODO:外部音源拔出或者没有接入,播放系统音源
                        setSystemVoiceFrom(GpIoUtils.IO_STATE_1, GpIoUtils.IO_STATE_1);
                    }

                } catch (Exception ignore) {

                }
            }

        }

        @Override
        public void setVoiceFromOutSide() {
            GpIoUtils.setSystemSound_A_0();
            GpIoUtils.setSystemSound_B_0();
        }

        @Override
        public void setVoiceFromSystem() {
            GpIoUtils.setSystemSound_A_1();
            GpIoUtils.setSystemSound_B_1();
        }
    }
}
