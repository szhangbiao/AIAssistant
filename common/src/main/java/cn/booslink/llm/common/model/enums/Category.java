package cn.booslink.llm.common.model.enums;

public enum Category {
    UNKNOWN(""), WEATHER("IFLYTEK.weather");

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
