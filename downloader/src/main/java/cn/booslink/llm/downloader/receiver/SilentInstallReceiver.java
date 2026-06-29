package cn.booslink.llm.downloader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import javax.inject.Inject;

import cn.booslink.llm.downloader.IAppManager;
import cn.booslink.llm.downloader.model.InstallState;
import cn.booslink.llm.downloader.utils.InstallStateUtils;
import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class SilentInstallReceiver extends BroadcastReceiver {

    public static final String ACTION_SILENT_INSTALL = "cn.booslink.llm.silent.install";

    private static final String TAG = "SilentInstall";

    @Inject
    IAppManager mInstallManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Timber.tag(TAG).d("intent extras is %s", intent.getExtras() == null ? "null" : "not null");
        Bundle extras = intent.getExtras() != null ? intent.getExtras() : new Bundle();
        Timber.tag(TAG).d("intent = %s", action);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            populateInstallAction(context, action, extras);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void populateInstallAction(Context context, String action, Bundle extras) {
        if (ACTION_SILENT_INSTALL.equals(action)) {
            int status = extras.getInt(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            Timber.tag(TAG).d("status = %s", status);
            String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
            String packageName = extras.getString(PackageInstaller.EXTRA_PACKAGE_NAME);
            Timber.tag(TAG).d("Install message = %s, packageName = %s", message, packageName);
            switch (status) {
                case PackageInstaller.STATUS_PENDING_USER_ACTION:
                    // This test app isn't privileged, so the user has to confirm the install.
                    Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                    if (confirmIntent != null) {
                        confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(confirmIntent);
                    }
                    break;
                case PackageInstaller.STATUS_SUCCESS:
                    Timber.tag(TAG).d("Install succeeded! packageName = %s", packageName);
                    mInstallManager.onAppInstalled(InstallState.SUCCESS, packageName);
                    break;
                case PackageInstaller.STATUS_FAILURE:
                case PackageInstaller.STATUS_FAILURE_ABORTED:
                case PackageInstaller.STATUS_FAILURE_BLOCKED:
                case PackageInstaller.STATUS_FAILURE_CONFLICT:
                case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                case PackageInstaller.STATUS_FAILURE_INVALID:
                case PackageInstaller.STATUS_FAILURE_STORAGE:
                    Timber.tag(TAG).d("Install failed! " + status + ", " + message);
                    InstallState installState = InstallStateUtils.checkInstallResult(status);
                    mInstallManager.onAppInstalled(installState, packageName);
                    break;
                default:
                    Timber.tag(TAG).d("Unrecognized status received from installer: %s", status);
                    break;
            }
        }
    }
}
