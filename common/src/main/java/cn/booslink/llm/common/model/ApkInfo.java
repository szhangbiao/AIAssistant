package cn.booslink.llm.common.model;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class ApkInfo implements Parcelable {

    private String name;
    private Drawable icon;
    private String size;
    private String path;
    private String pkgName;
    private String versionName;
    private int versionCode;
    private boolean isInstalled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.size);
        dest.writeString(this.path);
        dest.writeString(this.pkgName);
        dest.writeString(this.versionName);
        dest.writeInt(this.versionCode);
        dest.writeByte(this.isInstalled ? (byte) 1 : (byte) 0);
    }

    public ApkInfo() {
    }

    protected ApkInfo(Parcel in) {
        this.name = in.readString();
        this.size = in.readString();
        this.path = in.readString();
        this.pkgName = in.readString();
        this.versionName = in.readString();
        this.versionCode = in.readInt();
        this.isInstalled = in.readByte() != 0;
    }

    public static final Creator<ApkInfo> CREATOR = new Creator<ApkInfo>() {
        @Override
        public ApkInfo createFromParcel(Parcel source) {
            return new ApkInfo(source);
        }

        @Override
        public ApkInfo[] newArray(int size) {
            return new ApkInfo[size];
        }
    };
}
