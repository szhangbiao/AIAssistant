package cn.booslink.llm.common.model.enums;

public enum AIUIIntent {

    UNKNOWN(""),
    EXIT("EXIT"),
    VOLUME_PLUS("VOLUME_PLUS"),
    VOLUME_MINUS("VOLUME_MINUS"),
    VOLUME_MAX("VOLUME_MAX"),
    VOLUME_MIN("VOLUME_MIN"),
    UNMUTE("UNMUTE"),
    MUTE("MUTE");

    public static AIUIIntent fromString(String intent) {
        if (intent == null) {
            return null;
        }
        for (AIUIIntent aiIntent : AIUIIntent.values()) {
            if (aiIntent.intent.equals(intent)) {
                return aiIntent;
            }
        }
        return UNKNOWN;
    }

    private final String intent;

    AIUIIntent(String intent) {
        this.intent = intent;
    }

    public String getIntent() {
        return intent;
    }
}
