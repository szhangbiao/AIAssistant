package cn.booslink.llm.downloader.utils;

import android.content.pm.PackageInstaller;

import cn.booslink.llm.downloader.model.InstallState;

public class InstallStateUtils {
    public static InstallState checkInstallResult(int status) {
        switch (status) {
            case PackageInstaller.STATUS_SUCCESS:
                return InstallState.SUCCESS;
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
                return InstallState.USER_RESTRICTED;
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
                return InstallState.CONFLICT;
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                return InstallState.INCOMPATIBLE;
            case PackageInstaller.STATUS_FAILURE_INVALID:
                return InstallState.INVALID;
            case PackageInstaller.STATUS_FAILURE_STORAGE:
                return InstallState.INSUFFICIENT_STORAGE;
            case PackageInstaller.STATUS_FAILURE_ABORTED:
                return InstallState.USER_CANCELED;
            default:
                return InstallState.UNKNOWN;
        }
    }

    public static InstallState checkInstallReturnCode(int returnCode) {
        switch (returnCode) {
            case 1:
                return InstallState.SUCCESS; // PackageManager.INSTALL_SUCCEEDED
            case -111:
                return InstallState.USER_RESTRICTED; // PackageManager.INSTALL_FAILED_USER_RESTRICTED
            case -5:
                return InstallState.CONFLICT; // PackageManager.INSTALL_FAILED_DUPLICATE_PACKAGE
            case -7:
            case -8:
                return InstallState.INCOMPATIBLE; // PackageManager.INSTALL_FAILED_UPDATE_INCOMPATIBLE或PackageManager.INSTALL_FAILED_SHARED_USER_INCOMPATIBLE
            case -2:
                return InstallState.INVALID; // PackageManager.INSTALL_FAILED_INVALID_APK
            case -3:
                return InstallState.INVALID_PATH; // PackageManager.INSTALL_FAILED_INVALID_URI
            case -4:
                return InstallState.INSUFFICIENT_STORAGE; // PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE
            case -12:
                return InstallState.OLDER_SDK; // PackageManager.INSTALL_FAILED_OLDER_SDK
            case -16:
                return InstallState.ABI_INCOMPATIBLE; // PackageManager.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE
            case -25:
                return InstallState.UID_CHANGED; // PackageManager.INSTALL_FAILED_UID_CHANGED
            default:
                return InstallState.UNKNOWN;
        }
    }
}
