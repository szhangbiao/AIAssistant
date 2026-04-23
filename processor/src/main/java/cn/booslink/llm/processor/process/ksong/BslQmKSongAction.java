package cn.booslink.llm.processor.process.ksong;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import timber.log.Timber;

public class BslQmKSongAction implements IKSongAction {

    private static final String TAG = "BoosLinKSong";

    @Inject
    public BslQmKSongAction() {

    }

    @Nullable
    @Override
    public Intent play() {
        return getIntentWithParams(17);
    }

    @Nullable
    @Override
    public Intent pause() {
        return getIntentWithParams(18);
    }

    @Nullable
    @Override
    public Intent originTrack() {
        return getIntentWithParams(23, 1);
    }

    @Nullable
    @Override
    public Intent accompanyTrack() {
        return getIntentWithParams(23, 0);
    }

    @Nullable
    @Override
    public Intent next() {
        return getIntentWithParams(21);
    }

    @Nullable
    @Override
    public Intent replay() {
        return getIntentWithParams(25);
    }

    @Nullable
    @Override
    public Intent addSong(String artist, String song, boolean isTop) {
        return getIntentWithParams(1000, isTop ? 1 : 0, artist, song);
    }

    @Nullable
    @Override
    public Intent removeSong(String artist, String song) {
        return null;
    }

    @Nullable
    @Override
    public Intent topSong(String artist, String song) {
        return getIntentWithParams(1000, 1, artist, song);
    }

    @Nullable
    @Override
    public Intent openScore() {
        return getIntentWithParams(1005);
    }

    @Nullable
    @Override
    public Intent closeScore() {
        return getIntentWithParams(1006);
    }

    @Nullable
    @Override
    public Intent fullScreen() {
        return getIntentWithParams(1001);
    }

    @Nullable
    @Override
    public Intent exitFullScreen() {
        return getIntentWithParams(1002);
    }

    @Nullable
    @Override
    public Intent openPlaylist() {
        return getIntentWithParams(8);
    }

    @Nullable
    @Override
    public Intent select(int num) {
        return null;
    }

    @Nullable
    @Override
    public Intent previousPage() {
        return null;
    }

    @Nullable
    @Override
    public Intent nextPage() {
        return null;
    }

    @Nullable
    @Override
    public Intent closePage() {
        return null;
    }

    @Nullable
    @Override
    public Intent openRecent() {
        return getIntentWithParams(7);
    }

    @Nullable
    @Override
    public Intent openFavorite() {
        return getIntentWithParams(1008);
    }

    @Nullable
    @Override
    public Intent openLocal() {
        return getIntentWithParams(1008);
    }

    @Nullable
    @Override
    public Intent openFrequent() {
        return getIntentWithParams(7);
    }

    private Intent getIntentWithParams(int action) {
        return getIntentWithParams(action, -1);
    }

    private Intent getIntentWithParams(int action, int m0) {
        return getIntentWithParams(action, m0, null, null);
    }

    private Intent getIntentWithParams(int action, int m0, String artist, String song) {
        StringBuilder deeplink = new StringBuilder("booslink_kg://?action=" + action);
        if (m0 != -1) {
            deeplink.append("&m0=").append(m0);
        }
        if (!TextUtils.isEmpty(artist)) {
            deeplink.append("&m1=").append(artist);
        }
        if (!TextUtils.isEmpty(song)) {
            deeplink.append("&m2=").append(song);
        }
        deeplink.append("&pull_from=12121&mb=false");
        Uri uri = Uri.parse(deeplink.toString());
        Timber.tag(TAG).d("getIntentWithParams: %s", deeplink.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
