package cn.booslink.llm.common.model;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable {
    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel source) {
            return new AppInfo(source);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
    private String name;
    private String pkgName;
    private String versionName;
    private int versionCode;
    private String size;
    private Drawable icon;
    private boolean isUninstalling;


    public AppInfo(String pkgName, String name, Drawable icon, String versionName, int versionCode, String size) {
        this.pkgName = pkgName;
        this.name = name;
        this.icon = icon;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.size = size;
        this.isUninstalling = false;
    }

    public AppInfo() {
    }

    protected AppInfo(Parcel in) {
        this.name = in.readString();
        this.pkgName = in.readString();
        this.versionName = in.readString();
        this.versionCode = in.readInt();
        this.size = in.readString();
        this.isUninstalling = in.readByte() != 0;
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isUninstalling() {
        return isUninstalling;
    }

    public void setUninstalling(boolean uninstalling) {
        isUninstalling = uninstalling;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.pkgName);
        dest.writeString(this.versionName);
        dest.writeInt(this.versionCode);
        dest.writeString(this.size);
        dest.writeByte(this.isUninstalling ? (byte) 1 : (byte) 0);
    }
}
