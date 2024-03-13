package com.run.treadmill.widget;

import android.app.Activity;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import com.run.treadmill.activity.runMode.RunningParam;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.util.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VideoPlayerSelf implements SurfaceHolder.Callback {
    private String TAG = "VideoPlayerSelf";
    private String SAMPLE = "/storage/card/BEACH.mp4";
    private PlayerThread mPlayer = null;
    private MediaExtractor extractor;
    private MediaCodec decoder;
    private int TIME = 40;
    private int waitTime = TIME;
    private SurfaceView sv;
    private boolean isFirst = true;
    private LinearLayout mLinearLayout;

    /**
     * 用于计算什么时间进度的时候回调
     */
    private int mTime;
    private OnTimeCallBack callBack;
    private float maxSpeed, minSpeed;

    //ms
    /*private long duration = 0l;*/
    private Context mContext;
    private int oldTimeSample = -1;
    private int curTimeSample = 0;

    private boolean isThreadEnd = false;

    public VideoPlayerSelf(Context context, int resId, LinearLayout linearLayout, String src) {
        SAMPLE = src;

        mContext = context;
//        mLinearLayout = linearLayout;
//        mLinearLayout.setBackgroundResource(R.drawable.bk_01);

        sv = (SurfaceView) ((Activity) context).findViewById(resId);
        sv.getHolder().addCallback(this);
    }

    public VideoPlayerSelf(Context context, SurfaceView surfaceView, String src) {
        SAMPLE = src;
        mContext = context;
        sv = surfaceView;
        sv.getHolder().addCallback(this);
    }

    public synchronized void videoPlayerStart() {
        Log.d(TAG, "videoPlayerStart ==" + mPlayer);
        if (mPlayer != null && !mPlayer.isPalying) {
            mPlayer.startThread();
//            mLinearLayout.setBackgroundResource(R.color.default_black);
            mPlayer.isPalying = true;
        }
    }

    public synchronized void videoPlayerStartPause() {
        Log.d(TAG, "videoPlayerStartPause ==" + mPlayer);
        if (mPlayer != null && mPlayer.isPalying) {
            mPlayer.pauseThread();
        }
    }

    /**
     * 设置最大最小速度
     *
     * @param minSpeed 公制
     * @param maxSpeed 公制
     */
    public void setMinMaxSpeed(float minSpeed, float maxSpeed) {
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
    }

    public void setSpeedCtrl(float amount) {
        if (RunningParam.getInstance().isCoolDownStatus()) {
            Logger.i("当前是Cool Down 停止播放视频");
            videoPlayerStartPause();
            return;
        }
//		Log.d(TAG,"onAmountChange " + amount  );
//        if (amount >= 10) {
//            waitTime = TIME - TIME / 10 * (amount - 10);
//        } else if (amount < 10 && amount >= 5) {
//            waitTime = TIME + TIME / 10 * (10 - amount);
//        }
        //0.5最小倍率，2最大倍率(Speed = (B - minB)/((maxB - minB) / (maxSpeed - minSpeed)) + minSpeed)
        //不知啥情况，0.5倍率是对的，但是2倍率会慢，所以这里适当把2倍率改大一点看起来像是2倍
        waitTime = (int) (TIME / ((amount - minSpeed) * (1.7 / (maxSpeed - minSpeed)) + 0.5));//-3
//        waitTime = TIME - (amount + 5);
    }

    public synchronized void onRelease() {
        try {
            isThreadEnd = true;
            Log.d(TAG, "onRelease ==" + mPlayer);
            if (mPlayer != null) {
                mPlayer.interrupt();
                mPlayer = null;
            }
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "===========surfaceCreated============");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "===========surfaceChanged============");
        if (mPlayer == null) {
            mPlayer = new PlayerThread(holder.getSurface());
            mPlayer.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mPlayer != null) {
            mPlayer.interrupt();
        }
    }

    /**
     * 隐藏播放器
     */
    public void hideSurfaceView() {
        if (sv.getVisibility() == View.VISIBLE) {
            sv.setVisibility(View.GONE);
        }
    }

    /**
     * 设置在影片播放到某个时间的时候回调
     *
     * @param time     这里设置多少时间回调一次
     * @param callBack 回调
     */
    public void setOnTimeCallBack(int time, OnTimeCallBack callBack) {
        this.mTime = time;
        this.callBack = callBack;
    }

    public interface OnTimeCallBack {
        /**
         * 在某个时间戳回调
         *
         * @param timePosition 返回第几段
         */
        void onTime(int timePosition);
    }

    private class PlayerThread extends Thread {

        private Surface surface;
        ByteBuffer[] inputBuffers;
        ByteBuffer[] outputBuffers;
        boolean isPalying = false;

        private Lock lock = null;
        private Condition notEmpt = null;
        private boolean blockFlag = true;

        public PlayerThread(Surface surface) {
            this.surface = surface;
            lock = new ReentrantLock();
            notEmpt = lock.newCondition();
            Log.d(TAG, "===========PlayerThread=======0=====");
        }

        public void startThread() {
            blockFlag = false;
            synchronized (notEmpt) {
                notEmpt.notify();
            }
        }

        public void pauseThread() {
            blockFlag = true;
        }

        @Override
        public void run() {
            isPalying = true;
            extractor = new MediaExtractor();
            try {
                extractor.setDataSource(SAMPLE);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    try {
                        decoder = MediaCodec.createDecoderByType(mime);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    decoder.configure(format, surface, null, 0);
                    break;
                }
            }

            if (decoder == null) {
                Log.e(TAG, "Can't find video info!");
                return;
            }

            decoder.start();

            inputBuffers = decoder.getInputBuffers();
            outputBuffers = decoder.getOutputBuffers();
            BufferInfo info = new BufferInfo();

            while (!Thread.interrupted() && !isThreadEnd) {
                try {
                    try {
                        if (isPalying) {
                            int inIndex = decoder.dequeueInputBuffer(0);
                            if (inIndex >= 0) {
                                ByteBuffer buffer = inputBuffers[inIndex];
                                int sampleSize = extractor.readSampleData(buffer, 0);
                                if (sampleSize < 0) {
                                    // We shouldn't stop the playback at this point, just pass the EOS
                                    // flag to decoder, we will get it again from the
                                    // dequeueOutputBuffer
                                    Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                                    decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                                    isPalying = false;
                                    {
                                        resetVideoPlay();
                                        continue;
                                    }
                                } else {
//                                    sendTimeMesg(extractor.getSampleTime());
                                    if (callBack != null) {
                                        if (extractor.getSampleTime() / 1000000 % mTime == 0) {
                                            callBack.onTime((int) (extractor.getSampleTime() / 1000000 / mTime));
                                        }
                                    }
                                    decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                                    /*decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);*/
                                    extractor.advance();
                                }
                            }
                        }
	
						/*if( isFirst ) {
							sv.setBackgroundResource(0);
							isFirst = false;
						}*/
                        //long starttime = System.currentTimeMillis();
                        int outIndex = decoder.dequeueOutputBuffer(info, 0);

                        switch (outIndex) {
                            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                //Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                                outputBuffers = decoder.getOutputBuffers();
                                break;
                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                //Log.d(TAG, "New format " + decoder.getOutputFormat());
                                break;
                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                //Log.d(TAG, "dequeueOutputBuffer timed out!");
                                break;
                            default:
                                ByteBuffer buffer = outputBuffers[outIndex];
                                //Log.v(TAG, "We can't use this buffer but render it due to the API limit, " + buffer);
                                sleep(waitTime);
                                decoder.releaseOutputBuffer(outIndex, true);
                                break;
                        }

                        // All decoded frames have been rendered, we can stop playing now
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                            break;
                        }
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    synchronized (notEmpt) {
                        if (blockFlag) {
                            Log.v(TAG, "the value is wait ");
                            isPalying = false;
                            notEmpt.wait();
                            isPalying = true;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                }
            }
			/*decoder.stop();
			decoder.release();
			extractor.release();*/
        }

        public void resetVideoPlay() {
            decoder.stop();
            decoder.release();
            extractor.release();

            extractor = new MediaExtractor();
            try {
                extractor.setDataSource(SAMPLE);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    try {
                        decoder = MediaCodec.createDecoderByType(mime);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    decoder.configure(format, surface, null, 0);
                    break;
                }
            }

            if (decoder == null) {
                Log.e(TAG, "Can't find video info!");
                return;
            }

            decoder.start();

            inputBuffers = decoder.getInputBuffers();
            outputBuffers = decoder.getOutputBuffers();
        }
    }

//    /*public long getVideoDuration(String path) {
//    	MediaMetadataRetriever retriever = null;
//    	long duration = 0l;
//    	try {
//			retriever = new MediaMetadataRetriever();
//		    retriever.setDataSource(path);
//		    String val = retriever.extractMetadata(
//		            MediaMetadataRetriever.METADATA_KEY_DURATION);
//		    duration = (val == null) ? duration : Long.parseLong(val);
//		} catch (RuntimeException ex) {
//		    Log.e(TAG, "MediaMetadataRetriever.setDataSource() fail:"
//		            + ex.getMessage());
//		}catch(Exception e1) {
//		        Log.e(TAG, "MediaMetadataRetriever.setDataSource() fail:"
//		                + e1.getMessage());
//		} finally {
//			if(retriever != null) {
//				retriever.release();
//			}
//		}
//    	return duration;
//    }*/

//    public void sendTimeMesg(long time) {
//        /*curTimeSample = (int)(time / 1000000 / 5);*/
//        //根据视频播放时间，获取incline数组中的位置
//        curTimeSample = getCurPos(time / 1000000);
//        if (oldTimeSample != curTimeSample) {
//            oldTimeSample = curTimeSample;
////    		((RunningVRActivity)mContext).sendMsgPro(CTConstant.MSG_VR_FRESH_TIME, curTimeSample);
//    		/*Log.d(TAG," extractor.curTimeSample() = "+ curTimeSample +
//    				" vrSceneVideoNo " + UserInfoManager.getInstance().vrSceneVideoNo);*/
//        }
//    	/*Log.d(TAG," extractor.curTimeSample() = "+ curTimeSample + " oldTimeSample " + oldTimeSample +
//				" vrSceneVideoNo " + UserInfoManager.getInstance().vrSceneVideoNo +
//				" time " + time);*/
//
//    }

//    public int getCurPos(long time) {
////    	int length = RunParamTableManager.getInstance(mContext).
////    		vrSpeedTimeTable[UserInfoManager.getInstance().vrSceneVideoNo].length;
//        int curPos = 0;
////    	for (int pos = 0; pos < length; pos++ ) {
////    		if ( time <= RunParamTableManager.getInstance(mContext).
////    	    		vrSpeedTimeTable[UserInfoManager.getInstance().vrSceneVideoNo][pos]) {
////    			curPos = pos;
////    			break;
////    		}
////
////    	}
//        return curPos;
//    }

}