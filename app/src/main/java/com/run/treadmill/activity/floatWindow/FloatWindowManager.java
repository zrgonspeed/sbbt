package com.run.treadmill.activity.floatWindow;import android.bluetooth.BluetoothAdapter;import android.bluetooth.le.BluetoothLeScanner;import android.bluetooth.le.ScanCallback;import android.bluetooth.le.ScanResult;import android.content.Context;import android.os.Handler;import android.os.Looper;import android.os.Message;import android.text.SpannableString;import android.view.View;import android.view.WindowManager;import androidx.annotation.NonNull;import com.chuhui.btcontrol.BtCallBack;import com.chuhui.btcontrol.BtHelper;import com.chuhui.btcontrol.CbData;import com.run.android.ShellCmdUtils;import com.run.serial.RxDataCallBack;import com.run.serial.SerialCommand;import com.run.serial.SerialUtils;import com.run.treadmill.Custom;import com.run.treadmill.R;import com.run.treadmill.activity.EmptyMessageTask;import com.run.treadmill.activity.floatWindow.runBottom.QuickStartBottomFloat;import com.run.treadmill.activity.floatWindow.runTop.QuickStartTopFloat;import com.run.treadmill.activity.runMode.BaseRunActivity;import com.run.treadmill.activity.runMode.RunParamCallback;import com.run.treadmill.activity.runMode.RunningParam;import com.run.treadmill.common.CTConstant;import com.run.treadmill.manager.BuzzerManager;import com.run.treadmill.manager.ControlManager;import com.run.treadmill.manager.ErrorManager;import com.run.treadmill.manager.SystemSoundManager;import com.run.treadmill.manager.control.NormalParam;import com.run.treadmill.manager.control.ParamCons;import com.run.treadmill.serial.SerialKeyValue;import com.run.treadmill.sp.SpManager;import com.run.treadmill.util.DataTypeConversion;import com.run.treadmill.util.KeepAliveUtil;import com.run.treadmill.util.Logger;import com.run.treadmill.util.MsgWhat;import com.run.treadmill.util.StringUtil;import com.run.treadmill.util.ThirdApkSupport;import com.run.treadmill.util.ThreadUtils;import com.run.treadmill.util.VolumeUtils;import java.lang.ref.WeakReference;import java.util.Timer;/** * @Description 这里用一句话描述 * @Author GaleLiu * @Time 2019/06/14 */public class FloatWindowManager implements RxDataCallBack, RunParamCallback, BtCallBack {    private final String TAG = "FloatWindowManager";    private BaseRunActivity mActivity;    private WindowManager mWindowManager;    private BaseRunBottomFloat baseRunBottomFloat;    private BaseRunTopFloat baseRunTopFloat;    private MediaDotFloatWindow mMediaDotFloatWindow;    private BackDotFloatWindow backDotFloatWindow;    private VoiceFloatWindow mVoiceFloatWindow;    private CalculatorFloatWindow mCalculatorFloatWindow;    private PauseFloatWindow pauseFw;    public void showPauseFw() {        pauseFw.show();    }    public void clickPauseQuit() {        pauseFw.clickQuit();    }    public void stopPauseTimer() {        baseRunBottomFloat.stopPauseTimer();    }    private MyFloatHandler myFloatHandler;    private Message msg;    private EmptyMessageTask mCountdownTask;    private Timer mTimer;    /**     * 是否显示悬浮窗     */    private boolean isShow = true;    /**     * 公英制     */    public boolean isMetric;    /**     * 记录当前音量，321go需要固定音量，做复位音量用     */    public int currentPro;    private String pkgName = "com.run.treadmill";    private String[] className = {            pkgName + ".activity.runMode.quickStart.QuickStartActivity",            pkgName + ".activity.runMode.goal.GoalActivity",            pkgName + ".activity.runMode.hill.HillActivity",            pkgName + ".activity.runMode.userProgram.UserProgramActivity",            pkgName + ".activity.runMode.hrc.HrcActivity",            pkgName + ".activity.runMode.fitness.FitnessTestActivity",            pkgName + ".activity.runMode.vision.VisionActivity",            pkgName + ".activity.runMode.interval.IntervalActivity",            pkgName + ".activity.runMode.program.ProgramActivity",    };    private String currClassName;    public RunningParam mRunningParam;    /**     * 运动数据的单位字体大小     */    int runParamUnitTextSize;    private boolean isKinomap = false;    final BluetoothLeScanner bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();    private final ScanCallback mScanCallback = new ScanCallback() {// 扫描Callback        @Override        public void onScanResult(int callbackType, ScanResult result) {            Logger.d(TAG, "scan time = " + result.getTimestampNanos() +                    " >> device name >> " + result.getDevice().getName() + " getRssi " + result.getRssi()                    + " callbackType " + callbackType);        }    };    protected int runMode;    public FloatWindowManager(@NonNull BaseRunActivity activity) {        this.mActivity = activity;        isMetric = activity.isMetric;        runParamUnitTextSize = mActivity.getResources().getDimensionPixelSize(R.dimen.font_size_run_param_unit);        mWindowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);        currentPro = SystemSoundManager.getInstance().getCurrentPro();    }    private void startFloatWindow(@CTConstant.RunMode int runMode) {        this.runMode = runMode;        RunningParam.getInstance().setCallback(this);        mRunningParam = RunningParam.getInstance();        myFloatHandler = new MyFloatHandler(Looper.getMainLooper(), this);        regRxDataCallBackAgain();        switch (runMode) {            case CTConstant.QUICKSTART:                baseRunBottomFloat = new QuickStartBottomFloat(mActivity, mWindowManager);                baseRunTopFloat = new QuickStartTopFloat(mActivity, mWindowManager);                currClassName = className[0];                break;            case CTConstant.GOAL:                currClassName = className[1];                break;            case CTConstant.HILL:                currClassName = className[2];                break;            case CTConstant.USER_PROGRAM:                currClassName = className[3];                break;            case CTConstant.HRC:                currClassName = className[4];                break;            case CTConstant.FITNESS_TEST:                currClassName = className[5];                break;            case CTConstant.VISION:                currClassName = className[6];                break;            case CTConstant.INTERVAL:                currClassName = className[7];                break;            case CTConstant.PROGRAM:                currClassName = className[8];                break;            default:                break;        }        mVoiceFloatWindow = new VoiceFloatWindow(mActivity, mWindowManager);        pauseFw = new PauseFloatWindow(mActivity, mWindowManager);        mCalculatorFloatWindow = new CalculatorFloatWindow(mActivity, mWindowManager);        mMediaDotFloatWindow = new MediaDotFloatWindow(mActivity, mWindowManager);        backDotFloatWindow = new BackDotFloatWindow(mActivity, mWindowManager);        baseRunBottomFloat.startFloat(this);        baseRunTopFloat.startFloat(this);        mMediaDotFloatWindow.startFloat(this);        backDotFloatWindow.startFloat(this);        backDotFloatWindow.hide();        if (isQuickToMedia()) {            backDotFloatWindow.setRemove(false);            backDotFloatWindow.show();        }        mVoiceFloatWindow.startFloat(this);        pauseFw.startFloat(this);        mCalculatorFloatWindow.startFloat(this);        mCalculatorFloatWindow.setCalculatorCallBack(baseRunBottomFloat);        BtHelper.getInstance().setCallback(this);        //开启一个 不让后台kill 自己的命令线程        KeepAliveUtil.runCmdThread(mActivity);    }    /**     * 控制悬浮窗的显示和隐藏     */    void showOrHideFloatWindow() {        if (baseRunTopFloat != null) {            baseRunTopFloat.showOrHideFloatWindow(isShow);        }        if (baseRunBottomFloat != null) {            baseRunBottomFloat.showOrHideFloatWindow(isShow);        }        if (mCalculatorFloatWindow != null) {            mCalculatorFloatWindow.showOrHideFloatWindow(true);        }        if (mVoiceFloatWindow != null) {            mVoiceFloatWindow.showOrHideFloatWindow(true);        }        if (backDotFloatWindow != null) {            backDotFloatWindow.showOrHideFloatWindow(isShow);        }        isShow = !isShow;    }    private void stopCtrlFloatWindow() {        if (baseRunBottomFloat != null) {            baseRunBottomFloat.stopFloat();            baseRunBottomFloat = null;        }    }    private void stopParamFloatWindow() {        if (baseRunTopFloat != null) {            baseRunTopFloat.stopFloat();            baseRunTopFloat = null;        }    }    private void stopDotFloatWindow() {        if (mMediaDotFloatWindow != null) {            mMediaDotFloatWindow.stopFloat();            mMediaDotFloatWindow = null;        }    }    private void stopBackDotFloatWindow() {        if (backDotFloatWindow != null) {            backDotFloatWindow.stopFloat();            backDotFloatWindow = null;        }    }    private void stopVoiceFloatWindow() {        if (mVoiceFloatWindow != null) {            mVoiceFloatWindow.stopFloat();            mVoiceFloatWindow = null;        }    }    private void stopPauseFloatWindow() {        if (pauseFw != null) {            pauseFw.stopFloat();            pauseFw = null;        }    }    public void stopCalculatorFloatWindow() {        if (mCalculatorFloatWindow != null) {            mCalculatorFloatWindow.stopFloat();            mCalculatorFloatWindow = null;        }    }    public void startCalculatorFloatWindow(int floatPoint, int editType, int stringId) {        if (mCalculatorFloatWindow != null) {            mCalculatorFloatWindow.reset();            mCalculatorFloatWindow.setFloatPoint(floatPoint);            mCalculatorFloatWindow.setEditType(editType);            mCalculatorFloatWindow.setEditTypeName(stringId);            mCalculatorFloatWindow.showOrHideFloatWindow();        }    }    void hideFloatWindow() {        if (mCalculatorFloatWindow != null) {            mCalculatorFloatWindow.showOrHideFloatWindow(true);        }    }    public void hideCalcFloatWindowByInclineError() {        if (mCalculatorFloatWindow != null) {            if (mCalculatorFloatWindow.getEditType() == CTConstant.TYPE_INCLINE) {                mCalculatorFloatWindow.showOrHideFloatWindow(true);            }        }    }    public boolean isShowingCalculator() {        return (mCalculatorFloatWindow != null && mCalculatorFloatWindow.isShowing());    }    public void showOrHideVoiceFloatWindow() {        if (mVoiceFloatWindow != null) {            mVoiceFloatWindow.showOrHideFloatWindow();        }    }    /**     * 控制音量     *     * @param isShow     */    public void showOrHideVoiceFloatWindow(boolean isShow) {        if (mVoiceFloatWindow != null) {            mVoiceFloatWindow.showOrHideFloatWindow(isShow);            baseRunTopFloat.setVoiceEnable(!isShow);        }    }    public void goBackMyApp() {        ThirdApkSupport.doStartApplicationWithPackageName(mActivity, pkgName, currClassName);        stopFloatWindow();        Logger.e("goBackMyApp()");        // killThirdApk();    }    public void fitShowStopRunning() {        stopFloatWindow();        killThirdApk();        ThirdApkSupport.doStartApplicationWithPackageName(mActivity, pkgName, currClassName, "isFinish", true);    }    public void goBackHome() {        ThirdApkSupport.doStartApplicationWithPackageName(mActivity, pkgName, "com.run.treadmill.activity.home.HomeActivity");        stopFloatWindow();        killThirdApk();    }    private void killThirdApk() {        SystemSoundManager.MusicPause(mActivity);        ThirdApkSupport.killCommonApp(mActivity.getApplicationContext(), mRunningParam.mediaPkgName);        ThirdApkSupport.killInputmethodPid(mActivity, "com.android.inputmethod.latin");//        if (mRunningParam.mediaPkgName.contains("com.android.cameraSelf")) {//            EarphoneSoundCheck.initSysSoundOut();//        }        if (mRunningParam.mediaPkgName.contains("com.softwinner.fireplayer")) {            ShellCmdUtils.getInstance().execCommand("mv /data/data/" + mRunningParam.mediaPkgName + "/shared_prefs " + "/data/");            ShellCmdUtils.getInstance().execCommand("rm -rf /data/data/" + mRunningParam.mediaPkgName + "/*");            ShellCmdUtils.getInstance().execCommand("mv /data/shared_prefs " + "/data/data/" + mRunningParam.mediaPkgName + "/");        }    }    /**     * 结束(各个)悬浮窗     */    public void stopFloatWindow() {        if (isKinomap) {            bluetoothLeScanner.stopScan(mScanCallback); //停止扫描        }        KeepAliveUtil.stopCmdThread();        if (mRunningParam.isPrepare()) {            cancelTime();            myFloatHandler.removeMessages(MsgWhat.MSG_PREPARE_TIME);        }        if (mRunningParam != null) {            mRunningParam.waiteTime = 993;            mRunningParam.waiteNanosTime = 14000;            mRunningParam.recodePreRunData();        }        ControlManager.getInstance().setSendWaiteTime(70);        stopCtrlFloatWindow();        stopParamFloatWindow();        stopDotFloatWindow();        stopBackDotFloatWindow();        stopVoiceFloatWindow();        stopCalculatorFloatWindow();        stopPauseFloatWindow();    }    public void runningActivityStartMedia(@CTConstant.RunMode int runMode) {        if (baseRunTopFloat != null ||                baseRunBottomFloat != null ||                mMediaDotFloatWindow != null ||                backDotFloatWindow != null        ) {            throw new RuntimeException("悬浮窗已创建，不能再次创建！");        }        startFloatWindow(runMode);    }    public void regRxDataCallBackAgain() {        ControlManager.getInstance().regRxDataCallBack(this);    }    synchronized void addView(View view, WindowManager.LayoutParams params) {        if (view != null) {            mWindowManager.addView(view, params);        }    }    synchronized void removeView(View view) {        if (view != null) {            mWindowManager.removeView(view);        }    }    public void setSpeedValue() {        if (baseRunTopFloat != null) {            baseRunTopFloat.setSpeedValue(getSpeedValue(String.valueOf(mRunningParam.getCurrSpeed())));        }    }    public void setInclineValue() {        if (baseRunTopFloat != null) {            baseRunTopFloat.setInclineValue(StringUtil.valueAndUnit(                    String.valueOf((int) mRunningParam.getCurrIncline()), mActivity.getString(R.string.string_unit_percent), runParamUnitTextSize));        }    }    void afterSpeedChanged(float speed) {        baseRunBottomFloat.afterSpeedChanged(speed);    }    void afterInclineChanged(float incline) {        baseRunBottomFloat.afterInclineChanged(incline);    }    void inclineError() {        if (baseRunBottomFloat == null) {            return;        }        baseRunBottomFloat.inclineError();        if (baseRunTopFloat.getInclineStr().equals("E" + ErrorManager.ERR_INCLINE_ADJUST)) {            return;        }        mRunningParam.setInclineError();    }    /**     * 获取带单位的速度值     *     * @param speed     * @return     */    SpannableString getSpeedValue(String speed) {        return StringUtil.valueAndUnit(speed,                isMetric ? mActivity.getString(R.string.string_unit_kph) : mActivity.getString(R.string.string_unit_mph),                runParamUnitTextSize);    }    /***********************************  数据返回处理  *****************************************/    @Override    public void onSucceed(byte[] data, int len) {        if (ErrorManager.getInstance().errorDelayTime > 0) {            ErrorManager.getInstance().errorDelayTime = ErrorManager.SAFE_DELAY_TIME;            ErrorManager.getInstance().exitError = true;            sendNormalMsg(MsgWhat.MSG_ERROR);            return;        }        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {            int curSafeError = resolveDate(data, NormalParam.SAFE_ERROR_INX, NormalParam.SAFE_ERROR_LEN);            int curSysError = resolveDate(data, NormalParam.SYS_ERROR_INX, NormalParam.SYS_ERROR_LEN);            //curSysError = ErrorManager.ERR_NO_ERROR;            //TODO: 注意--> CTConstant.DEVICE_TYPE_AA 强制屏蔽部分错误,后面是否删除,待议//            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {//                if (curSysError == 0x0C) {//                    curSysError = 0;//                }//            }            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {                //AC 机种 25错误也归为安全key 错误                if (curSysError == ErrorManager.ERR_SAFE_FC_ERROR) {                    curSafeError = ErrorManager.ERR_SAFE_FC_ERROR;                }            }            if (curSafeError != ErrorManager.ERR_NO_ERROR) {                if (!ErrorManager.getInstance().isSafeError) {                    ErrorManager.getInstance().lastError = ErrorManager.getInstance().errStatus;                    ErrorManager.getInstance().errStatus = ErrorManager.ERR_SAFE_ERROR;                    ErrorManager.getInstance().isSafeError = true;                    ControlManager.getInstance().emergencyStop();                    SerialUtils.getInstance().stopResend();                }                if (ErrorManager.getInstance().errorDelayTime != ErrorManager.SAFE_DELAY_TIME) {                    ErrorManager.getInstance().errorDelayTime = ErrorManager.SAFE_DELAY_TIME;                }                ErrorManager.getInstance().exitError = true;                myFloatHandler.sendEmptyMessage(MsgWhat.MSG_ERROR);                return;            }            //非扬升错误            if (ErrorManager.getInstance().isNoInclineError(curSysError)) {                sendNormalMsg(MsgWhat.MSG_ERROR);                return;            }            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {                int curInclineError = resolveDate(data, NormalParam.INCLINE_ERROR_INX, NormalParam.INCLINE_ERROR_LEN);                //int curInclineError=ErrorManager.ERR_NO_ERROR;                if (ErrorManager.getInstance().errStatus != ErrorManager.ERR_INCLINE_CALIBRATE && curInclineError != ErrorManager.ERR_NO_ERROR) {                    ErrorManager.getInstance().errStatus = ErrorManager.ERR_INCLINE_ADJUST;                }                if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_INCLINE_ADJUST && !ErrorManager.getInstance().hasInclineError) {                    ErrorManager.getInstance().hasInclineError = true;                    sendNormalMsg(MsgWhat.MSG_ERROR_INCLINE);                }            } /*else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {//暂时屏蔽不报扬升出错                int curInclineError = resolveDate(data, NormalParam.INCLINE_ERROR_INX, NormalParam.INCLINE_ERROR_LEN);                //int curInclineError=ErrorManager.ERR_NO_ERROR;                if (ErrorManager.getInstance().errStatus != ErrorManager.ERR_INCLINE_CALIBRATE && curInclineError != ErrorManager.ERR_NO_ERROR) {                    ErrorManager.getInstance().hasInclineError = true;                }                if (ErrorManager.getInstance().errStatus == ErrorManager.ERR_INCLINE_ADJUST && !ErrorManager.getInstance().hasInclineError) {                    ErrorManager.getInstance().hasInclineError = true;                    sendNormalMsg(MsgWhat.MSG_ERROR_INCLINE);                }            }*/ else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {                if (curSysError == ErrorManager.ERR_INCLINE_ADJUST) {                    ErrorManager.getInstance().errStatus = curSysError;                    ErrorManager.getInstance().hasInclineError = true;                }                if (ErrorManager.getInstance().isHasInclineError()) {                    sendNormalMsg(MsgWhat.MSG_ERROR_INCLINE);                }            }            int keyResult = SerialKeyValue.isNeedSendMsg(resolveDate(data, NormalParam.KEY_VALUE_INX, NormalParam.KEY_VALUE_LEN));            if (keyResult != -1) {                sendNormalMsg(MsgWhat.MSG_DATA_KEY_EVENT, keyResult);            }            if (mRunningParam == null) {                return;            }            if (mRunningParam.isStopStatus()) {                int[] reDate = new int[3];                reDate[0] = resolveDate(data, NormalParam.BELT_STATE_INX, NormalParam.BELT_STATE_LEN);                if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_DC) {                    reDate[1] = resolveDate(data, NormalParam.INCLINE_STATE_INX, NormalParam.INCLINE_STATE_LEN);                } else if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {                    //如果无其他错误只有扬升错误，不管扬升处于什么状态都认为可以开始运动                    // reDate[1] = (data[NormalParam.INCLINE_STATE_INX] & 0x03);                }                // Logger.d("****float 发送****");                sendNormalMsg(MsgWhat.MSG_DATA_BELT_AND_INCLINE, reDate);            }            ErrorManager.getInstance().lastSpeed = resolveDate(data, NormalParam.CURR_SPEED_INX, NormalParam.CURR_SPEED_LEN);/*            int belt = resolveDate(data, NormalParam.BELT_STATE_INX, NormalParam.BELT_STATE_LEN);            sendNormalMsg(MsgWhat.MSG_DATA_BELT_STATUS, belt);*/            if (resolveDate(data, NormalParam.HR_VALUE1_INX, NormalParam.HR_VALUE1_LEN) == 0) {                mRunningParam.setCurrPulse(resolveDate(data, NormalParam.HR_VALUE2_INX, NormalParam.HR_VALUE2_LEN));            } else {                mRunningParam.setCurrPulse(resolveDate(data, NormalParam.HR_VALUE1_INX, NormalParam.HR_VALUE1_LEN));            }            mRunningParam.setCurrAD(resolveDate(data, NormalParam.CURR_AD_INX, NormalParam.CURR_AD_LEN));            mRunningParam.setStepNumber(resolveDate(data, NormalParam.Step_Number_VALUE_INX, NormalParam.Step_Number_VALUE_LEN));        }    }    @Override    public void onFail(byte[] data, int len, int count) {        Logger.e("数据返回失败  --->   onFail()");        //需要根据自己需求,可以在第40次重发出现前,如果要让串口继续运行下去,则必须调用下面方法,而成功会默认调用//        if (count >= 5 && ErrorManager.getInstance().errStatus != ErrorManager.ERR_CMD_FAIL) {//            ErrorManager.getInstance().errStatus = ErrorManager.ERR_CMD_FAIL;//            sendMsg(MsgWhat.MSG_ERROR, ErrorManager.ERR_CMD_FAIL);//        }//        ControlManager.getInstance().reMoveReSendPackage();        //音量恢复        SystemSoundManager.getInstance().setAudioVolume(currentPro, SystemSoundManager.maxVolume);        if (Custom.DEF_DEVICE_TYPE == CTConstant.DEVICE_TYPE_DC) {            // DC光感下控，发最小速度失败时            if ((mRunningParam.isPrepare()                    || mRunningParam.isContinue())                    && data[3] == ParamCons.CMD_SET_SPEED) {                SerialUtils.getInstance().reMoveReSendPackage();            }        }    }    @Override    public void onTimeOut() {        Logger.e("通信超时  --->   onTimeOut()");        if (ErrorManager.getInstance().errStatus != ErrorManager.ERR_TIME_OUT) {            ErrorManager.getInstance().errStatus = ErrorManager.ERR_TIME_OUT;        }        myFloatHandler.sendEmptyMessage(MsgWhat.MSG_ERROR);        //音量恢复        SystemSoundManager.getInstance().setAudioVolume(currentPro, SystemSoundManager.maxVolume);    }    @Override    public void dataCallback() {        if (mRunningParam.isStopStatus() || mRunningParam.isContinue()) {            return;        }        if (!mRunningParam.isRunning()) {            goBackMyApp();            return;        }        // Logger.i("dataCallback()");        if (baseRunBottomFloat != null) {            baseRunBottomFloat.setData();            checkStop();        }        if (baseRunBottomFloat == null) {            return;        }        baseRunBottomFloat.dealLineChart();//        if( mRunningParam.getLcCurStageNum() == -1){//            return;//        }//        mRunningParam.setCurrIncline(mRunningParam.mInclineArray[mRunningParam.getLcCurStageNum()]);//        mRunningParam.setCurrSpeed(mRunningParam.mSpeedArray[mRunningParam.getLcCurStageNum()]);    }    private void checkStop() {        if (mRunningParam.stepManager.isStopRunning) {            // mBaseRunCtrlFloatWindow.btn_start_stop_skip.performClick();            mRunningParam.stepManager.clean();            ControlManager.getInstance().resetIncline();        }    }    @Override    public void onCurStageNumChange() {        setSpeedValue();        if (!ErrorManager.getInstance().isHasInclineError()) {            setInclineValue();        }    }    @Override    public void cooldown10Callback() {    }    private int resolveDate(byte[] date, int offSet, int len) {        int result;        if (len == 3) {            //3个字节 ,暂时不知道如何处理            result = 0;        } else if (len == 2) {            result = DataTypeConversion.bytesToShortLiterEnd(date, offSet);        } else if (len == 1) {            result = DataTypeConversion.byteToInt(date[offSet]);        } else {            result = 0;        }        return result;    }    /**     * 出现错误只执行一次     */    private boolean isActionVolume;    /**     * 音量恢复     */    private void restoreVolume() {        // zrg 打印        Logger.e("restoreVolume()--isActionVolume = " + isActionVolume + " currentPro = " + currentPro);        if (isActionVolume) {            return;        }        isActionVolume = true;        new Thread() {            @Override            public void run() {                SystemSoundManager.getInstance().setAudioVolume(currentPro, SystemSoundManager.maxVolume);            }        }.start();    }    void startPrepare() {        baseRunBottomFloat.disClick();        backDotFloatWindow.disClick();        mMediaDotFloatWindow.disClick();        baseRunBottomFloat.stopPauseTimer();        currentPro = SystemSoundManager.getInstance().getCurrentPro();        //设置音量        SystemSoundManager.getInstance().setAudioVolume(VolumeUtils.Go321Volume, SystemSoundManager.maxVolume);        showOrHideVoiceFloatWindow(true);        mCountdownTask = new EmptyMessageTask(myFloatHandler, MsgWhat.MSG_PREPARE_TIME);        if (mTimer == null) {            mTimer = new Timer();        }        mTimer.schedule(mCountdownTask, 1000, 1000);    }    public void cancelTime() {        if (mCountdownTask != null) {            mCountdownTask.cancel();        }        if (mTimer != null) {            mTimer.cancel();            mTimer.purge();            mTimer = null;        }    }    @Override    public void onRequestConnect() {/*        if(btType == BtHelper.BT_ZY){            BtHelper.getInstance().setCurrRunStatus();        }*/    }    @Override    public void onLastConnect() {    }    @Override    public void onDataCallback(CbData data) {        if (data.dataType == CbData.TYPE_START_RUN) {            // sendMsg(MsgWhat.MSG_DATA_KEY_EVENT, SerialKeyValue.START_CLICK);        }        if (data.dataType == CbData.TYPE_STOP_RUN) {            // sendMsg(MsgWhat.MSG_DATA_KEY_EVENT, SerialKeyValue.STOP_CLICK);        } else if (data.dataType == CbData.TYPE_FINISH_RUN) {            // sendMsg(MsgWhat.MSG_DATA_KEY_EVENT, SerialKeyValue.STOP_CLICK);        }        // if(data.dataType == CbData.TYPE_INCLINE && mBaseInclineFloatWindow != null){        //     // mBaseInclineFloatWindow.onDataCallback(data);        // }        if (data.dataType == CbData.TYPE_INCLINE) {            if (baseRunBottomFloat instanceof QuickStartBottomFloat) {                ((QuickStartBottomFloat) baseRunBottomFloat).onDataCallback(data);            }        }    }    private boolean disPauseBtn = false;    public boolean isQuickToMedia() {        return mActivity.quickToMedia;    }    static class MyFloatHandler extends Handler {        private WeakReference<FloatWindowManager> mWeakRefrence;        private FloatWindowManager mFwm;        private boolean hasError;        MyFloatHandler(Looper looper, FloatWindowManager fwm) {            super(looper);            mWeakRefrence = new WeakReference<>(fwm);        }        @Override        public void handleMessage(Message msg) {            if (mWeakRefrence == null) {                return;            }            mFwm = mWeakRefrence.get();            if (mFwm == null) {                return;            }            switch (msg.what) {                default:                    break;                case MsgWhat.MSG_PREPARE_TIME:                    if (mFwm.mRunningParam.countDown == 0) {                        BuzzerManager.getInstance().buzzRingLongObliged(1000);                    } else if (mFwm.mRunningParam.countDown == -1) {                        mFwm.mRunningParam.countDown = 3;                        mFwm.cancelTime();                        // mFwm.mBaseRunCtrlFloatWindow.btn_start_stop_skip.setEnabled(false);                        mFwm.baseRunBottomFloat.afterPrepare();                        mFwm.backDotFloatWindow.remove();                        mFwm.mMediaDotFloatWindow.enClick();                        //防止go声音没结束就修改回原来的声音                        postDelayed(() -> {                            //音量恢复                            SystemSoundManager.getInstance().setAudioVolume(mFwm.currentPro, SystemSoundManager.maxVolume);                        }, 1000);                        // 321GO之后，禁用1秒暂停键                        mFwm.disPauseBtn = true;                        Logger.i("disPauseBtn = true");                        ThreadUtils.postOnMainThread(() -> {                            try {                                mFwm.disPauseBtn = false;                                Logger.i("disPauseBtn = false");                                mFwm.baseRunBottomFloat.enClick();                            } catch (Exception e) {                                Logger.e("悬浮窗321go后按安全key或返回quickstart，mFwm.mBaseRunCtrlFloatWindow.btn_start_stop_skip.setEnabled(true);");                            }                        }, 1000);                        return;                    } else {                        if (Custom.DEF_DEVICE_TYPE == CTConstant.DEVICE_TYPE_DC) {                            if (mFwm.mRunningParam.countDown == 1) {                                ControlManager.getInstance().reset();                                ControlManager.getInstance().setSpeed(SpManager.getMinSpeed(mFwm.isMetric));                            }                        }                        BuzzerManager.getInstance().buzzRingLongObliged(200);                    }                    mFwm.mRunningParam.countDown--;                    break;                case MsgWhat.MSG_ERROR:                    if (hasError) {                        return;                    }                    hasError = true;                    mFwm.restoreVolume();                    mFwm.cancelTime();                    mFwm.stopFloatWindow();                    Logger.e("mFwm.stopFloatWindow();");                    //TODO:有可能杀不死第三方                    mFwm.killThirdApk();                    ThirdApkSupport.shortDownThirtyLoginApp(mFwm.mActivity);                    mFwm.goBackHome();                    break;                case MsgWhat.MSG_ERROR_INCLINE:                    mFwm.inclineError();                    break;                case MsgWhat.MSG_DATA_KEY_EVENT:                    if (mFwm.baseRunBottomFloat == null) {                        return;                    }                    int curKeyValue = msg.arg1;                    if (curKeyValue == SerialKeyValue.VOICE_UP_CLICK                            || curKeyValue == SerialKeyValue.VOICE_UP_CLICK_LONG_1                            || curKeyValue == SerialKeyValue.VOICE_UP_CLICK_LONG_2) {                        if (mFwm.mVoiceFloatWindow != null                                && !mFwm.mRunningParam.isPrepare()                                && !mFwm.mRunningParam.isContinue()) {                            mFwm.mVoiceFloatWindow.setProgress(1);                        }                        return;                    }                    if (curKeyValue == SerialKeyValue.VOICE_DOWN_CLICK                            || curKeyValue == SerialKeyValue.VOICE_DOWN_CLICK_LONG_1                            || curKeyValue == SerialKeyValue.VOICE_DOWN_CLICK_LONG_2) {                        if (mFwm.mVoiceFloatWindow != null                                && !mFwm.mRunningParam.isPrepare()                                && !mFwm.mRunningParam.isContinue()) {                            mFwm.mVoiceFloatWindow.setProgress(-1);                        }                        return;                    }                    mFwm.baseRunBottomFloat.cmdKeyValue(curKeyValue);                    break;                case MsgWhat.MSG_DATA_BELT_AND_INCLINE:                    int[] data = (int[]) msg.obj;                    if (mFwm.baseRunBottomFloat != null) {                        // 每次进入暂停页面, 保持1秒禁用continue                        if (mFwm.disFlag) {                            mFwm.pauseFw.disContinue();                            return;                        }                        if (mFwm.disPauseBtn) {                            mFwm.baseRunBottomFloat.disIvPause();                            return;                        }                        int beltStatus = data[0];                        int inclineStatus = data[1];                        if (beltStatus != 0) {                            break;                        }                        if (ErrorManager.getInstance().isHasInclineError()) {                            if (mFwm.mRunningParam.isStopStatus()) {                                mFwm.pauseFw.enContinue();                            }                            break;                        }                        if (inclineStatus == 0) {                            if (mFwm.mRunningParam.isStopStatus()) {                                mFwm.pauseFw.enContinue();                            }                        }                    }                    break;            }        }    }    public void postRunnable(Runnable r) {        if (myFloatHandler != null) {            myFloatHandler.post(r);        }    }    private void sendNormalMsg(int what) {        msg = new Message();        msg.what = what;        if (myFloatHandler != null) {            myFloatHandler.sendMessage(msg);        }    }    private void sendNormalMsg(int what, int arg1) {        msg = new Message();        msg.what = what;        msg.arg1 = arg1;        if (myFloatHandler != null) {            myFloatHandler.sendMessage(msg);        }    }    private void sendNormalMsg(int what, Object obj) {        msg = new Message();        msg.what = what;        msg.obj = obj;        if (myFloatHandler != null) {            myFloatHandler.sendMessage(msg);        }    }    public void goBackMyAppToSummary() {        mRunningParam.isQuickToSummary = true;        goBackMyApp();    }    public void paramEnterPauseState() {        if (baseRunTopFloat != null) {            baseRunTopFloat.enterPauseState();        }    }    public void hideCalc() {        if (mCalculatorFloatWindow != null) {            mCalculatorFloatWindow.showOrHideFloatWindow(true);        }    }    public boolean disFlag = false;}