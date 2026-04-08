package cn.booslink.llm.common.model;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import cn.booslink.llm.common.model.enums.ApkStatus;

public class ApkDownload {

    public static final int FLAG_APK_FAIL = 101;

    private int apkId;
    private String name;
    private String icon;
    private String downloadUrl;
    private String pkgName;
    private String md5Hash;
    private String apkPath;
    private String versionName;
    private int versionCode;
    private boolean isAppUpdate;
    private int progress;
    private ApkStatus status;
    private int retryCount;
    private String speed;
    private String failedReason;

    private Drawable apkIcon;

    public static ApkDownload empty() {
        ApkDownload download = new ApkDownload();
        download.setApkId(-1);
        return download;
    }

    public static ApkDownload createFromPkgInfo(PkgInfo pkgInfo) {
        ApkDownload download = new ApkDownload();
        if (pkgInfo.getApkId() > 0) {
            download.setApkId(pkgInfo.getApkId());
        }
        download.setName(pkgInfo.getName());
        download.setIcon(pkgInfo.getApkIcon());
        download.setDownloadUrl(pkgInfo.getDownloadUrl());
        download.setPkgName(pkgInfo.getPkgName());
        download.setMd5Hash(pkgInfo.getApkMd5());
        download.setVersionName(pkgInfo.getVersionName());
        download.setVersionCode(pkgInfo.getVersionCode());
        download.setStatus(ApkStatus.DOWNLOAD_PADDING);
        download.setRetryCount(0);
        return download;
    }

    public static ApkDownload createFromApkInfo(ApkInfo apkInfo, boolean isAppUpdate) {
        ApkDownload dto = new ApkDownload();
        dto.setName(apkInfo.getName());
        dto.setPkgName(apkInfo.getPkgName());
        dto.setApkPath(apkInfo.getPath());
        dto.setVersionCode(apkInfo.getVersionCode());
        dto.setStatus(ApkStatus.INSTALL_PADDING);
        dto.setRetryCount(0);
        dto.setProgress(100);
        dto.setApkIcon(apkInfo.getIcon());
        dto.setAppUpdate(isAppUpdate);
        return dto;
    }

    public int getApkId() {
        return apkId;
    }

    public void setApkId(int apkId) {
        this.apkId = apkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public ApkStatus getStatus() {
        if (status == null) {
            status = ApkStatus.UNKNOWN;
        }
        return status;
    }

    public void setStatus(ApkStatus status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isAppUpdate() {
        return isAppUpdate;
    }

    public void setAppUpdate(boolean appUpdate) {
        isAppUpdate = appUpdate;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    public Drawable getApkIcon() {
        return apkIcon;
    }

    public void setApkIcon(Drawable apkIcon) {
        this.apkIcon = apkIcon;
    }

    public boolean isLocalApkInstall() {
        return !TextUtils.isEmpty(apkPath);
    }

    public String getFileName() {
        if (isLocalApkInstall()) {
            return apkPath.substring(apkPath.lastIndexOf("/") + 1);
        }
        return name + getSuffixOrDefault();
    }

    public String getNewFileName() {
        if (isLocalApkInstall()) {
            return apkPath.substring(apkPath.lastIndexOf("/") + 1);
        }
        return pkgName + getVersionOrDefault() + getSuffixOrDefault();
    }

    private String getVersionOrDefault() {
        return !TextUtils.isEmpty(versionName) ? versionName : "";
    }

    private String getSuffixOrDefault() {
        return downloadUrl.contains(".") ? downloadUrl.substring(downloadUrl.lastIndexOf(".")) : ".apk";
    }

    public void progress(int progress, String speed) {
        this.status = ApkStatus.DOWNLOAD_PROGRESS;
        this.progress = progress;
        this.speed = speed;
        this.failedReason = "";
        if (progress == 100) {
            this.speed = "0 KB/S";
        }
    }

    public void progressOnly(int progress, String speed) {
        this.progress = progress;
        this.speed = speed;
        this.failedReason = "";
        if (progress == 100) {
            this.speed = "0 KB/S";
        }
    }

    public void downloadFail() {
        this.status = ApkStatus.DOWNLOAD_FAIL;
        this.progress = 0;
        this.speed = "下载失败，请稍后再试！";
        this.retryCount = FLAG_APK_FAIL;
    }

    public void downloadPauseWithNoSpace() {
        this.status = ApkStatus.DOWNLOAD_PAUSE_WITH_ERROR;
        this.failedReason = "设备存储空间不足，下载失败";
    }

    public void retry() {
        this.status = ApkStatus.DOWNLOAD_FAIL;
        this.progress = 0;
        this.speed = "Apk校验失败，自动重新下载";
    }

    public void retryFail() {
        this.status = ApkStatus.DOWNLOAD_FAIL;
        this.progress = 0;
        this.speed = "Apk校验失败，请联系管理员";
        this.retryCount = FLAG_APK_FAIL;
    }

    public void installRePadding() {
        if (this.status == ApkStatus.INSTALL_FAIL || this.status == ApkStatus.INSTALL_SUCCESS) {
            this.status = ApkStatus.INSTALL_RE_PADDING;
        }
        this.retryCount = 0;
    }

    public void installResult(boolean isSuccess, boolean shouldRetryInstall) {
        this.status = isSuccess ? ApkStatus.INSTALL_SUCCESS : ApkStatus.INSTALL_FAIL;
        if (retryCount > 0 || (!isSuccess && !shouldRetryInstall)) {
            this.retryCount = FLAG_APK_FAIL;
        } else if (retryCount == 0 && !isSuccess) {
            this.retryCount += 1;
        }
        // 目前是存储空间不足不重试安装
        this.failedReason = !shouldRetryInstall ? "设备存储空间不足，安装失败" : "";
    }

    public void installRandom() {
        this.status = ApkStatus.INSTALL_RANDOM;
    }

    public boolean isEmpty() {
        return apkId == -1 && TextUtils.isEmpty(apkPath);
    }

    public boolean isDBEmpty() {
        return apkId == -1 || apkId == 0;
    }

    public void updateByApkInfo(ApkInfo apkInfo) {
        this.name = apkInfo.getName();
        this.apkPath = apkInfo.getPath();
        this.status = ApkStatus.INSTALL_PADDING;
        this.retryCount = 0;
        this.progress = 100;
    }

    public void updateDownloadInfo(ApkDownload otherDownload) {
        this.status = otherDownload.getStatus();
        this.retryCount = otherDownload.getRetryCount();
        this.progress = otherDownload.getProgress();
        this.speed = otherDownload.getSpeed();
        this.failedReason = otherDownload.getFailedReason();
    }

    public boolean isDownloadInstalling() {
        return status == ApkStatus.INSTALL_GOING || (status == ApkStatus.INSTALL_FAIL && retryCount > 0);
    }

    public boolean shouldRemoveFromInstallList() {
        return status.getStatus() >= ApkStatus.INSTALL_PADDING.getStatus();
    }

    public boolean isInstallFail() {
        return status == ApkStatus.INSTALL_FAIL && retryCount == FLAG_APK_FAIL;
    }

    public boolean isDownloadFail() {
        return (status == ApkStatus.DOWNLOAD_FAIL || status == ApkStatus.DOWNLOAD_PAUSE_WITH_ERROR) && retryCount == FLAG_APK_FAIL;
    }

    public boolean isDownloadComplete() {
        return progress == 100;
    }
}
