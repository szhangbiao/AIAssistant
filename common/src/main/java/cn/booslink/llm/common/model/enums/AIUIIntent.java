package cn.booslink.llm.common.model.enums;

public enum AIUIIntent {

    UNKNOWN(""),
    EXIT("EXIT"),
    // 页面跳转
    PAGE_BACK("PAGE_BACK"),
    PAGE_OPEN("PAGE_OPEN"),
    LAUNCH("LAUNCH"),
    DOWNLOAD("DOWNLOAD"),
    INSTALL("INSTALL"),
    VOLUME_PLUS("VOLUME_PLUS"),
    VOLUME_MINUS("VOLUME_MINUS"),
    VOLUME_MAX("VOLUME_MAX"),
    VOLUME_MIN("VOLUME_MIN"),
    UNMUTE("UNMUTE"),
    MUTE("MUTE"),
    BRIGHT_UP("BRIGHT_UP"),
    BRIGHT_DOWN("BRIGHT_DOWN"),
    BRIGHT_MAX("BRIGHT_MAX"),
    BRIGHT_MIN("BRIGHT_MIN"),
    RANDOM_SEARCH("RANDOM_SEARCH"),
    RANDOM_KSONG("RANDOM_KSONG"),
    PLAY("PLAY"),
    QUERY("QUERY"),
    RESUME_PLAY("RESUME_PLAY"),
    PAUSE("PAUSE"),
    REPLAY("REPLAY"),
    CHOOSE_PREVIOUS("CHOOSE_PREVIOUS"),
    CHOOSE_NEXT("CHOOSE_NEXT"),
    CHOOSE_WHICH("CHOOSE_WHICH"),
    CHOOSE_LAST("CHOOSE_LAST"),
    FAST_FORWARD("FAST_FORWARD"),
    REWIND("REWIND"),
    PLAYTIME_SET("PLAYTIME_SET"),
    SKIP_SET("SKIP_SET"),
    SCREEN_FULL("SCREEN_FULL"),
    EXIT_SCREEN_FULL("EXIT_SCREEN_FULL"),
    PLAYLIST_OPEN("PLAYLIST_OPEN"),
    OPEN_SCORE("OPEN_SCORE"),
    CLOSE_SCORE("CLOSE_SCORE"),
    KSONG_TOP("KSONG_TOP"),
    KSONG_ADD("KSONG_ADD"),
    ACCOMPANY("ACCOMPANY"),
    ORIGINAL("ORIGINAL"),
    SPEED_DOWN("SPEED_DOWN"),
    SPEED_UP("SPEED_UP"),
    RATE_DOWN("RATE_DOWN"),
    RATE_UP("RATE_UP"),
    FAVOR_REMOVE("FAVOR_REMOVE"),
    FAVOR_ADD("FAVOR_ADD"),
    CLOSE_DANMU("CLOSE_DANMU"),
    OPEN_DANMU("OPEN_DANMU"),
    CHANGE_RATE("CHANGE_RATE"),
    CHANGE_SPEED("CHANGE_SPEED");

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
