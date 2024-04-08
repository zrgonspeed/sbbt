package com.run.treadmill;


import com.run.serial.RxDataCallBack;
import com.run.treadmill.activity.factory.FactoryPresenter;
import com.run.treadmill.activity.floatWindow.mcu.FloatMcuData;
import com.run.treadmill.activity.home.help.HomeMcu;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.mcu.control.ControlManager;

/**
 * 方便跳转到需要修改的地方
 */
public class Custom {
    public static final String PROJECT_NAME = "AC00554-54T-11";
    public static final String MCU_UPDATE_NAME = PROJECT_NAME;
    public static final int DEF_DEVICE_TYPE = CTConstant.DEVICE_TYPE_DC;
    public static final boolean isTestServer = false;
    // 是否开启点击按钮水波纹效果  使用第三方库的
    public static final boolean CLICK_VIEW_ANIMATION = true;
    // 进入home显示加载动画的时间 秒
    public static final long HomeLoadAnimTime = 7;

    public interface Volume {
        int normalMaxVolume = 15;   // 正常喇叭播放
        int insertEarMaxVolume = 15;    // 插入耳机
        int inputMaxVolume = 15;    // 音源输入
        int Go321Volume = 3;

        int db = 2;  //db为0表示保持音量不变，db为负数表示较低音量，为正数表示提高音量
    }

    public interface HomeSleep {
        int SLEEP_TIME = 60 * 30; // 单位 秒
        boolean SLEEP_DEF = true;
    }

    public interface Language {
        String defLanguage = "en";  // de en fr es pt  繁体 简体
    }

    public static int logo = R.drawable.img_logo;

    public static class Mcu {
        public interface McuReboot {
        }

        public interface CmdParams {
        }

        public interface Normal {
        }

        public interface Key {
        }

        public interface Control {
        }

        public interface SubControl {
        }

        public interface Calibration {
            default void cali() {
                ControlManager.getInstance().calibrate();
            }

            default void onSuccess() {
                new FactoryPresenter().onSucceed(null, 0);
            }
        }

        public interface McuOnSuccess {
            default void home() {
                new HomeMcu(null).onSucceed(null, 0);
                new HomeMcu(null).cmdKeyValue(0);
                new HomeMcu(null).beltAndInclineStatus(0, 0, 0);
            }

            default void floatWindow() {
                FloatMcuData.onSucceed(null, 0, null);
            }

            default void factory() {
                new FactoryPresenter().onSucceed(null, 0);
            }

            default void base() {
                RxDataCallBack.class.toString();
            }
        }
    }

    public interface Application {
    }
}
