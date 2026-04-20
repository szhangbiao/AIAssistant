package cn.booslink.llm.common.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class PkgInfo {

    @SerializedName("apk_id")
    protected int apkId = -1;
    @SerializedName("name")
    protected String name;
    @SerializedName("pkg_name")
    protected String pkgName;
    @SerializedName("img_url")
    protected String apkIcon;
    @SerializedName("info")
    protected String apkInfo;
    @SerializedName("apk_url")
    protected String downloadUrl;
    @SerializedName("apk_md5")
    protected String apkMd5;
    @SerializedName("version_name")
    private String versionName;
    @SerializedName("version_code")
    private int versionCode;

    private transient String entryPoint;

    private transient boolean isDownloaded;
    private transient String localPath;

    public static PkgInfo empty() {
        return new PkgInfo();
    }

    public static PkgInfo ignore() {
        PkgInfo pkgInfo = new PkgInfo();
        pkgInfo.setApkId(-2);
        return pkgInfo;
    }

    public PkgInfo() {

    }

    public boolean isEmpty() {
        return apkId == -1;
    }

    public boolean isIgnore() {
        return apkId == -2;
    }

    public int getApkId() {
        return apkId;
    }

    public void setApkId(int apkId) {
        this.apkId = apkId;
    }

    public String getName() {
        return !TextUtils.isEmpty(name) ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPkgName() {
        return !TextUtils.isEmpty(pkgName) ? pkgName : "";
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getApkIcon() {
        return !TextUtils.isEmpty(apkIcon) ? apkIcon : "";
    }

    public void setApkIcon(String apkIcon) {
        this.apkIcon = apkIcon;
    }

    public String getApkInfo() {
        return !TextUtils.isEmpty(apkInfo) ? apkInfo : "";
    }

    public void setApkInfo(String apkInfo) {
        this.apkInfo = apkInfo;
    }

    public String getFormatApkInfo() {
        if (TextUtils.isEmpty(apkInfo)) {
            return "";
        }
        return apkInfo.replaceAll("\\n|\\s", "");
    }

    public String getApkMd5() {
        return apkMd5;
    }

    public void setApkMd5(String apkMd5) {
        this.apkMd5 = apkMd5;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
