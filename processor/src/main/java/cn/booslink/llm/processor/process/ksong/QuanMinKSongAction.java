package cn.booslink.llm.processor.process.ksong;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import timber.log.Timber;

public class QuanMinKSongAction implements IKSongAction {

    private static final String TAG = "QuanMinKSong";

    @Inject
    public QuanMinKSongAction() {

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
        return null;
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
        return null;
    }

    @Nullable
    @Override
    public Intent exitFullScreen() {
        return null;
    }

    @Nullable
    @Override
    public Intent openPlaylist() {
        return getIntentWithParams(8);
    }

    @Nullable
    @Override
    public Intent select(int num) {
        return getIntentWithParams(22, num);
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
        return null;
    }

    @Nullable
    @Override
    public Intent openFavorite() {
        return null;
    }

    @Nullable
    @Override
    public Intent openLocal() {
        return null;
    }

    @Nullable
    @Override
    public Intent openFrequent() {
        return null;
    }

    private Intent getIntentWithParams(int action) {
        return getIntentWithParams(action, -1);
    }

    private Intent getIntentWithParams(int action, int m0) {
        StringBuilder deeplink = new StringBuilder("karaoketv://?action=").append(action);
        if (m0 != -1) {
            deeplink.append("&m0=").append(m0);
        }
        deeplink.append("&pull_from=12121&mb=false");
        Uri uri = Uri.parse(deeplink.toString());
        Timber.tag(TAG).d("getIntentWithParams: %s", deeplink.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // TODO if app open and send broadcast
        return intent;
    }
}
