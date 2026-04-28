package cn.booslink.llm.common.model.enums;

public enum Category {
    UNKNOWN(""),
    APP("IFLYTEK.app"),
    WEATHER("IFLYTEK.weather"),
    MUSIC("IFLYTEK.musicX"),
    VIDEO("IFLYTEK.video@2"),
    VIDEO_ENHANCE("BOOSLINK.video_control"),
    DRAMA("IFLYTEK.drama"),
    CONTROL("AIUI.control"),
    KSONG("BOOSLINK.ksong"),
    PAGE_CONTROL("BOOSLINK.page_control"),
    DANCE("BOOSLINK.square_dance"),
    SLEEP("Custom.sleep"); // 自定义

    private final String category;

    public static Category fromString(String category) {
        if (category == null) {
            return null;
        }
        for (Category cat : Category.values()) {
            if (cat.category.equals(category)) {
                return cat;
            }
        }
        return UNKNOWN;
    }

    Category(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}
