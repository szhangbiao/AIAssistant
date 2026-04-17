package cn.booslink.llm.common.model.enums;

public enum VideoTag {
    QQ("qq", "com.ktcp.tvvideo"),
    IQIYI("iqiyi", "com.qiyi.video.iv"),
    IQIYI2("iqiyi2", "com.qiyi.video.iv"),
    YOUKU("", "youku");

    private final String tag;
    private final String pkgName;

    VideoTag(String tag, String pkgName) {
        this.tag = tag;
        this.pkgName = pkgName;
    }

    public String getTag() {
        return tag;
    }

    public String getPkgName() {
        return pkgName;
    }
}
