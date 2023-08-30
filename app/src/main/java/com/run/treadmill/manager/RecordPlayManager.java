package com.run.treadmill.manager;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import com.run.treadmill.util.Logger;

import java.util.LinkedList;

/**
 * 参考文章(整体实现)        :https://blog.csdn.net/qq_36982160/article/details/79383046
 * 参考文章(AudioRecord介绍) :https://www.cnblogs.com/mythou/p/3241925.html
 * 参考文章(AudioTrack介绍)  :https://www.jianshu.com/p/74fa2b1ac61f
 */
public class RecordPlayManager {
    private static final String TAG = "RecordPlayManager";

    private static RecordPlayManager ourInstance;

    /**
     * 录制音频源
     */
    private int recordAudioSource = MediaRecorder.AudioSource.CAMCORDER;

    /**
     * 播放音频流类型
     */
    private int playAudioSource = AudioManager.STREAM_MUSIC;

    /**
     * 采样率
     */
    private int sampleRateInHz = 44100;

    /**
     * 声道设置
     */
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;

    /**
     * 编码制式和采样大小
     */
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 音频数据载入模式
     */
    private int mode = AudioTrack.MODE_STREAM;


    /**
     * AudioRecord 写入缓冲区大小
     */
    private int m_in_buf_size;
    /**
     * 录制音频对象
     */
    private AudioRecord m_in_rec;
    /**
     * 录入的字节数组
     */
    private byte[] m_in_bytes;

    /**
     * 存放录入字节数组的大小
     */
    private LinkedList<byte[]> m_in_q;
    /**
     * AudioTrack 播放缓冲大小
     */
    private int m_out_buf_size;
    /**
     * 播放音频对象
     */
    private AudioTrack m_out_trk;
    /**
     * 播放的字节数组
     */
    private byte[] m_out_bytes;
    private Thread recordAndPlay;
    /**
     * 让线程停止的标志
     */
    private boolean flag = true;
    private boolean flagThread = true;

    public static RecordPlayManager getInstance() {

        if (null == ourInstance) {
            synchronized (RecordPlayManager.class) {
                if (null == ourInstance) {
                    ourInstance = new RecordPlayManager();
                }
            }
        }
        return ourInstance;
    }

    private RecordPlayManager() {
        init();
    }

    public synchronized void init() {
        try {
            // AudioRecord 得到录制最小缓冲区的大小
            m_in_buf_size = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

            // 实例化录制音频对象
            m_in_rec = new AudioRecord(recordAudioSource, sampleRateInHz, channelConfig, audioFormat, m_in_buf_size);
            // 实例化一个字节数组，长度为最小缓冲区的长度
            m_in_bytes = new byte[m_in_buf_size];

            // 实例化一个链表，用来存放字节组数
            m_in_q = new LinkedList<byte[]>();

            // AudioTrack 得到播放最小缓冲区的大小
            m_out_buf_size = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
            // 实例化播放音频对象
            m_out_trk = new AudioTrack(playAudioSource, sampleRateInHz, channelConfig, audioFormat, m_out_buf_size, mode);
            // 实例化一个长度为播放最小缓冲大小的字节数组
            m_out_bytes = new byte[m_out_buf_size];

        } catch (Exception ignore) {
        }
    }


    public synchronized void startRecordPlay() {
        flag = true;
        flagThread = true;
        if (recordAndPlay == null) {
            recordAndPlay = new Thread(new RecordAndPlayThread());
            recordAndPlay.start();
        }
    }

    public synchronized void stopRecordPlay() {
        try {
            flag = false;
            //flagThread = false;
        } catch (Exception ignore) {
        }
    }

    public synchronized void releaseRecordPlay() {
        try {
            flag = false;
            flagThread = false;
            recordAndPlay = null;
            m_in_rec.release();
            m_out_trk.release();
        } catch (Exception ignore) {
        }
    }

    public synchronized void restartInitRecord(int source) {
        recordAudioSource = source;//MediaRecorder.AudioSource.MIC;
        init();
    }

    private class RecordAndPlayThread implements Runnable {
        @Override
        public void run() {
            try {
                int size;
                while (flagThread) {
                    Logger.d(TAG, "........recordSound run()......");
                    m_in_rec.startRecording();
                    m_out_trk.play();
                    while (flag) {
                        size = m_in_rec.read(m_in_bytes, 0, m_in_buf_size);
                        byte[] byteYuanNew = new byte[size];
                        AmplifyPCMData(m_in_bytes, size, byteYuanNew, 16, (float) factor);
                        m_out_trk.write(byteYuanNew, 0, size);
                        //m_out_trk.write(m_in_bytes, 0, size);
                    }
                    m_in_rec.stop();
                    m_out_trk.stop();

                    while (flagThread) {
                        if (flag) {
                            break;
                        }
                        Thread.sleep(1003);
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }

    private short SHRT_MAX = (short) 0x7F00;
    private short SHRT_MIN = (short)-0x7F00;
    //db为0表示保持音量不变，db为负数表示较低音量，为正数表示提高音量
    private int db = 2;
    private double factor = Math.pow(10, (double)db / 20);

    private short getShort(byte[] data, int start) {
        return (short)((data[start] & 0xFF) | (data[start+1] << 8));
    }

    int AmplifyPCMData(byte[] pData, int nLen, byte[] data2, int nBitsPerSample, float multiple) {
        int nCur = 0;
        if (16 == nBitsPerSample) {
            while (nCur < nLen) {
                short volum = getShort(pData, nCur);
                volum = (short) (volum * multiple);
                if (volum < SHRT_MIN) {
                    volum = SHRT_MIN;
                }
                if (volum > SHRT_MAX) {//爆音的处理
                    volum = SHRT_MAX;
                }
                data2[nCur] = (byte)( volum & 0xFF);
                data2[nCur+1] = (byte)((volum >> 8) & 0xFF);
                nCur += 2;
            }
        }
        return 0;
    }

}
