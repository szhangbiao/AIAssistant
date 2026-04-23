package cn.booslink.llm.processor.process.ksong;

import android.content.Intent;

import androidx.annotation.Nullable;

import javax.inject.Inject;

public class DuoChangKSongAction implements IKSongAction {

    @Inject
    public DuoChangKSongAction() {

    }

    @Nullable
    @Override
    public Intent play() {
        return null;
    }

    @Nullable
    @Override
    public Intent pause() {
        return null;
    }

    @Nullable
    @Override
    public Intent originTrack() {
        return null;
    }

    @Nullable
    @Override
    public Intent accompanyTrack() {
        return null;
    }

    @Nullable
    @Override
    public Intent next() {
        return null;
    }

    @Nullable
    @Override
    public Intent replay() {
        return null;
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
        return null;
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
}
