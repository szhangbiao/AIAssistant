package cn.booslink.llm.processor.process.ksong;

import android.content.Intent;

import androidx.annotation.Nullable;


public interface IKSongAction {

    @Nullable
    Intent play(); // 播放

    @Nullable
    Intent pause(); // 暂停

    @Nullable
    Intent originTrack(); // 原唱

    @Nullable
    Intent accompanyTrack(); // 伴唱

    @Nullable
    Intent next(); // 下一曲

    @Nullable
    Intent replay(); // 重播

    @Nullable
    Intent addSong(String artist, String song, boolean isTop); // 点歌

    @Nullable
    Intent removeSong(String artist, String song); // 移除点歌

    @Nullable
    Intent topSong(String artist, String song); // 置顶

    @Nullable
    Intent openScore(); // 打开评分

    @Nullable
    Intent closeScore(); // 关闭评分

    @Nullable
    Intent fullScreen(); // 全屏

    @Nullable
    Intent exitFullScreen(); // 退出全屏

    @Nullable
    Intent openPlaylist(); // 播放列表

    @Nullable
    Intent select(int num); // 选择第几首

    @Nullable
    Intent previousPage(); // 上一页

    @Nullable
    Intent nextPage(); // 下一页

    @Nullable
    Intent closePage(); // 关闭当前页

    @Nullable
    Intent openRecent(); // 打开最近播放

    @Nullable
    Intent openFavorite(); // 打开收藏

    @Nullable
    Intent openLocal(); //

    @Nullable
    Intent openFrequent(); //打开常唱
}
