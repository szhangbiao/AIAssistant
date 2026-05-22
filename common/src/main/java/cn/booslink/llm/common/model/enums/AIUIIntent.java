package cn.booslink.llm.common.model.enums;

public enum AIUIIntent {
    UNKNOWN(""),
    EXIT("EXIT"),
    SETTING_CLOSE("SETTING_CLOSE"),
    EXIT_APP("EXIT_APP"),
    EXIT_AGENT("EXIT_AGENT"),
    // 页面跳转
    PAGE_BACK("PAGE_BACK"),
    PAGE_OPEN("PAGE_OPEN"),
    OPEN_RANK("OPEN_RANK"),
    OPEN_CHANNEL("OPEN_CHANNEL"),
    LAUNCH("LAUNCH"),
    LAUNCH_APP("LAUNCH"),
    DOWNLOAD("DOWNLOAD"),
    DOWNLOAD_APP("DOWNLOAD_APP"),
    INSTALL("INSTALL"),
    INSTALL_APP("INSTALL_APP"),
    VOLUME_PLUS("VOLUME_PLUS"),
    VOLUME_MINUS("VOLUME_MINUS"),
    VOLUME_SET("VOLUME_SET"),
    VOLUME_MAX("VOLUME_MAX"),
    VOLUME_MIN("VOLUME_MIN"),
    UNMUTE("UNMUTE"),
    MUTE("MUTE"),
    BRIGHT_UP("BRIGHT_UP"),
    BRIGHT_DOWN("BRIGHT_DOWN"),
    BRIGHT_MAX("BRIGHT_MAX"),
    BRIGHT_MIN("BRIGHT_MIN"),
    RANDOM_MUSIC("RANDOM_MUSIC"),
    MUSIC_ADD("MUSIC_ADD"),
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
    KSONG_REMOVE("KSONG_REMOVE"),
    KSONG_ACCOM("KSONG_ACCOM"),
    CLOSE_ACCOM("CLOSE_ACCOM"),
    KSONG_ORIGIN("KSONG_ORIGIN"),
    KSONG_REPLAY("KSONG_REPLAY"),
    CLOSE_ORIGIN("CLOSE_ORIGIN"),
    SPEED_DOWN("SPEED_DOWN"),
    SPEED_UP("SPEED_UP"),
    CLARITY_DOWN("CLARITY_DOWN"),
    CLARITY_UP("CLARITY_UP"),
    FAVORITE_REMOVE("FAVORITE_REMOVE"),
    FAVORITE_ADD("FAVORITE_ADD"),
    CLOSE_DANMU("CLOSE_DANMU"),
    OPEN_DANMU("OPEN_DANMU"),
    CHANGE_CLARITY("CHANGE_CLARITY"),
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
