package cn.booslink.llm.downloader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import javax.inject.Inject;

import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.utils.DeviceUtils;
import cn.booslink.llm.downloader.IAppManager;
import cn.booslink.llm.downloader.model.InstallState;
import dagger.Lazy;


public class PkgInstallBroadcastReceiver extends BroadcastReceiver {

    private final Lazy<IAppManager> mAppManagerProvider;

    @Inject
    public PkgInstallBroadcastReceiver(Lazy<IAppManager> appManagerProvider) {
        this.mAppManagerProvider = appManagerProvider;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            String packageName = null;
            if (intent.getData() != null) {
                packageName = intent.getData().getSchemeSpecificPart();
            }
            boolean isSystemApp = ContextUtils.isSystemApp(context);
            if (!TextUtils.isEmpty(packageName) && !context.getPackageName().equals(packageName)) {
                IAppManager mAppManager = mAppManagerProvider.get();
                if (mAppManager == null) return;
                if (isSystemApp) {
                    onAppInstalledBroadcast(mAppManager, packageName);
                } else {
                    mAppManager.onAppInstalled(InstallState.SUCCESS, packageName);
                }
            }
        }
    }

    private void onAppInstalledBroadcast(IAppManager mAppManager, String packageName) {
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !mAppManager.isPkgInstalling()) || DeviceUtils.unSupportInstallServiceRunWhenAppInBackgroundAboveAndroidQ()) {// 大于Q的版本有Service唤醒回调，不需要考虑onAppInstalled的调用
            mAppManager.onAppInstalled(InstallState.SUCCESS, packageName);
        }
    }
}
