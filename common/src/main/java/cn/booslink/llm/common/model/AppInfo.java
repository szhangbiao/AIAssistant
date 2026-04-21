package cn.booslink.llm.common.model;

public class AppInfo {

    private String name;
    private String pkgName;
    private String versionName;
    private int versionCode;
    private String launchActivity;

    public AppInfo(String pkgName, String name, String versionName, int versionCode) {
        this.pkgName = pkgName;
        this.name = name;
        this.versionName = versionName;
        this.versionCode = versionCode;
    }

    public AppInfo(String pkgName, String name, String versionName, int versionCode, String launchActivity) {
        this.pkgName = pkgName;
        this.name = name;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.launchActivity = launchActivity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
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

    public String getLaunchActivity() {
        return launchActivity;
    }

    public void setLaunchActivity(String launchActivity) {
        this.launchActivity = launchActivity;
    }
}
