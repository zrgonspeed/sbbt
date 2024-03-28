package com.run.treadmill;


import com.run.serial.RxDataCallBack;
import com.run.treadmill.activity.factory.FactoryPresenter;
import com.run.treadmill.activity.floatWindow.FloatWindowManager;
import com.run.treadmill.activity.home.HomeSleepManager;
import com.run.treadmill.activity.home.help.HomeMcu;
import com.run.treadmill.common.CTConstant;
import com.run.treadmill.manager.ControlManager;
import com.run.treadmill.sp.SpManager;

/**
 * 方便跳转到需要修改的地方
 */
public class Custom {
    public static final String PROJECT_NAME = "AC00554-54T-11";
    public static final String MCU_UPDATE_NAME = PROJECT_NAME;
    public static final int DEF_DEVICE_TYPE = CTConstant.DEVICE_TYPE_DC;
    public static final boolean isTestServer = false;
    // 是否开启点击按钮水波纹效果
    public static final boolean CLICK_VIEW_ANIMATION = true;

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
                new FloatWindowManager(null).onSucceed(null, 0);
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

    public interface Volume {
    }

    public interface HomeSleep {
        default void fun() {
            new HomeSleepManager(null).timerComply(0, null);
        }
    }

    public interface Language {
        default void fun() {
            SpManager.getLanguage();
        }
    }

    public static int logo = R.drawable.img_logo;

}
