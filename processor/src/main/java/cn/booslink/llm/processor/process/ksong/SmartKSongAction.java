package cn.booslink.llm.processor.process.ksong;

import android.content.Intent;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import timber.log.Timber;

public class SmartKSongAction implements IKSongAction {

    private static final String TAG = "SmartKSong";

    private static final String ENTER_FULL_SCREEN_ACTION = "enter_full_screen";
    private static final String EXIT_FULL_SCREEN_ACTION = "exit_full_screen";
    private static final String SELECT_ITEM_ACTION = "select_item";
    private static final String PREV_PAGE_ACTION = "prev_page";
    private static final String NEXT_PAGE_ACTION = "next_page";
    private static final String CLOSE_PAGE_ACTION = "close_page";
    private static final String OPEN_RECENT_ACTION = "open_recent";
    private static final String OPEN_FAV_ACTION = "open_fav";
    private static final String OPEN_LOCAL_ACTION = "open_local";
    private static final String OPEN_ORDERED_ACTION = "open_ordered";
    private static final String OPEN_SANG_ACTION = "open_sang";
    private static final String NEXT_ACTION = "next";
    private static final String PLAY_ACTION = "play";
    private static final String PAUSE_ACTION = "pause";
    private static final String REPLAY_ACTION = "replay";
    private static final String ORIGINAL_ACTION = "original";
    private static final String ACCOMP_ACTION = "accomp";
    private static final String ORDER_SONG_ACTION = "order_song";

    @Inject
    public SmartKSongAction() {

    }

    @Nullable
    @Override
    public Intent play() {
        return getIntentWithParams(PLAY_ACTION);
    }

    @Nullable
    @Override
    public Intent pause() {
        return getIntentWithParams(PAUSE_ACTION);
    }

    @Nullable
    @Override
    public Intent originTrack() {
        return getIntentWithParams(ORIGINAL_ACTION);
    }

    @Nullable
    @Override
    public Intent accompanyTrack() {
        return getIntentWithParams(ACCOMP_ACTION);
    }

    @Nullable
    @Override
    public Intent next() {
        return getIntentWithParams(NEXT_ACTION);
    }

    @Nullable
    @Override
    public Intent replay() {
        return getIntentWithParams(REPLAY_ACTION);
    }

    @Nullable
    @Override
    public Intent addSong(String artist, String song, boolean isTop) {
        return getIntentWithParams(ORDER_SONG_ACTION, artist, song);
    }

    @Nullable
    @Override
    public Intent removeSong(String artist, String song) {
        return null;
    }

    @Nullable
    @Override
    public Intent topSong(String artist, String song) {
        return null;
    }

    @Nullable
    @Override
    public Intent openScore() {
        return null;
    }

    @Nullable
    @Override
    public Intent closeScore() {
        return null;
    }

    @Nullable
    @Override
    public Intent fullScreen() {
        return getIntentWithParams(ENTER_FULL_SCREEN_ACTION);
    }

    @Nullable
    @Override
    public Intent exitFullScreen() {
        return getIntentWithParams(EXIT_FULL_SCREEN_ACTION);
    }

    @Nullable
    @Override
    public Intent openPlaylist() {
        return getIntentWithParams(OPEN_ORDERED_ACTION);
    }

    @Nullable
    @Override
    public Intent select(int num) {
        return getIntentWithParams(SELECT_ITEM_ACTION, String.valueOf(num), null);
    }

    @Nullable
    @Override
    public Intent previousPage() {
        return getIntentWithParams(PREV_PAGE_ACTION);
    }

    @Nullable
    @Override
    public Intent nextPage() {
        return getIntentWithParams(NEXT_PAGE_ACTION);
    }

    @Nullable
    @Override
    public Intent closePage() {
        return getIntentWithParams(CLOSE_PAGE_ACTION);
    }

    @Nullable
    @Override
    public Intent openRecent() {
        return getIntentWithParams(OPEN_RECENT_ACTION);
    }

    @Nullable
    @Override
    public Intent openFavorite() {
        return getIntentWithParams(OPEN_FAV_ACTION);
    }

    @Nullable
    @Override
    public Intent openLocal() {
        return getIntentWithParams(OPEN_LOCAL_ACTION);
    }

    @Nullable
    @Override
    public Intent openFrequent() {
        return getIntentWithParams(OPEN_SANG_ACTION);
    }

    private Intent getIntentWithParams(String action) {
        return getIntentWithParams(action, null, null);
    }

    private Intent getIntentWithParams(String action, String m0, String m1) {
        StringBuilder builder = new StringBuilder("huiaichang://?action=").append(action);
        if (m0 != null) {
            builder.append("&m0=").append(m0);
        }
        if (m1 != null) {
            builder.append("&m1=").append(m1);
        }
        String uri = builder.toString();
        Timber.tag(TAG).d("getIntentWithParams: %s", uri);
        Intent intent = new Intent();
        intent.setAction("com.hhc.huiaichang.AI_ACTION");
        intent.putExtra("uri", uri);
        return intent;
    }
}
