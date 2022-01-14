package com.run.treadmill.manager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.IntDef;

import com.run.serial.SerialUtils;
import com.run.treadmill.R;
import com.run.treadmill.serial.SerialKeyValue;
import com.run.treadmill.util.GpIoUtils;
import com.run.treadmill.util.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/14
 */
public class BuzzerManager {
    private static final String TAG = BuzzerManager.class.getSimpleName();
    public static boolean canBuzzerWhenLongKey = true;

    public static final int BUZZER_CMD = 1;
    public static final int BUZZER_SYSTEM = 2;
    public static final int BUZZER_IO = 3;

    private static BuzzerManager instance;
    private boolean canBuzzer = true;
    private @BuzzerType
    int currType;

    @IntDef({BUZZER_CMD, BUZZER_SYSTEM, BUZZER_IO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BuzzerType {
    }

    private Strategy mStrategy;

    public static BuzzerManager getInstance() {
        if (instance == null) {
            synchronized (BuzzerManager.class) {
                if (instance == null) {
                    instance = new BuzzerManager();
                }
            }
        }
        return instance;
    }

    private BuzzerManager() {
    }

    private Context mContext;

    /**
     * 最好全局设置好蜂鸣的方式，一次性
     *
     * @param buzzType
     */
    public void init(@BuzzerType int buzzType, Context context) {
        mContext = context;

        currType = buzzType;
        switch (buzzType) {
            case BUZZER_CMD:
                mStrategy = new BuzzCmd();
                break;
            case BUZZER_SYSTEM:
                mStrategy = new BuzzSystem(context);
                break;
            case BUZZER_IO:
                mStrategy = new BuzzIO();
                break;
            default:
                break;
        }
    }

    public @BuzzerType
    int getBuzzerType() {
        return this.currType;
    }

    public void setBuzzerEnable(boolean enable) {
        this.canBuzzer = enable;
    }

    /**
     * 蜂鸣一声
     */
    public synchronized void buzzerRingOnce() {
        if (!canBuzzer) {
            return;
        }

        // 连续key的按键音取消 (speed incline)
        int keyValue = SerialUtils.keyValue;
//        Logger.e("keyValue == " + keyValue);
        if (keyValue == SerialKeyValue.INCLINE_UP_CLICK_LONG_1 || keyValue == SerialKeyValue.INCLINE_UP_CLICK_LONG_2
                || keyValue == SerialKeyValue.SPEED_UP_CLICK_LONG_1 || keyValue == SerialKeyValue.SPEED_UP_CLICK_LONG_2
                || keyValue == SerialKeyValue.INCLINE_DOWN_CLICK_LONG_1 || keyValue == SerialKeyValue.INCLINE_DOWN_CLICK_LONG_2
                || keyValue == SerialKeyValue.SPEED_DOWN_CLICK_LONG_1 || keyValue == SerialKeyValue.SPEED_DOWN_CLICK_LONG_2
                || keyValue == SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_1 || keyValue == SerialKeyValue.INCLINE_UP_HAND_CLICK_LONG_2
                || keyValue == SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_1 || keyValue == SerialKeyValue.SPEED_UP_HAND_CLICK_LONG_2
                || keyValue == SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_1 || keyValue == SerialKeyValue.INCLINE_DOWN_HAND_CLICK_LONG_2
                || keyValue == SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_1 || keyValue == SerialKeyValue.SPEED_DOWN_HAND_CLICK_LONG_2
        ) {
            return;
        }


//        Logger.e("canBuzzerWhenLongKey == " + canBuzzerWhenLongKey);
        if (!canBuzzerWhenLongKey) {
            return;
        }

        mStrategy.buzzerRingOnce();
    }

    /**
     * 响多久
     *
     * @param time
     */
    public synchronized void buzzerRingLong(long time) {
        if (!canBuzzer || time <= 0) {
            return;
        }
        mStrategy.buzzerRingLong(time);
    }

    /**
     * 蜂鸣多久多少次
     *
     * @param time
     * @param count
     */
    public synchronized void buzzerRingMoreTime(long time, int count) {
        if (!canBuzzer || time <= 0 || count < 0) {
            return;
        }
        mStrategy.buzzerRingMoreTime(time, count);
    }

    /**
     * 停止蜂鸣
     */
    public synchronized void buzzerStop() {
        if (!canBuzzer) {
            return;
        }
        mStrategy.buzzerStop();
    }

    /**
     * 响一声（无关声音开关）
     *
     * @param time
     */
    public synchronized void buzzRingLongObliged(long time) {
        mStrategy.buzzerRingLong(time);
    }

    interface Strategy {
        /**
         * 蜂鸣一次
         */
        void buzzerRingOnce();

        /**
         * 蜂鸣一次的时长
         *
         * @param time 时长
         */
        void buzzerRingLong(long time);

        /**
         * 蜂鸣多久多次
         *
         * @param time  时长
         * @param count 次数
         */
        void buzzerRingMoreTime(long time, int count);

        /**
         * 停止蜂鸣
         */
        void buzzerStop();
    }

    class BuzzCmd implements Strategy {
        /**
         * 最后下发bi一声的时间
         */
        long lastBuzzOnceTime;

        @Override
        public void buzzerRingOnce() {
            //需要间隔一个   bi~时间 + 常态包 时间
            if (System.currentTimeMillis() < (lastBuzzOnceTime + 10 + 140)) {
                return;
            }
            lastBuzzOnceTime = System.currentTimeMillis();
//            ControlManager.getInstance().buzz(1, 10);
        }

        @Override
        public void buzzerRingLong(long time) {
//            ControlManager.getInstance().buzz(1, (int) time / 10);
        }

        @Override
        public void buzzerRingMoreTime(long time, int count) {
//            ControlManager.getInstance().buzz(count, (int) time / 10);

        }

        @Override
        public void buzzerStop() {
//            ControlManager.getInstance().buzz(0, 0);
        }
    }

    /**
     * 系统声音暂时无法控制次数和时长
     */
    class BuzzSystem implements Strategy {
        private Map<String, Integer> poolMap;
        private SoundPool mSoundPool;
        private AudioManager mAudioManager;

        private int playId = -1;

        BuzzSystem(Context context) {
            poolMap = new HashMap<>();
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build();
            //实例化soundPool
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(3)//音频数量
                    .setAudioAttributes(audioAttributes)
                    .build();

            poolMap.put("spasm", mSoundPool.load(context, R.raw.beep_once, 1));
            poolMap.put("start_short", mSoundPool.load(context, R.raw.beep_200, 1));
            poolMap.put("start_long", mSoundPool.load(context, R.raw.beep_1000, 1));
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        @Override
        public void buzzerRingOnce() {
            Logger.d(TAG, "buzzerRingOnce()");
//            mAudioManager.playSoundEffect(SoundEffectConstants.CLICK);
            mSoundPool.play(poolMap.get("spasm"), 1, 1, 1000, 0, 1f);
        }

        @Override
        public void buzzerRingLong(long time) {
            if (playId == 0) {
                mSoundPool.stop(playId);
            }
            if (time == 200) {
                playId = mSoundPool.play(poolMap.get("start_short"), 0.5f, 1.0f, 0, 0, 1.0f);
            } else if (time == 1000) {
                playId = mSoundPool.play(poolMap.get("start_long"), 0.5f, 1.0f, 0, 0, 1.0f);
            }
        }

        @Override
        public void buzzerRingMoreTime(long time, int count) {

        }

        @Override
        public void buzzerStop() {
            mSoundPool.pause(playId);
            playId = -1;
        }
    }

    class BuzzIO implements Strategy {
        private Handler buzzHandler;

        BuzzIO() {
            loop();
        }

        @Override
        public void buzzerRingOnce() {
            buzzerRingLong(30);
        }

        @Override
        public void buzzerRingLong(long time) {
            if (time == 3210) {
                return;
            }
            if (time <= 0 || time >= 2000) {
                time = 100;
            }
            GpIoUtils.IOBuzzer_1();
            buzzHandler.sendEmptyMessageDelayed(0, time);
        }

        @Override
        public void buzzerRingMoreTime(long time, int count) {
            if (time <= 0 || time >= 2000) {
                time = 100;
            }
            for (int i = 0; i < count; i++) {
                buzzHandler.sendEmptyMessageDelayed(1, (i * (80 + time)));
                buzzHandler.sendEmptyMessageDelayed(0, ((i + 1) * time + 80 * i));

            }
        }

        @Override
        public void buzzerStop() {
            GpIoUtils.IOBuzzer_0();
            buzzHandler.removeMessages(0);
            buzzHandler.removeMessages(1);
        }

        void loop() {
            if (buzzHandler != null) {
                return;
            }
            new Thread(() -> {
                Looper.prepare();
                buzzHandler = new Handler(Looper.myLooper()) {
                    @Override
                    public synchronized void handleMessage(Message msg) {
                        switch (msg.what) {
                            case 1:
                                GpIoUtils.IOBuzzer_1();
                                break;
                            default:
                                GpIoUtils.IOBuzzer_0();
                                break;
                        }
                    }
                };
                Looper.loop();
            }).start();
        }
    }
}
