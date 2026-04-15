package cn.booslink.llm.common.model.enums;

import android.text.TextUtils;

public enum AIUITag {
    UNKNOW(""), LAUNCH("launch");
    private final String tag;

    public static AIUITag fromTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return UNKNOW;
        }
        for (AIUITag aiuiTag : AIUITag.values()) {
            if (aiuiTag.tag.equals(tag)) {
                return aiuiTag;
            }
        }
        return UNKNOW;
    }

    AIUITag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
