package com.run.treadmill.activity.floatWindow.otherFloat;import android.app.Activity;import android.content.Context;import android.graphics.PixelFormat;import android.os.Handler;import android.os.Looper;import android.os.Message;import android.view.Gravity;import android.view.LayoutInflater;import android.view.View;import android.view.WindowManager;import android.widget.ImageView;import android.widget.LinearLayout;import com.run.android.ShellCmdUtils;import com.run.serial.RxDataCallBack;import com.run.serial.SerialCommand;import com.run.serial.SerialUtils;import com.run.treadmill.R;import com.run.treadmill.common.CTConstant;import com.run.treadmill.util.MsgWhat;import com.run.treadmill.manager.BuzzerManager;import com.run.treadmill.mcu.control.ControlManager;import com.run.treadmill.manager.ErrorManager;import com.run.treadmill.manager.FitShowManager;import com.run.treadmill.mcu.param.NormalParam;import com.run.treadmill.mcu.param.ParamCons;import com.run.treadmill.manager.fitshow.other.FitShowStatusCallBack;import com.run.treadmill.serial.SerialKeyValue;import com.run.treadmill.util.DataTypeConversion;import com.run.treadmill.util.ThirdApkSupport;import java.lang.ref.WeakReference;/** * @Description 这里用一句话描述 * @Author GaleLiu * @Time 2019/06/18 */public class SettingBackFloatWindow implements RxDataCallBack, View.OnClickListener, FitShowStatusCallBack {    private Context mContext;    private Activity activity;    private MyFloatHandler myFloatHandler;    private Message msg;    private WindowManager mWindowManager;    private WindowManager.LayoutParams wmParams;    private LinearLayout mFloatWindow;    private ImageView btn_setting_back;    private String pkgName = "com.run.treadmill";    private String actName = pkgName + ".activity.setting.SettingActivity";    private boolean isNoShowErr = false;    public SettingBackFloatWindow(Context context, Activity activity) {        mContext = context;        this.activity = activity;        mWindowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);        myFloatHandler = new MyFloatHandler(Looper.getMainLooper(), this);    }    public SettingBackFloatWindow(Context context, Activity activity, boolean isNoShowErr) {        mContext = context;        this.activity = activity;        this.isNoShowErr = isNoShowErr;        mWindowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);        myFloatHandler = new MyFloatHandler(Looper.getMainLooper(), this);    }    private LinearLayout createFloatWindow(int w, int h) {        View view = LayoutInflater.from(mContext).inflate(R.layout.float_window_setting_back, null);        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));        LinearLayout mWindow = (LinearLayout) view;        wmParams = new WindowManager.LayoutParams();        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;        wmParams.format = PixelFormat.RGBA_8888;        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;        wmParams.gravity = Gravity.END | Gravity.CENTER_HORIZONTAL;        wmParams.x = 0;        wmParams.y = 0;        wmParams.width = w;        wmParams.height = h;        wmParams.windowAnimations = android.R.style.Animation_Translucent;        return mWindow;    }    public void startFloat() {        ControlManager.getInstance().regRxDataCallBack(this);        mFloatWindow = createFloatWindow(mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_110_x), mContext.getResources().getDimensionPixelSize(R.dimen.dp_px_85_y));        mWindowManager.addView(mFloatWindow, wmParams);        btn_setting_back = (ImageView) mFloatWindow.findViewById(R.id.btn_setting_back);        btn_setting_back.setOnClickListener(this);        FitShowManager.getInstance().setFitShowStatusCallBack(this);    }    protected void stopFloat() {        BuzzerManager.getInstance().buzzerRingOnce();        ThirdApkSupport.doStartApplicationWithPackageName(activity, pkgName, pkgName + "." + activity.getLocalClassName(), isNoShowErr);        ShellCmdUtils.getInstance().execCommand("kill " + ThirdApkSupport.findPid(mContext, "com.android.settings"));//        ShellCmdUtils.getInstance().execCommand("kill " + ThirdApkSupport.findPid(mContext, "com.android.bluetooth"));        if (mFloatWindow != null) {            mWindowManager.removeView(mFloatWindow);            mFloatWindow = null;        }        FitShowManager.getInstance().setFitShowStatusCallBack(null);    }    public synchronized void stopFloatWindow() {        if (mFloatWindow != null) {            mFloatWindow.setVisibility(View.GONE);            mWindowManager.removeView(mFloatWindow);            mFloatWindow = null;        }        ThirdApkSupport.killInputmethodPid(mContext, "com.google.android.inputmethod.pinyin");        ShellCmdUtils.getInstance().execCommand("kill " + ThirdApkSupport.findPid(mContext, "com.android.settings"));//        ShellCmdUtils.getInstance().execCommand("kill " + ThirdApkSupport.findPid(mContext, "com.android.bluetooth"));    }    @Override    public void onClick(View v) {        //回到Setting界面        stopFloat();    }    @Override    public void onSucceed(byte[] data, int len) {        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {            int curSafeError = NormalParam.getSafeError(data);            int curSysError = NormalParam.getSysError(data);            //int curSysError = ErrorManager.ERR_NO_ERROR;            //TODO: 注意--> CTConstant.DEVICE_TYPE_AA 强制屏蔽部分错误,后面是否删除,待议//            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AA) {//                if (curSysError == 0x0C) {//                    curSysError = 0;//                }//            }            if (ControlManager.deviceType == CTConstant.DEVICE_TYPE_AC) {                //AC 机种 25错误也归为安全key 错误                if (curSysError == ErrorManager.ERR_SAFE_FC_ERROR) {                    curSafeError = ErrorManager.ERR_SAFE_FC_ERROR;                }            }            if (curSafeError != ErrorManager.ERR_NO_ERROR) {                if (!ErrorManager.getInstance().isSafeError) {                    ErrorManager.getInstance().lastError = ErrorManager.getInstance().errStatus;                    ErrorManager.getInstance().errStatus = ErrorManager.ERR_SAFE_ERROR;                    ErrorManager.getInstance().isSafeError = true;                    ControlManager.getInstance().emergencyStop();                    SerialUtils.getInstance().stopResend();                }                if (ErrorManager.getInstance().errorDelayTime != ErrorManager.SAFE_DELAY_TIME) {                    ErrorManager.getInstance().errorDelayTime = ErrorManager.SAFE_DELAY_TIME;                }                myFloatHandler.sendEmptyMessage(MsgWhat.MSG_ERROR);                return;            }            //非扬升错误            if (ErrorManager.getInstance().isNoInclineError(curSysError)) {                myFloatHandler.sendEmptyMessage(MsgWhat.MSG_ERROR);                return;            }            int curKeyValue = NormalParam.getKey(data);            int keyResult = SerialKeyValue.isNeedSendMsg(curKeyValue);            if (keyResult == SerialKeyValue.BACK_KEY_CLICK) {                myFloatHandler.sendEmptyMessage(MsgWhat.MSG_ERROR);            }        }    }    @Override    public void onFail(byte[] data, int len, int count) {    }    @Override    public void onTimeOut() {        if (ErrorManager.getInstance().errStatus != ErrorManager.ERR_TIME_OUT) {            ErrorManager.getInstance().errStatus = ErrorManager.ERR_TIME_OUT;        }    }    private int resolveDate(byte[] date, int offSet, int len) {        int result;        if (len == 3) {            //3个字节 ,暂时不知道如何处理            result = 0;        } else if (len == 2) {            result = DataTypeConversion.bytesToShortLiterEnd(date, offSet);        } else if (len == 1) {            result = DataTypeConversion.byteToInt(date[offSet]);        } else {            result = 0;        }        return result;    }    private void sendMsg(int wath, Object obj) {        msg = Message.obtain();        msg.what = wath;        msg.obj = obj;        if (myFloatHandler != null) {            myFloatHandler.sendMessage(msg);        }    }    private void sendMsg(int what, int arg1) {        msg = Message.obtain();        msg.what = what;        msg.arg1 = arg1;        if (myFloatHandler != null) {            myFloatHandler.sendMessage(msg);        }    }    @Override    public void fitShowStartRunning() {    }    @Override    public void isFitShowConnect(boolean isConnect) {        if (isConnect) {            goBackHome();        }    }    public void goBackHome() {        ThirdApkSupport.doStartApplicationWithPackageName(activity, pkgName, "com.run.treadmill.activity.home.HomeActivity");        stopFloatWindow();    }    private static class MyFloatHandler extends Handler {        private WeakReference<SettingBackFloatWindow> mWeakRefrence;        private SettingBackFloatWindow mFwm;        private boolean isRelease = false;        MyFloatHandler(Looper looper, SettingBackFloatWindow fwm) {            super(looper);            mWeakRefrence = new WeakReference<>(fwm);        }        @Override        public void handleMessage(Message msg) {            if (mWeakRefrence == null) {                return;            }            mFwm = mWeakRefrence.get();            switch (msg.what) {                default:                    break;                case MsgWhat.MSG_ERROR:                    if (mFwm.isNoShowErr) {                        return;                    }                    if (isRelease) {                        return;                    }                    isRelease = true;                    mFwm.stopFloat();                    break;            }        }    }}