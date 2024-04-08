package com.run.treadmill.base;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Nullable;

import com.run.serial.RxDataCallBack;
import com.run.serial.SerialCommand;
import com.run.treadmill.activity.home.HomePresenter;
import com.run.treadmill.interceptor.ErrorInterceptor;
import com.run.treadmill.interceptor.NormalInterceptor;
import com.run.treadmill.interceptor.RealChain;
import com.run.treadmill.interceptor.SafeKeyInterceptor;
import com.run.treadmill.interceptor.SerialInterceptor;
import com.run.treadmill.manager.ErrorManager;
import com.run.treadmill.mcu.control.ControlManager;
import com.run.treadmill.mcu.param.NormalParam;
import com.run.treadmill.mcu.param.ParamCons;
import com.run.treadmill.otamcu.OtaMcuUtils;
import com.run.treadmill.sp.SpManager;
import com.run.treadmill.util.DataTypeConversion;
import com.run.treadmill.util.Logger;
import com.run.treadmill.util.MsgWhat;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/05/29
 */
public abstract class BasePresenter<V extends BaseView> implements RxDataCallBack {
    public String TAG;
    private V view;

    protected CmdHandler mCmdHandler;
    private Message msg, msgNomal;

    public boolean inOnSleep = false;

    private List<SerialInterceptor> mInterceptors;
    private RealChain realChain;

    /**
     * Presenter被创建后调用
     *
     * @param savedState 被意外销毁后重建后的Bundle
     */
    public void onCreatePresenter(@Nullable Bundle savedState) {
        TAG = getClass().getSimpleName();
        // Logger.i("P onCreatePresenter");
        mCmdHandler = new CmdHandler(Looper.getMainLooper(), this);
        mInterceptors = new ArrayList<>();
        mInterceptors.add(new SafeKeyInterceptor());
        mInterceptors.add(new ErrorInterceptor());
        mInterceptors.add(new NormalInterceptor());

        realChain = new RealChain(mInterceptors);
    }

    public void attachView(V v) {
        if (v != null) {
            this.view = v;
        }
    }

    public V getView() {
        return view;
    }

    public void deAttachView() {
        if (view != null) {
            this.view = null;
        }
    }

    /**
     * Presenter被销毁时调用
     */
    public void onDestroyPresenter() {
        // Logger.i("P onDestroyPresenter " + this);
        //销毁时机会错乱，暂时让callback直接覆盖
//        SerialUtils.getInstance().unRegisterPresenter();
    }

    /**
     * 在Presenter意外销毁的时候被调用，它的调用时机和Activity、Fragment、View中的onSaveInstanceState时机相同
     *
     * @param outState
     */
    public void onSaveInstanceState(Bundle outState) {
        // Logger.i("P onSaveInstanceState");
    }

    @Override
    public void onSucceed(byte[] data, int len) {
        // 常态包打印
        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {
            NormalParam.print(data);
        }
        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {
            realChain.changeData(0, data, inOnSleep);
            msgNomal = realChain.procced(data, inOnSleep);

            if (msgNomal.what == MsgWhat.MSG_NOMAL_DATA) {
                normalData(data, len);
            } else {
                mCmdHandler.sendMessage(msgNomal);
            }
        }
    }

    private void normalData(byte[] data, int len) {
        if (ErrorManager.getInstance().isInclineError()) {
            sendMsg(MsgWhat.MSG_ERROR, ErrorManager.getInstance().errStatus);
        }
        //暂停的时候速度也会0
        if (NormalParam.getSpeed(data) != 0) {
            ErrorManager.getInstance().lastSpeed = NormalParam.getSpeed(data);
        }
        if (msgNomal.obj == null) {
            return;
        }
        int[] reDate = (int[]) msgNomal.obj;
        int keyValue = reDate[0];
        if (keyValue != -1) {
            sendNormalMsg(MsgWhat.MSG_DATA_KEY_EVENT, keyValue);
        }
        sendNormalMsg(MsgWhat.MSG_DATA_BELT_AND_INCLINE, reDate);
    }

    @Override
    public void onFail(byte[] data, int len, int count) {
//        Logger.d("onFail    = " + ConvertData.byteArrayToHexString(data, len));
        Logger.e("数据返回失败  --->   onFail()");
//        if (count >= 5 && ErrorManager.getInstance().errStatus != ErrorManager.ERR_CMD_FAIL) {
//            ErrorManager.getInstance().errStatus = ErrorManager.ERR_CMD_FAIL;
//            sendMsg(MsgWhat.MSG_ERROR, ErrorManager.ERR_CMD_FAIL);
//            //需要根据自己需求,可以在第40次重发出现前,如果要让串口继续运行下去,则必须调用下面方法,而成功会默认调用
//            ControlManager.getInstance().reMoveReSendPackage();
//        }

    }

    @Override
    public void onTimeOut() {
        Logger.e("通信超时  --->   onTimeOut()");
        if (ErrorManager.getInstance().errStatus != ErrorManager.ERR_TIME_OUT) {
            ErrorManager.getInstance().errStatus = ErrorManager.ERR_TIME_OUT;
        }
        mCmdHandler.sendEmptyMessage(MsgWhat.MSG_TIME_OUT);
    }

    /**
     * 处理数据返回
     *
     * @param msg
     */
    public void handleCmdMsg(Message msg) {
        if (msg.what == ErrorManager.ERR_SAFE_ERROR || ErrorManager.getInstance().errorDelayTime > 0) {
            if (!(this instanceof HomePresenter)) {
                ErrorManager.getInstance().exitError = true;
            }
            getView().safeError();
            if (inOnSleep) {
                int curKeyValue = msg.arg1;
                if (curKeyValue != 0) {
                    if (OtaMcuUtils.curIsOtamcu) {
                        return;
                    }
                    getView().cmdKeyValue(curKeyValue);
                }
            }
            return;
        }
        if (ErrorManager.getInstance().isSafeError && ErrorManager.getInstance().errorDelayTime <= 0) {
            ErrorManager.getInstance().isSafeError = false;
            getView().hideTips();

            if (SpManager.getGSMode()) {
                ControlManager.getInstance().stopIncline();
            } else {
                ControlManager.getInstance().resetIncline();
            }
        }
        if (msg.what == MsgWhat.MSG_DATA_BELT_AND_INCLINE) {
            getView().hideTips();
        }
        switch (msg.what) {
            default:
                break;
            case MsgWhat.MSG_TIME_OUT:
                getView().commOutError();
                break;
            case MsgWhat.MSG_ERROR:
                getView().showError(msg.arg1);
                if (inOnSleep) {
                    int curKeyValue2 = msg.arg2;
                    if (curKeyValue2 != 0) {
                        if (OtaMcuUtils.curIsOtamcu) {
                            return;
                        }
                        getView().cmdKeyValue(curKeyValue2);
                    }
                }
                break;
            case MsgWhat.MSG_DATA_KEY_EVENT:
                int curKeyValue = msg.arg1;
                if (curKeyValue != -1) {
                    if (OtaMcuUtils.curIsOtamcu) {
                        Logger.i("当前是otamcu界面，不响应按键");
                        return;
                    }
                    getView().cmdKeyValue(curKeyValue);
                }
                break;
            case MsgWhat.MSG_DATA_BELT_AND_INCLINE:
                getView().beltAndInclineStatus(((int[]) msg.obj)[1], ((int[]) msg.obj)[2], ((int[]) msg.obj)[4]);
                break;
        }
    }

    public void sendNormalMsg(int what) {
        msg = new Message();
        msg.what = what;
        mCmdHandler.sendMessage(msg);
    }

    public void sendNormalMsg(int what, int arg1) {
        msg = new Message();
        msg.what = what;
        msg.arg1 = arg1;
        mCmdHandler.sendMessage(msg);
    }

    public void sendNormalMsg(int what, int arg1, int arg2) {
        msg = new Message();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        mCmdHandler.sendMessage(msg);
    }

    public void sendNormalMsg(int what, Object obj) {
        msg = new Message();
        msg.what = what;
        msg.obj = obj;
        mCmdHandler.sendMessage(msg);
    }

    /**
     * 发送msg
     *
     * @param what 消息类型
     */
    public void sendMsg(int what) {
        msg = Message.obtain();
        msg.what = what;
        mCmdHandler.sendMessage(msg);
    }

    /**
     * 发送msg
     *
     * @param what 消息类型
     * @param arg1 消息内容
     */
    public void sendMsg(int what, int arg1) {
        msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        mCmdHandler.sendMessage(msg);
    }

    /**
     * 发送msg
     *
     * @param what 消息类型
     * @param arg1 消息内容1
     * @param arg2 消息内容2
     */
    protected void sendMsg(int what, int arg1, int arg2) {
        msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        mCmdHandler.sendMessage(msg);
    }

    /**
     * 发送msg
     *
     * @param what 消息类型
     * @param obj  obj
     */
    protected void sendMsg(int what, Object obj) {
        msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        mCmdHandler.sendMessage(msg);
    }

    public int resolveDate(byte[] date, int offSet, int len) {
        int result;
        if (len == 3) {
            //3个字节 ,暂时不知道如何处理
            result = 0;
        } else if (len == 2) {
            result = DataTypeConversion.bytesToShortLiterEnd(date, offSet);
        } else if (len == 1) {
            result = DataTypeConversion.byteToInt(date[offSet]);
        } else {
            result = 0;
        }
        return result;
    }
}