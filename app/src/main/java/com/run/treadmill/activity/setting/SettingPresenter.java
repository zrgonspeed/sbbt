package com.run.treadmill.activity.setting;

import com.run.treadmill.base.BasePresenter;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.manager.SpManager;

public class SettingPresenter extends BasePresenter<SettingView> {
    private String lockPass = InitParam.CUSTOM_PASS;

    public String loadLockPass() {
        lockPass = SpManager.getCustomPass();
        return lockPass;
    }

    public String loadSrsPass() {
        return InitParam.SRS_PASS;
    }

    public void checkLock(String str) {
        if (str.equals(lockPass) || str.equals(InitParam.SRS_PASS)) {
            getView().enterLockResult(true);
        } else {
            getView().enterLockResult(false);
        }
    }

}
