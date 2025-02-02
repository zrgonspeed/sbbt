package com.run.treadmill.manager;

import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;

public class HardwareSoundManager implements Runnable {
    public final String TAG = "HardwareSoundManager";

    public static final int HARDWARE_A133_1 = 51;

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
            if (hardwareType == HARDWARE_A133_1) {
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

        /**
         * 播放hdmi in的 gpio 操作
         */
        void setVoiceFromHdmi_in();
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
         * 系统当前播声音来源
         */
        protected int systemVoiceState = -1;
        /**
         * 系上一次设置的统当前播声音来源
         */
        protected int lastSystemVoiceState = -1;


        /**
         * 外部音源状态
         */
        protected int ioOutSideState = -1;

        /**
         * hdmi in音源状态
         */
        protected int ioHdmiInState = -1;

        /**
         * 上一次设置的 喇叭开关状态
         */
        protected int lastLoudspeakerState = -1;


        /**
         * 控制外接喇叭开关
         */
        protected synchronized void setLoudspeakerState(int io_state) {
            if (lastLoudspeakerState != io_state) {
                lastLoudspeakerState = io_state;
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
            //Logger.e("lastSystemVoiceState == " + lastSystemVoiceState + "    systemVoiceState == " + systemVoiceState);
            if (lastSystemVoiceState != ioOutSideState) {
                lastSystemVoiceState = ioOutSideState;

                //Logger.e("setSystemSound_A setSystemSound_B");
                GpIoUtils.setSystemSound_A(A_io_state);
                GpIoUtils.setSystemSound_B(B_io_state);
            }
        }
    }


    /**
     * A133控制任务1:持续检查外接音源状态,切换系统当前播放音源
     */
    private class Hardware_A133_1 extends BaseHardwareMission {
        public boolean record = true;

        @Override
        public synchronized void loopMissionStart() {
            while (true) {
                try {
                    Thread.sleep(1000);

                    // 两个硬件插口
                    int b3 = GpIoUtils.checkOutSideSoundState_B3();
                    int b4 = GpIoUtils.checkOutSideSoundState_B4();
                    // Logger.i("io1_B3 == " + b3 + "   io2_B4 == " + b4);

                    if (b3 == 0 || b4 == 0) {
                        ioOutSideState = 0;
                    } else {
                        ioOutSideState = 1;
                    }

                    systemVoiceState = GpIoUtils.checkSystemVoiceFrom();

                    // Logger.e("ioOutSideState == " + ioOutSideState + "    systemVoiceState == " + systemVoiceState);
                    //TODO: 0是接入，1是没有接入(根据硬件设计有所改变)
                    if (ioOutSideState == GpIoUtils.IO_STATE_0) {
                        //TODO:外部音源接入,播放外部音源
                        if (b3 == 0) {
                            setSystemVoiceFrom(GpIoUtils.IO_STATE_1, GpIoUtils.IO_STATE_0);
                        } else if (b4 == 0) {
                            setSystemVoiceFrom(GpIoUtils.IO_STATE_0, GpIoUtils.IO_STATE_1);
                        }
                        if (record) {
                            startOrStopRecordPlay(true);
                            record = false;
                        }
                    } else if (ioOutSideState == GpIoUtils.IO_STATE_1) {
                        //TODO:外部音源拔出或者没有接入,播放系统音源
                        setSystemVoiceFrom(GpIoUtils.IO_STATE_1, GpIoUtils.IO_STATE_1);
                        if (!record) {
                            startOrStopRecordPlay(false);
                            record = true;
                        }
                    }

                } catch (Exception ignore) {
                    Logger.e(TAG, ignore.getMessage());
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

        @Override
        public void setVoiceFromHdmi_in() {

        }

        private void startOrStopRecordPlay(boolean start) {
            if (start) {
                RecordPlayManager.getInstance().startRecordPlay();
            } else {
                RecordPlayManager.getInstance().stopRecordPlay();
            }
        }
    }
}
