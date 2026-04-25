package cn.booslink.llm.processor.process.dance;

import android.content.Intent;

public interface IDanceAction {
    //----------- App功能 --------------
    Intent squareDance(); // 我要看/打开炫舞广场舞

    Intent dancePerformance(); // 我要看/打开舞队展演

    Intent squareDanceFull(); // 打开 广场舞大全

    Intent danceDrama(); // 打开戏曲

    Intent eightSection(); // 八段锦

    Intent pekingOpera();// 京剧

    Intent modernDance();// 现代舞

    // ------ 播放处理 --------
    Intent pageBack();

    Intent play(); // 播放

    Intent pause(); //暂停

    Intent replay(); // 重新播放

    Intent next(); // 下一集

    Intent choosePlay(String value); // 选集播放

    Intent fastForward(String duration); // 快进

    Intent fastBackword(String duration); // 快退

    Intent seekTo(String duration); // 跳转到指定时间
}
