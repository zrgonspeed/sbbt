package com.run.treadmill.activity.home;import android.content.Context;import com.chuhui.btcontrol.BtHelper;import com.run.serial.SerialCommand;import com.run.treadmill.activity.home.help.GoRun;import com.run.treadmill.activity.runMode.RunningParam;import com.run.treadmill.base.BasePresenter;import com.run.treadmill.manager.SystemBrightManager;import com.run.treadmill.manager.SystemSoundManager;import com.run.treadmill.manager.control.NormalParam;import com.run.treadmill.manager.control.ParamCons;import com.run.treadmill.sp.SpManager;import java.util.Arrays;public class HomePresenter extends BasePresenter<HomeView> {    private Context mContext;    void setContext(Context context) {        this.mContext = context;    }    public void setUpRunningParam(boolean isMetric) {        RunningParam.reset();        Arrays.fill(RunningParam.getInstance().mInclineArray, 0.0f);        Arrays.fill(RunningParam.getInstance().mSpeedArray, SpManager.getMinSpeed(isMetric));    }    /**     * 检测加油提示和加锁     */    void checkLubeAndLock() {        if (SpManager.getMaxLubeDis() != 0 && SpManager.getMaxLubeDis() - SpManager.getRunLubeDis() <= 0) {            getView().showLube();            return;        }        if ((SpManager.getBackUpTotalRunDis() > 0 && (SpManager.getBackUpTotalRunDis() - SpManager.getBackUpRunDis() <= 0))                || (SpManager.getBackUpRunTotalTime() > 0 && (SpManager.getBackUpRunTotalTime() - SpManager.getBackUpRunTime() <= 0))) {            getView().showLock();            return;        }    }    @Override    public void onSucceed(byte[] data, int len) {        super.onSucceed(data, len);        if (data[2] == SerialCommand.TX_RD_SOME && data[3] == ParamCons.NORMAL_PACKAGE_PARAM) {            int homeHr;            if (resolveDate(data, NormalParam.HR_VALUE1_INX, NormalParam.HR_VALUE1_LEN) == 0) {                homeHr = (resolveDate(data, NormalParam.HR_VALUE2_INX, NormalParam.HR_VALUE2_LEN));            } else {                homeHr = (resolveDate(data, NormalParam.HR_VALUE1_INX, NormalParam.HR_VALUE1_LEN));            }            if (homeHr >= 0) {                if (BtHelper.getInstance().connected()) {                    // 设置发到中颖蓝牙的心率, 在home界面有心跳，zwift也能搜到心率                    BtHelper.getInstance().getRunParamBuilder()                            .hr(homeHr);                }            }        }    }    void reSetSleepTime() {        getView().reSetSleepTime();    }    void wakeUpSleep() {        getView().wakeUpSleep();    }    int getSafeKeyDelayTime(boolean isRebootFinish) {        return GoRun.getSafeKeyDelayTime(isRebootFinish);    }    /**     * 设置语言、音量和亮度的预设值     */    public void setVolumeAndBrightness() {        if (!SpManager.getInitLanguageSoundBrightness()) {            SpManager.setInitLanguageSoundBrightness(true);            SystemSoundManager.getInstance().setAudioVolume((int) (0.6 * SystemSoundManager.maxVolume), SystemSoundManager.maxVolume);            SystemBrightManager.setBrightness(mContext, 167);        }    }}