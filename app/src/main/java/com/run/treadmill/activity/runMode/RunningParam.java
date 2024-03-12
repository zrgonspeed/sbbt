package com.run.treadmill.activity.runMode;

import android.os.Handler;
import android.os.Message;

import com.chuhui.btcontrol.BtHelper;
import com.chuhui.btcontrol.bean.RunParam;
import com.run.android.ShellCmdUtils;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.util.MsgWhat;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.manager.FitShowManager;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.FormulaUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.TimeStringUtil;
import com.run.treadmill.util.UnitUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/06/21
 */
public class RunningParam {

    private volatile static RunningParam instance;
    private RunParamCallback mCallback;
    private RunParamHandler mRunParamHandler;
    public boolean isQuickToSummary = false;

    /**
     * prepare时间,预防像mp4这样的app自己退出
     */
    public int countDown = 3;

    /**
     * 运动中与运动结束（暂停也算运动中）
     */
    private boolean isRunning;
    /**
     * 运动状态
     */
    public volatile int runStatus = CTConstant.RUN_STATUS_NORMAL;
    /**
     * warm up时间
     */
    private long warmUpTime = InitParam.WARM_UP_TIME;// 10;//
    /**
     * cool down时间
     */
    private long coolDownTime = InitParam.COOL_DOWN_TIME;

    /**
     * 当前速度(公制即公制，英制即英制)
     */
    private float currSpeed = InitParam.DEFAULT_SPEED;
    /**
     * 当前扬升
     */
    private float currIncline = InitParam.DEFAULT_INCLINE;
    /**
     * 已运动时间,单位:秒
     */
    public long alreadyRunTime;
    /**
     * 已运动距离
     */
    public float alreadyRunDistance;
    /**
     * 已运动卡路里
     */
    public float alreadyRunCalories;
    /**
     * 已运动MET
     */
    public float alreadyRunMet;
    /**
     * 速度累加
     */
    public float alreadyRunSpeed;
    /**
     * 扬升累加
     */
    public float alreadyRunIncline;
    /**
     * 心率累加
     */
    public int alreadyRunPulse;
    /**
     * 有心率的总时间
     */
    private int hasPulseTimeCount;

    /*********************** warm up 和 cooldown 的累加数据 *************************/
    /**
     * 已运动距离
     */
    private float alreadyWarmUpDistance;
    /**
     * 已运动卡路里
     */
    private float alreadyWarmUpCalories;
    /**
     * 已运动MET
     */
    private float alreadyWarmUpMet;
    /**
     * 已运动距离
     */
    private float alreadyCoolDownDistance;
    /**
     * 已运动卡路里
     */
    private float alreadyCoolDownCalories;
    /**
     * 已运动MET
     */
    private float alreadyCoolDownMet;

    /**
     * 目标时间 (单位:秒)
     */
    public long targetTime;
    /**
     * 目标距离
     */
    public float targetDistance;
    /**
     * 目标卡路里
     */
    public int targetCalories;
    /**
     * 目标心率
     */
    public int targetHrc;
    /**
     * 体重(随公英制变化)
     */
    public float curWeight = InitParam.DEFAULT_WEIGHT_METRIC;
    /**
     * 年龄
     */
    public int curAge = InitParam.DEFAULT_AGE;
    /**
     * 性别
     */
    public int curGender = InitParam.DEFAULT_GENDER_MALE;

    /******************* 需要显示的数据 *********************/
    private String showTime = "00:00";
    private String showDistance = "0.0";
    private String showCalories = "0";
    private String showMets = "0";
    private String showPulse = "0";
    /******************* 需要显示的数据 *********************/

    /**
     * 第几轮的 30段
     */
    public int round = 1;
    /**
     * 折线图当前段数
     */
    private int lcCurStageNum = -1;
    /**
     * 记录运动结束的当前段数（因为lcCurStageNum会置为-1）
     */
    private int runLccurStageNum = 0;
    /**
     * 速度数据数组
     */
    public float mSpeedArray[] = new float[InitParam.TOTAL_RUN_STAGE_NUM];
    /**
     * 扬升数据数组
     */
    public float mInclineArray[] = new float[InitParam.TOTAL_RUN_STAGE_NUM];
    /**
     * 当前速度下标，经过最大最小速度得到间隔为0.1的速度数组后，计算当前速度的下标，之后的速度变化都通过下标取速度值
     * 消除公英制转换导致的误差
     */
    public int currSpeedInx;
    /**
     * 保存多轮的运动数据 speed
     */
    float[] mAllSpeedList = new float[InitParam.TOTAL_RUN_STAGE_NUM];
    /**
     * 保存多轮的运动数据 incline
     */
    float[] mAllInclineList = new float[InitParam.TOTAL_RUN_STAGE_NUM];

    /**
     * 运动中采集到的速度
     */
    private ArrayList<Double> speedList;
    /**
     * 运动中采集到的的扬升
     */
    private ArrayList<Double> inclineList;
    /**
     * 运动中采集到的心率
     */
    private ArrayList<Double> pulseList;
    /**
     * 运动中记录的时间点
     */
    private ArrayList<Double> timeList;
    /**
     * 运动中被记录最大速度
     */
    private float maxSpeed = -1f;
    /**
     * 运动中被记录最小速度
     */
    private float minSpeed = -1f;
    /**
     * 运动中被记录最大速度的下标
     */
    private int maxSpeedInx = -1;
    /**
     * 运动中被记录最小速度的下标
     */
    private int minSpeedInx = -1;
    /**
     * 运动中被记录最大扬升
     */
    private float maxIncline = -1f;
    /**
     * 运动中被记录最小扬升
     */
    private float minIncline = -1f;
    /**
     * 运动中最大扬升的下标
     */
    private int maxInclineInx = -1;
    /**
     * 运动中最小扬升的下标
     */
    private int minInclineInx = -1;
    /**
     * 运动中被记录最大心率
     */
    private int maxPulse = -1;
    /**
     * 运动中被记录最小心率
     */
    private int minPulse = -1;
    /**
     * 运动中被记录最大心率的下标
     */
    private int maxPulseInx = -1;
    /**
     * 运动中被记录最小心率的下标
     */
    private int minPulseInx = -1;

    /**
     * (hrc)无心率 次数
     */
    public int noPulseCount;
    /**
     * (hrc)心率超过 次数
     */
    public int overPulseCount;
    /**
     * (hrc)速度根据心率变化累计 次数(小于  5)
     */
    public int lessSpeedChangeCount;
    /**
     * (hrc)速度根据心率变化累计 次数(大于  5)
     */
    public int moreSpeedChangeCount;

    /**
     * 当前AD
     */
    private int currAD;

    private boolean isMetric;
    public String mediaPkgName = "";
    private boolean reFlashDate = false;
    public long waiteTime = 993;
    public int waiteNanosTime = 14000;
    private Thread timeThread, dateThread;

    /**
     * 防止在悬浮窗秒按安全key，卡顿造成回到运动界面没有报错误，而停留在运动界面
     */
//    public int errorCode;
    private RunningParam() {
        mRunParamHandler = new RunParamHandler(this);

        speedList = new ArrayList<>();
        inclineList = new ArrayList<>();
        pulseList = new ArrayList<>();
        timeList = new ArrayList<>();

        isMetric = SpManager.getIsMetric();
        if (!isMetric) {
            curWeight = InitParam.DEFAULT_WEIGHT_IMPERIAL;
        }
    }

    public static RunningParam getInstance() {
        if (instance == null) {
            synchronized (RunningParam.class) {
                if (instance == null) {
                    ControlManager.getInstance().reset();
                    instance = new RunningParam();
                }
            }
        }
        return instance;
    }

    /**
     * 重置 RunningParam(目前在运动设置参数界面重置)
     */
    public static void reset() {
        if (instance != null) {
            synchronized (RunningParam.class) {
                if (instance != null) {
                    instance = null;
                }
            }
        }
    }

    /**
     * 开始刷新数据
     */
    public synchronized void startRefreshData() {
        if (isRunning) {
            return;
        }
        showTime = TimeStringUtil.getMsToMinSecValue(targetTime * 1000f);
        showCalories = String.valueOf(UnitUtil.getFloatToInt(targetCalories));
        showDistance = String.valueOf(UnitUtil.getFloatToInt(targetDistance));
        if ("0".equals(showDistance)) {
            showDistance = "0.0";
        }
        if (runStatus == CTConstant.RUN_STATUS_WARM_UP) {
            showTime = TimeStringUtil.getMsToMinSecValue(warmUpTime * 1000f);
        }
        isRunning = true;
        timeThread = new RefreshRunningDataTask();
        dateThread = new RefreshRunningDataTask2();
        timeThread.start();
        dateThread.start();
    }

    /**
     * 继续刷新数据
     */
    public synchronized void notifyRefreshData() {
        ControlManager.getInstance().reset();
        notifyAll();
    }

    /**
     * 运动结束时调用
     */
    public synchronized void end() {
        if (isRunning) {
            isRunning = false;
        }
        this.mCallback = null;
        FitShowManager.getInstance().clean();
        interrupted();

        BtHelper.isOnRunning = false;
        BtHelper.getInstance().stopSport();
        BtHelper.getInstance().getRunParamBuilder()
                .runStatus(RunParam.RUN_STOP_STATUS);
    }

    /**
     * 是否结束运动
     *
     * @return
     */
    public boolean isRunningEnd() {
        return !isRunning;
    }

    public synchronized void interrupted() {
        try {
            if (timeThread != null) {
                timeThread.interrupt();
                timeThread = null;
            }
            if (dateThread != null) {
                dateThread.interrupt();
                dateThread = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void warmUpToRunning() {
        showTime = TimeStringUtil.getMsToMinSecValue(targetTime * 1000f);
        showDistance = "0.0";
        showCalories = "0";
        showMets = "0";

        //打断线程再跑起来
        interrupted();
        timeThread = new RefreshRunningDataTask();
        timeThread.start();
        dateThread = new RefreshRunningDataTask2();
        dateThread.start();

        setLcCurStageNum(0);
        runStatus = CTConstant.RUN_STATUS_RUNNING;
        mRunParamHandler.sendEmptyMessage(MsgWhat.MSG_REFRESH_DATA);
    }

    public void runningToCoolDown() {
        showTime = TimeStringUtil.getMsToMinSecValue(coolDownTime * 1000f);
        showDistance = "0.0";
        showCalories = "0";
        showMets = "0";

        runStatus = CTConstant.RUN_STATUS_COOL_DOWN;
        lcCurStageNum = -1;
        mRunParamHandler.sendEmptyMessage(MsgWhat.MSG_REFRESH_DATA);

        if (UnitUtil.getFloatBy1f(currSpeed * 0.8f) >= SpManager.getMinSpeed(isMetric)) {
            setCurrSpeed(UnitUtil.getFloatBy1f(currSpeed * 0.8f));
        } else if (currSpeed != SpManager.getMinSpeed(isMetric)) {
            setCurrSpeed(SpManager.getMinSpeed(isMetric));
        }
        mRunParamHandler.sendEmptyMessage(MsgWhat.MSG_COOL_DOWN_10);
    }

    public void setCallback(RunParamCallback callback) {
        if (callback != null) {
            this.mCallback = callback;
        }
    }

    /**
     * 获取平均 速度
     *
     * @return
     */
    public float getAvgSpeed() {
        if (alreadyRunTime == 0) {
            return 0f;
        }
        return UnitUtil.getFloatBy1f(alreadyRunSpeed / alreadyRunTime);
//        return UnitUtil.getFloatBy1f(alreadyRunDistance / (alreadyRunTime / 3600f));
    }

    /**
     * 获取平均 mets
     *
     * @return
     */
    public float getAvgMets() {
        if (alreadyRunTime == 0) {
            return 0f;
        }
        return UnitUtil.getFloatBy1f(alreadyRunMet / alreadyRunTime);
    }

    /**
     * 获取平均 incline
     *
     * @return
     */
    public float getAvgIncline() {
        if (alreadyRunTime == 0) {
            return 0f;
        }
        return UnitUtil.getFloatBy1f(alreadyRunIncline / alreadyRunTime);
    }

    /**
     * 获取平均 pulse
     *
     * @return
     */
    public float getAvgPulse() {
        if (hasPulseTimeCount == 0) {
            return 0f;
        }
        return UnitUtil.getFloatBy1f((float) alreadyRunPulse / hasPulseTimeCount);
    }

    /**
     * 保存已运行的历史扬升速度数据
     */
    public void saveHasRunData() {
        float[] tmpSpeedArray = new float[InitParam.TOTAL_RUN_STAGE_NUM * round];
        float[] tmpInclineArray = new float[InitParam.TOTAL_RUN_STAGE_NUM * round];

        System.arraycopy(mAllSpeedList, 0, tmpSpeedArray, 0, mAllSpeedList.length);
        System.arraycopy(mAllInclineList, 0, tmpInclineArray, 0, mAllInclineList.length);

        System.arraycopy(mSpeedArray, 0, tmpSpeedArray, InitParam.TOTAL_RUN_STAGE_NUM * (round - 1),
                InitParam.TOTAL_RUN_STAGE_NUM);
        System.arraycopy(mInclineArray, 0, tmpInclineArray, InitParam.TOTAL_RUN_STAGE_NUM * (round - 1),
                InitParam.TOTAL_RUN_STAGE_NUM);

        mAllSpeedList = tmpSpeedArray;
        mAllInclineList = tmpInclineArray;
    }

    static class RunParamHandler extends Handler {
        private WeakReference<RunningParam> weakReference;
        private RunningParam mRunningParam;

        RunParamHandler(RunningParam mRunningParam) {
            if (mRunningParam != null) {
                weakReference = new WeakReference<>(mRunningParam);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            mRunningParam = weakReference.get();
            if (mRunningParam == null) {
                return;
            }
            if (msg.what == MsgWhat.MSG_REFRESH_DATA) {
                if (mRunningParam.isRunning && mRunningParam.mCallback != null) {
                    mRunningParam.mCallback.dataCallback();
                }
            } else if (msg.what == MsgWhat.MSG_COOL_DOWN_10) {
                if (mRunningParam.isRunning && mRunningParam.mCallback != null) {
                    mRunningParam.mCallback.cooldown10Callback();
                }
            }
        }
    }

    /**
     * 这里处理数据是为了运动界面和媒体的数据统一处理
     */
    private final class RefreshRunningDataTask extends Thread {

        @Override
        public void run() {
            while (isRunning) {
                try {
                    BtHelper.isOnRunning = true;

                    // Logger.i("runStatus == " + runStatus);
                    if (runStatus == CTConstant.RUN_STATUS_STOP) {
                        synchronized (instance) {
                            BtHelper.getInstance().getRunParamBuilder()
                                    .speed(0.0f)
                                    .runStatus(RunParam.RUN_PAUSE_STATUS);
                            instance.wait();
                        }
                        continue;
                    }
                    Thread.sleep(waiteTime, waiteNanosTime);
                    if (runStatus == CTConstant.RUN_STATUS_WARM_UP) {

                        float curRunDistance = FormulaUtil.getRunDistances(currSpeed, (1 / 60.0f / 60.0f));
                        pre_recode++;
                        pre_recode_time++;
                        pre_recode_dis += curRunDistance;

                        warmUpTime--;
                        showTime = TimeStringUtil.getMsToMinSecValue(warmUpTime * 1000f);

                        alreadyWarmUpDistance += UnitUtil.getFloatBy4f(FormulaUtil.getRunDistances(currSpeed, (1 / 60.0f / 60.0f)));
                        showDistance = String.valueOf(UnitUtil.getFloatBy1f(alreadyWarmUpDistance));

                        alreadyWarmUpCalories += FormulaUtil.getRunCalories(curWeight, currSpeed, currIncline, isMetric);
                        showCalories = String.valueOf(UnitUtil.getFloatToInt(alreadyWarmUpCalories));

                        alreadyWarmUpMet += FormulaUtil.getMETs(currSpeed, currIncline, isMetric);
                        showMets = String.valueOf(UnitUtil.getFloatBy1f(FormulaUtil.getMETs(currSpeed, currIncline, isMetric)));
                    } else if (runStatus == CTConstant.RUN_STATUS_COOL_DOWN) {
                        float curRunDistance = FormulaUtil.getRunDistances(currSpeed, (1 / 60.0f / 60.0f));
                        pre_recode_time++;
                        pre_recode_dis += curRunDistance;
                        pre_recode++;

                        coolDownTime--;
                        if ((coolDownTime % 10) == 0) {
                            if (UnitUtil.getFloatBy1f(currSpeed * 0.8f) >= SpManager.getMinSpeed(isMetric)) {
                                setCurrSpeed(UnitUtil.getFloatBy1f(currSpeed * 0.8f));
                            } else if (currSpeed != SpManager.getMinSpeed(isMetric)) {
                                setCurrSpeed(SpManager.getMinSpeed(isMetric));
                            }
                            mRunParamHandler.sendEmptyMessage(MsgWhat.MSG_COOL_DOWN_10);
                        }
                        showTime = TimeStringUtil.getMsToMinSecValue(coolDownTime * 1000f);

                        alreadyCoolDownDistance += UnitUtil.getFloatBy4f(FormulaUtil.getRunDistances(currSpeed, (1 / 60.0f / 60.0f)));
                        showDistance = String.valueOf(UnitUtil.getFloatBy1f(alreadyCoolDownDistance));

                        alreadyCoolDownCalories += FormulaUtil.getRunCalories(curWeight, currSpeed, currIncline, isMetric);
                        showCalories = String.valueOf(UnitUtil.getFloatToInt(alreadyCoolDownCalories));

                        alreadyCoolDownMet += FormulaUtil.getMETs(currSpeed, currIncline, isMetric);
                        showMets = String.valueOf(UnitUtil.getFloatBy1f(FormulaUtil.getMETs(currSpeed, currIncline, isMetric)));
                    } else if (runStatus == CTConstant.RUN_STATUS_RUNNING) {
                        float curRunDistance = FormulaUtil.getRunDistances(currSpeed, (1 / 60.0f / 60.0f));
                        pre_recode_time++;
                        pre_recode_dis += curRunDistance;
                        pre_recode++;

                        recordRunData(-1);
                        alreadyRunTime++;
                        showTime = TimeStringUtil.getMsToMinSecValueHasUp(((targetTime >= alreadyRunTime) ? (targetTime - alreadyRunTime) : alreadyRunTime) * 1000f);

                        alreadyRunDistance += FormulaUtil.getRunDistances(currSpeed, (1 / 60.0f / 60.0f));
                        showDistance = String.valueOf(UnitUtil.getFloatClearToPoint(((targetDistance >= alreadyRunDistance) ? (targetDistance - alreadyRunDistance) : alreadyRunDistance) % 100f, 1));

                        alreadyRunCalories += FormulaUtil.getRunCalories(curWeight, currSpeed, currIncline, isMetric);
                        showCalories = String.valueOf(UnitUtil.getFloatToInt((targetCalories >= alreadyRunCalories) ? (targetCalories - alreadyRunCalories) : alreadyRunCalories) % 10000);

                        alreadyRunMet += UnitUtil.getFloatBy1f(FormulaUtil.getMETs(currSpeed, currIncline, isMetric));
                        showMets = String.valueOf(UnitUtil.getFloatBy1f(FormulaUtil.getMETs(currSpeed, currIncline, isMetric)));

                        alreadyRunSpeed = UnitUtil.getFloatBy1f(alreadyRunSpeed + currSpeed);
                        alreadyRunIncline = UnitUtil.getFloatBy1f(alreadyRunIncline + (ErrorManager.getInstance().isHasInclineError() ? 0 : currIncline));
                        alreadyRunPulse += Integer.valueOf(showPulse);
                        if (Integer.valueOf(showPulse) > 0) {
                            hasPulseTimeCount++;
                        }
                    }
                    mRunParamHandler.sendEmptyMessage(MsgWhat.MSG_REFRESH_DATA);

                    // stepManager.refreshSecond();

                    // Logger.d("运动中 pre_recode == " + pre_recode);
                    // Logger.d("getRunTotalDis == " + UnitUtil.getFloatToInt(SpManager.getRunTotalDis()));
                    // Logger.d("getRunTotalTime == " + TimeStringUtil.getSecToHrMin(SpManager.getRunTotalTime()));
                    if (pre_recode % 120 == 0) {
                        Logger.i("达到2分钟，准备保存数据");
                        reFlashDate = true;
                        pre_recode = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    private int pre_recode = 0;
    private float pre_recode_dis = 0f;
    private int pre_recode_time = 0;

    /**
     * 记录和计算运动数据,防止不足时间，采取前120s每一秒都记录
     * 超过120s每5s记录一次
     * 超过30分钟每10秒记录一次
     * 超过2小时每30秒记录一次
     *
     * @param type 当前调整的类型 0：速度；1：扬升；-1：同时
     */
    private void recordRunData(int type) {
        if (type == 0) {
            speedList.add((double) currSpeed);
            inclineList.add(ErrorManager.getInstance().isHasInclineError() ? 0 : (double) mInclineArray[runLccurStageNum]);
            pulseList.add(Double.valueOf(showPulse));
            timeList.add((double) alreadyRunTime);
            return;
        }
        if (type == 1) {
            speedList.add((double) mSpeedArray[runLccurStageNum]);
            inclineList.add(ErrorManager.getInstance().isHasInclineError() ? 0 : (double) currIncline);
            pulseList.add(Double.valueOf(showPulse));
            timeList.add((double) alreadyRunTime);
            return;
        }
        if (alreadyRunTime <= 120) {
            saveDate((double) mSpeedArray[runLccurStageNum],
                    ErrorManager.getInstance().isHasInclineError() ? 0 : (double) mInclineArray[runLccurStageNum],
                    Double.valueOf(showPulse), (double) alreadyRunTime);
        } else if (alreadyRunTime <= (30 * 60) && (alreadyRunTime % 5) == 0) {
            saveDate((double) mSpeedArray[runLccurStageNum],
                    ErrorManager.getInstance().isHasInclineError() ? 0 : (double) mInclineArray[runLccurStageNum],
                    Double.valueOf(showPulse), (double) alreadyRunTime);
        } else if (alreadyRunTime <= (2 * 60 * 60) && (alreadyRunTime % 10) == 0) {
            saveDate((double) mSpeedArray[runLccurStageNum],
                    ErrorManager.getInstance().isHasInclineError() ? 0 : (double) mInclineArray[runLccurStageNum],
                    Double.valueOf(showPulse), (double) alreadyRunTime);
        } else if (alreadyRunTime > (2 * 60 * 60) && (alreadyRunTime % 30) == 0) {
            saveDate((double) mSpeedArray[runLccurStageNum],
                    ErrorManager.getInstance().isHasInclineError() ? 0 : (double) mInclineArray[runLccurStageNum],
                    Double.valueOf(showPulse), (double) alreadyRunTime);
        }
    }

    private void saveDate(double speed, double incline, double pulse, double time) {
        speedList.add(speed);
        inclineList.add(incline);
        pulseList.add(pulse);
        timeList.add(time);
    }

    private final class RefreshRunningDataTask2 extends Thread {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    if (runStatus == CTConstant.RUN_STATUS_STOP) {
                        synchronized (instance) {
                            BtHelper.getInstance().getRunParamBuilder()
                                    .speed(0f)
                                    .runStatus(RunParam.RUN_PAUSE_STATUS);
                            instance.wait();
                        }
                        continue;
                    }
                    Thread.sleep(200);
                    if (reFlashDate) {
                        recodePreRunData();
                        reFlashDate = false;
                    }
                    if (runStatus == CTConstant.RUN_STATUS_STOP || runStatus == CTConstant.RUN_STATUS_CONTINUE) {
                        continue;
                    }
                    ControlManager.getInstance().setSpeed(currSpeed);
                    if (!ErrorManager.getInstance().isHasInclineError()) {
                        ControlManager.getInstance().setIncline(currIncline);
                    }
                    if (FitShowManager.getInstance().isConnect()) {
                        FitShowManager.getInstance().buildFsTreadmillParam(
                                alreadyRunTime,
                                getCurPulse(),
                                currSpeed,
                                currIncline,
                                stepManager.getStepToFitShow(),
                                alreadyRunDistance,
                                alreadyRunCalories,
                                runLccurStageNum,
                                runStatus);
                    }

                    setZyBtAndCsafeData();

                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            if (reFlashDate) {
                SpManager.setRunLubeDis(FormulaUtil.getRunDistances(currSpeed, (1 / 60.0f / 60.0f)));
                SpManager.setRunDis(FormulaUtil.getRunDistances(currSpeed, (1 / 60.0f / 60.0f)));
                SpManager.setRunTime(1L);
            }
        }
    }

    /**
     * 每调用一次 将记录一次数据
     */
    public void recodePreRunData() {
        // Logger.i("recodePreRunData()");
        if (pre_recode_dis > 0f &&
                pre_recode_time > 0) {
            SpManager.setRunData(pre_recode_dis, pre_recode_time);
            ShellCmdUtils.getInstance().execCommand("sync");

            Logger.i("保存数据()");
        }
        pre_recode_dis = 0;
        pre_recode_time = 0;
    }

    public float getCurrSpeed() {
        return currSpeed;
    }

    public void setCurrSpeed(float speed) {
        if (speed != this.currSpeed) {
            this.currSpeed = speed;
        }
        if (lcCurStageNum < 0) {
            return;
        }
        if (maxSpeed == -1 || minSpeed == -1) {
            maxSpeed = speed;
            minSpeed = speed;
            maxSpeedInx = speedList.size();
            minSpeedInx = speedList.size();
            recordRunData(0);
            return;
        }

        if (speed >= maxSpeed) {
            maxSpeed = speed;
            maxSpeedInx = speedList.size();
            recordRunData(0);
        } else if (speed <= minSpeed) {
            minSpeed = speed;
            minSpeedInx = speedList.size();
            recordRunData(0);
        }
    }

    public float getCurrIncline() {
        return currIncline;
    }

    public void setCurrIncline(float incline) {

        if (this.currIncline != incline) {
            this.currIncline = incline;
        }

        if (lcCurStageNum < 0) {
            return;
        }
        if (maxIncline == -1 || minIncline == -1) {
            maxIncline = incline;
            maxInclineInx = inclineList.size();
            minIncline = incline;
            minInclineInx = inclineList.size();
            recordRunData(1);
            return;
        }
        if (incline >= maxIncline) {
            maxIncline = incline;
            maxInclineInx = inclineList.size();
            recordRunData(1);
        } else if (incline <= minIncline) {
            minIncline = incline;
            minInclineInx = inclineList.size();
            recordRunData(1);
        }
    }

    public void setCurrPulse(int pulse) {
        if (runStatus == CTConstant.RUN_STATUS_NORMAL) {
            // 进入悬浮窗，没开始运动也要能显示心率
            if (!this.showPulse.equals(String.valueOf(pulse))) {
                showPulse = String.valueOf(pulse);
            }
            return;
        }
        if (!this.showPulse.equals(String.valueOf(pulse))) {
            showPulse = String.valueOf(pulse);
        }
        if (lcCurStageNum < 0) {
            return;
        }
        if (maxPulse == -1 || minPulse == -1) {
            maxPulse = pulse;
            maxPulseInx = pulseList.size();
            minPulse = pulse;
            minPulseInx = pulseList.size();
            recordRunData(-1);
            return;
        }
        if (pulse >= maxPulse) {
            maxPulse = pulse;
            maxPulseInx = pulseList.size();
            recordRunData(-1);
        } else if (pulse <= minPulse) {
            minPulse = pulse;
            minPulseInx = pulseList.size();
            recordRunData(-1);
        }
    }

    /**
     * 扬升出错的时候记录
     */
    public void setInclineError() {
        // ControlManager.getInstance().stopIncline();
        minIncline = 0;
        minInclineInx = inclineList.size();
        recordRunData(1);
    }

    public void setLcCurStageNum(int curStageNum) {
        if (lcCurStageNum != curStageNum) {
            lcCurStageNum = curStageNum;
            if (runStatus == CTConstant.RUN_STATUS_RUNNING) {
                runLccurStageNum = curStageNum;
            }
            setCurrSpeed(mSpeedArray[lcCurStageNum]);
            if (!ErrorManager.getInstance().isHasInclineError()) {
                setCurrIncline(mInclineArray[this.lcCurStageNum]);
            } else if (this.currIncline != mInclineArray[this.lcCurStageNum]) {
                minIncline = 0;
                minInclineInx = inclineList.size();
                recordRunData(1);
                this.currIncline = mInclineArray[this.lcCurStageNum];
            }

            if (mCallback != null) {
                mCallback.onCurStageNumChange();
            }
        }
    }

    public int getLcCurStageNum() {
        return lcCurStageNum;
    }

    public int getRunLccurStageNum() {
        return runLccurStageNum;
    }

    public String getShowTime() {
        return showTime;
    }

    public long getWarmUpTime() {
        return warmUpTime;
    }

    public long getCoolDownTime() {
        return coolDownTime;
    }

    public String getShowDistance() {
        return showDistance;
    }

    public String getShowCalories() {
        return showCalories;
    }

    public String getShowMets() {
        return showMets;
    }

    public String getShowPulse() {
        return showPulse;
    }

    public int getCurPulse() {
        return Integer.valueOf(showPulse);
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getMinSpeed() {
        return minSpeed;
    }

    public float getMaxIncline() {
        return maxIncline < 0 ? 0 : maxIncline;
    }

    public float getMinIncline() {
        return minIncline < 0 ? 0 : minIncline;
    }

    public int getMaxPulse() {
        if (maxPulse == -1) {
            return 0;
        }
        return maxPulse;
    }

    public int getMinPulse() {
        if (minPulse == -1) {
            return 0;
        }
        return minPulse;
    }

    public int getCurrAD() {
        return currAD;
    }

    public void setCurrAD(int currAD) {
        this.currAD = currAD;
    }

    public ArrayList<Double> getSpeedList() {
        return speedList;
    }

    public ArrayList<Double> getInclineList() {
        return inclineList;
    }

    public ArrayList<Double> getPulseList() {
        return pulseList;
    }

    public ArrayList<Double> getTimeList() {
        return timeList;
    }

    public int getMaxSpeedInx() {
        return maxSpeedInx;
    }

    public int getMinSpeedInx() {
        return minSpeedInx;
    }

    public int getMaxInclineInx() {
        return maxInclineInx;
    }

    public int getMinInclineInx() {
        return minInclineInx;
    }

    public int getMaxPulseInx() {
        return maxPulseInx;
    }

    public int getMinPulseInx() {
        return minPulseInx;
    }

    private void setZyBtAndCsafeData() {
        if (BtHelper.getInstance().connected()) {
            if (runStatus == CTConstant.RUN_STATUS_WARM_UP) {
                BtHelper.getInstance().getRunParamBuilder().remainingTime(warmUpTime);
            } else if (runStatus == CTConstant.RUN_STATUS_COOL_DOWN) {
                BtHelper.getInstance().getRunParamBuilder().remainingTime(coolDownTime);
            } else {
                BtHelper.getInstance().getRunParamBuilder().remainingTime(0);
            }
            if (targetTime > 0) {
                if (runStatus == CTConstant.RUN_STATUS_RUNNING) {
                    BtHelper.getInstance().getRunParamBuilder().remainingTime(targetTime - alreadyRunTime);
                }
            }
            BtHelper.getInstance().getRunParamBuilder()
                    .time(getAllTime())
                    .speed(isMetric ? currSpeed : UnitUtil.getMileToKm(currSpeed))
                    .incline(currIncline)
                    .distance(isMetric ? getAllDistance() : UnitUtil.getMileToKm(getAllDistance()))
                    .hr(getCurPulse())
                    .kCal(UnitUtil.getFloatToInt(getAllCalories()))
                    .runStatus(RunParam.RUN_START_STATUS)
                    .runStage(RunParam.RUN_GENERAL_STAGE)
                    .atRest(false);
        }
    }

    /**
     * 获取总时间
     *
     * @return
     */
    public long getAllTime() {
        return ((InitParam.WARM_UP_TIME - warmUpTime) + alreadyRunTime + (InitParam.COOL_DOWN_TIME - coolDownTime));
    }

    /**
     * 获取总距离
     *
     * @return
     */
    public float getAllDistance() {
        return UnitUtil.addFloat(UnitUtil.addFloat(alreadyWarmUpDistance, alreadyCoolDownDistance), alreadyRunDistance);
    }

    /**
     * 获取总卡路里
     *
     * @return
     */
    public int getAllCalories() {
        return Math.round(UnitUtil.addFloat(UnitUtil.addFloat(alreadyWarmUpCalories, alreadyCoolDownCalories), alreadyRunCalories)) % 9999;
    }

    public StepManager stepManager = new StepManager(this);

    public void setStepNumber(int stepNumber) {
        // Logger.d("stepNumber == " + stepNumber);
       stepManager.setStepFromMCU(stepNumber);
    }

    public void cleanStep() {
        stepManager.clean();
    }

    public boolean isFloat = false;

}