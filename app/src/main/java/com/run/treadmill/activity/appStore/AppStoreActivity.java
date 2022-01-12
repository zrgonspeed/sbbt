package com.run.treadmill.activity.appStore;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.run.android.ShellCmdUtils;
import com.run.treadmill.R;
import com.run.treadmill.adapter.AppStoreAdapter;
import com.run.treadmill.base.BaseActivity;
import com.run.treadmill.base.EventMessage;
import com.run.treadmill.common.InitParam;
import com.run.treadmill.factory.CreatePresenter;
import com.run.treadmill.http.DownloadListener;
import com.run.treadmill.http.OkHttpHelper;
import com.run.treadmill.manager.Md5Manager;
import com.run.treadmill.manager.SpManager;
import com.run.treadmill.util.FileUtil;
import com.run.treadmill.util.Logger;
import com.run.treadmill.widget.RecycleViewDivider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/17
 */
@CreatePresenter(AppStorePresenter.class)
public class AppStoreActivity extends BaseActivity<AppStoreView, AppStorePresenter> implements AppStoreView, AppStoreAdapter.OnItemClickListener, DownloadListener {
    @BindView(R.id.rl_app)
    RecyclerView rl_app;
    @BindView(R.id.img_loading)
    ImageView img_loading;
    @BindView(R.id.tv_tip)
    TextView tv_tip;
    @BindView(R.id.btn_back)
    ImageView btn_back;

    private final int MSG_RESULT = 11111;
    private final int MSG_DOWNLOADING = 11112;
    /**
     * 安装时apk校验
     */
    private final int MSG_INSTALL_CHECK = 11113;

    private List<AppBean.AppInfo> mApps;
    private AppStoreAdapter adapter;

    /**
     * 当前正在下载的下标
     */
    private int currDownloadInx = -1;
    /**
     * 正在安装的下标
     */
    private int currInstallInx = -1;

    /**
     * 是否第一次加载，防止安装apk后重新加载（安装apk后会执行onpause - onresume）
     */
    private boolean isFirst = true;

    private boolean hasInstallApk = false;

    private MyHandler myHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        grantPermission("com.run.treadmill", Manifest.permission.REQUEST_INSTALL_PACKAGES);
        EventBus.getDefault().register(this);
        myHandler = new MyHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("========= appstore  onResume =======");

        getPresenter().setContext(this);

        SpManager.setInstallOpen(false);
        if (isFirst) {
            btn_back.setEnabled(false);
            getPresenter().getAppList(this);
            isFirst = false;
        }

        /*ArrayList<AppBean.AppInfo> appInfos = new ArrayList<>();
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("YouTube");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("GoogleChrome");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("YouTube");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("GoogleChrome");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("YouTube");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("GoogleChrome");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("YouTube");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("GoogleChrome");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("YouTube");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("GoogleChrome");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("YouTube");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("GoogleChrome");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("YouTube");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("GoogleChrome");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("YouTube");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
        {
            AppBean.AppInfo appInfo = new AppBean.AppInfo();
            appInfo.setName("GoogleChrome");
            appInfo.setVersion("14.25.57");
            appInfo.setImgId(getPresenter().getAppImgs().get(appInfo.getName()));
            appInfos.add(appInfo);
        }
                initList(appInfos);

        */

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_app_store;
    }

    @Override
    public void hideTips() {

    }

    @Override
    public void safeError() {
        if (currInstallInx != -1) {
            return;
        }
        super.safeError();
    }

    @Override
    public void commOutError() {
        if (currInstallInx != -1) {
            return;
        }
        super.commOutError();
    }

    @Override
    public void cmdKeyValue(int keyValue) {

    }

    @Override
    public void beltAndInclineStatus(int beltStatus, int inclineStatus, int curInclineAd) {

    }

    @Override
    public void showFailure() {
        hideLoading();
        tv_tip.setText(R.string.string_app_store_fail);
        tv_tip.setVisibility(View.VISIBLE);
        btn_back.setEnabled(true);
    }

    @Override
    public void showLoading() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        animation.setInterpolator(new LinearInterpolator());
        img_loading.startAnimation(animation);
    }

    @Override
    public void hideLoading() {
        img_loading.clearAnimation();
        img_loading.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(int position) {
        AppBean.AppInfo app = mApps.get(position);
        if (app == null) {
            return;
        }


        String path = getPresenter().downloadPath + "/" + mApps.get(position).getName() + ".apk";


        // apk文件存在
        if (FileUtil.isCheckExist(path)) {
            mApps.get(position).isUpdate = AppBean.CHECKING;
            adapter.notifyItemChanged(position);
            btnBackEnable();
            getApkMd5(path, position);
            return;
        }
        // 其它的下载中的apk还没下载完成
        if (currDownloadInx != -1) {
            mApps.get(position).isUpdate = AppBean.WAIT_DOWNLOAD;
            adapter.notifyItemChanged(position);
            btnBackEnable();
            return;
        }

        // 下载该apk
        downloadApk(app.getUrl(), app.getName(), position);
    }

    @Override
    public void onDownloadSuccess(File file) {
        Logger.d("==== 下载完成 ====" + file.getName());

        if (currInstallInx != -1) {
            // 等待安装，因为其它的apk还没装好
            mApps.get(currDownloadInx).isUpdate = AppBean.WAIT_INSTALL;
        } else {
            // 当前没有其它apk在安装
            mApps.get(currDownloadInx).isUpdate = AppBean.INSTALLING;
            installApk(getPresenter().downloadPath + "/" + mApps.get(currDownloadInx).getName().replace(" ", "") + ".apk", currDownloadInx);
        }

        adapter.notifyItemChanged(currDownloadInx);
        btnBackEnable();
        downLoadNext();
    }

    @Override
    public void onDownLoading(int progress, long lave) {
        Message msg = myHandler.obtainMessage();
        msg.what = MSG_DOWNLOADING;
        msg.arg1 = progress;
        myHandler.sendMessage(msg);
    }

    @Override
    public void onDownloadFailed(Exception e) {
        Logger.e("=== 下载异常 ===" + e);
        mApps.get(currDownloadInx).isUpdate = AppBean.DOWNLOAD_FAIL;
        adapter.notifyItemChanged(currDownloadInx);
        currDownloadInx = -1;
        btnBackEnable();
        downLoadNext();
    }

    private synchronized void downLoadNext() {
        for (int i = 0; i < mApps.size(); i++) {
            if (mApps.get(i).isUpdate == AppBean.WAIT_DOWNLOAD) {
                downloadApk(mApps.get(i).getUrl(), mApps.get(i).getName(), i);
                return;
            }
        }
        currDownloadInx = -1;
    }

    private synchronized void downloadApk(String url, String name, int inx) {
        Logger.d("=== 开始下载 ===" + inx + ">  名字  >>>" + name);
        mApps.get(inx).isUpdate = AppBean.DOWNLOADING;
        btnBackEnable();
        adapter.notifyItemChanged(inx);
        currDownloadInx = inx;
        //下载的apk命名不能有空格，不然命令安装会失败
        OkHttpHelper.download(url, getPresenter().downloadPath, name.replace(" ", "") + ".apk", "AppStoreActivity", this);
    }

    private synchronized void installApkNext() {
        for (int i = 0; i < mApps.size(); i++) {
            if (mApps.get(i).isUpdate == AppBean.WAIT_INSTALL) {
                mApps.get(i).isUpdate = AppBean.INSTALLING;
                adapter.notifyItemChanged(i);
                installApk(getPresenter().downloadPath + "/" + mApps.get(i).getName().replace(" ", "") + ".apk", i);
                return;
            }
        }
        currInstallInx = -1;
    }

    private void getApkMd5(String filePath, final int inx) {
        new Thread(() -> {
            Logger.d("====getApkMd5 calc MD5 ==== inx = " + inx);
            Message msg = new Message();
            msg.what = MSG_INSTALL_CHECK;
            msg.arg1 = inx;
            msg.obj = Md5Manager.fileToMD5(filePath);
            myHandler.sendMessage(msg);
        }).start();
    }

    private synchronized void checkApkMd5(final String md5, int inx) {
        String path = getPresenter().downloadPath + "/" + mApps.get(inx).getName() + ".apk";
        if (mApps.get(inx).getSign().equals(md5)) {
            if (currInstallInx != -1) {
                mApps.get(inx).isUpdate = AppBean.WAIT_INSTALL;
                adapter.notifyItemChanged(inx);
                return;
            }
            mApps.get(inx).isUpdate = AppBean.INSTALLING;
            currInstallInx = inx;
            adapter.notifyItemChanged(inx);
            installApk(path, inx);
            return;
        }
        Toast.makeText(this, R.string.string_app_store_md5_fail, Toast.LENGTH_LONG).show();
        installApkNext();

        mApps.get(inx).isUpdate = AppBean.UPDATE;
        adapter.notifyItemChanged(inx);

        //删除检验失败的文件
        getPresenter().deleteApkFile(path);
    }

    private synchronized void installApk(String filePath, int inx) {
        currInstallInx = inx;

        String apkMD5 = Md5Manager.fileToMD5(filePath);
        if (apkMD5 != null && !mApps.get(currInstallInx).getSign().equals(apkMD5)) {
            Toast.makeText(this, R.string.string_app_store_install_fail, Toast.LENGTH_LONG).show();
            installApkNext();
            return;
        }
        btnBackEnable();
        new Thread(() -> {
            Logger.d("==== 安装中 ====" + inx);
            if (inx == 0) {
                SpManager.setInstallOpen(true);
                SpManager.setUpdateIsNetwork(true);
            }
            int res = ShellCmdUtils.getInstance().execCommand("pm install -r " + filePath);
            Message msg = myHandler.obtainMessage();
            msg.what = MSG_RESULT;
            msg.arg1 = res;
            myHandler.sendMessage(msg);
        }).start();
    }

    public void goBack(View view) {
        this.finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(EventMessage message) {

        if ("下载成功".equals(message.getMessage()) || "下载失败".equals(message.getMessage())) {
            for (AppBean.AppInfo info : mApps) {
                if (info.getName().equals(InitParam.PROJECT_NAME)) {
                    info.isUpdate = AppBean.UPDATE;
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
        }
        OkHttpHelper.cancel("AppStoreActivity");
    }

    private void btnBackEnable() {
        for (AppBean.AppInfo app : mApps) {
            if (app.isUpdate == AppBean.WAIT_DOWNLOAD || app.isUpdate == AppBean.DOWNLOADING
                    || app.isUpdate == AppBean.WAIT_INSTALL || app.isUpdate == AppBean.INSTALLING
                    || app.isUpdate == AppBean.CHECKING) {
                btn_back.setEnabled(false);
                return;
            }
        }
        btn_back.setEnabled(true);
    }

    @Override
    public void initList(List<AppBean.AppInfo> apps) {
        btn_back.setEnabled(true);
        if (apps == null) {
            return;
        }
        apps.removeAll(Collections.singleton(null));
        if (apps.isEmpty()) {
            return;
        }

        for (AppBean.AppInfo app : apps) {
            app.checkUpdate(this, getPresenter().getAppPacknames().get(app.getName()));
            app.setImgId(getPresenter().getAppImgs().get(app.getName()));
        }
        mApps = new ArrayList<>();
        mApps.addAll(apps);
        rl_app.setVisibility(View.VISIBLE);
        adapter = new AppStoreAdapter(apps);
        adapter.addItemClickListener(AppStoreActivity.this);
        rl_app.setLayoutManager(new LinearLayoutManager(AppStoreActivity.this));
        rl_app.addItemDecoration(new RecycleViewDivider(AppStoreActivity.this, LinearLayoutManager.HORIZONTAL));
        ((SimpleItemAnimator) (rl_app.getItemAnimator())).setSupportsChangeAnimations(false);
        rl_app.setAdapter(adapter);
    }

    static class MyHandler extends Handler {
        private WeakReference<AppStoreActivity> reference;

        MyHandler(AppStoreActivity activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            AppStoreActivity mActivity = reference.get();
            if (mActivity == null) {
                return;
            }
            if (msg.what == mActivity.MSG_RESULT) {
                Logger.d("==== 安装结果 ====" + msg.arg1);

                if (msg.arg1 == 0) {//安装成功
                    mActivity.hasInstallApk = true;
                    mActivity.getPresenter().deleteApkFile(mActivity.getPresenter().downloadPath + "/" + mActivity.mApps.get(mActivity.currInstallInx).getName() + ".apk");
                    mActivity.mApps.get(mActivity.currInstallInx).isUpdate = AppBean.NO_UPDATE;
                } else {
                    //安装失败
                    Toast.makeText(mActivity, R.string.string_app_store_install_fail, Toast.LENGTH_LONG).show();
                    mActivity.mApps.get(mActivity.currInstallInx).isUpdate = AppBean.UPDATE;
                }

                mActivity.adapter.notifyItemChanged(mActivity.currInstallInx);
                mActivity.installApkNext();
                mActivity.btnBackEnable();
            } else if (msg.what == mActivity.MSG_DOWNLOADING) {
                if (mActivity.currDownloadInx == -1) {
                    return;
                }
                if (mActivity.mApps.get(mActivity.currDownloadInx).progress != msg.arg1) {
                    mActivity.mApps.get(mActivity.currDownloadInx).progress = msg.arg1;
                    mActivity.adapter.notifyItemChanged(mActivity.currDownloadInx);
                }
            } else if (msg.what == mActivity.MSG_INSTALL_CHECK) {
                mActivity.checkApkMd5((String) msg.obj, msg.arg1);
            }
        }
    }

    boolean grantPermission(String packageName, String permission) {
        try {
            Object object = getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Method method1 = localClass.getMethod("setMode", int.class, int.class, String.class, int.class);
            method1.setAccessible(true);
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(
                    "com.run.treadmill", 0);
            method1.invoke(object, 66, applicationInfo.uid, "com.run.treadmill", AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
