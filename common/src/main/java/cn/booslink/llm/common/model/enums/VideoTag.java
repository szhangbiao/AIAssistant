package cn.booslink.llm.common.model.enums;

import android.text.TextUtils;

public enum VideoTag {
    QQ("qq", "com.ktcp.tvvideo"),
    IQIYI("iqiyi", "com.qiyi.video.iv"),
    IQIYI2("iqiyi2", "com.qiyi.video.iv"),
    YOUKU("youku", "com.cibn.tv");

    private final String tag;
    private final String pkgName;

    VideoTag(String tag, String pkgName) {
        this.tag = tag;
        this.pkgName = pkgName;
    }

    public static VideoTag fromString(String jsonStr) {
        if (TextUtils.isEmpty(jsonStr)) return null;
        for (VideoTag tag : VideoTag.values()) {
            if (tag.getTag().equals(jsonStr)) {
                return tag;
            }
        }
        return null;
    }

    public String getTag() {
        return tag;
    }

    public String getPkgName() {
        return pkgName;
    }
}
