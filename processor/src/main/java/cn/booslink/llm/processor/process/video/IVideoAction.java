package cn.booslink.llm.processor.process.video;

import android.content.Intent;

public interface IVideoAction {
    //---------------- 页面基础控制 ----------------
    Intent exitApp(); // 退出APP

    Intent pageBack(); // 返回到上一级页面

    // param=urlencode({"tvid":"1","aid":"1","cid":"1"}), 需要有爱奇艺资源接口获取视频数据
    Intent playBack(String params); // 播放视频

    Intent openLogin(); // 打开登录页面

    Intent openBuyVip(); // 打开收银台页面

    Intent openHistory(); // 打开播放历史页面

    Intent openFavorite(); // 打开收藏页面

    // param=urlencode({"cid":"1"})
    Intent openRanking(String channel); // 打开榜单页面

    //param=urlencode({"cid":"1"})
    Intent openHomeChannel(String channel); // 打开首页频道页面

    // param=urlencode({"key":"特斯拉大战金刚狼"})
    Intent openSearch(String key); // 打开搜索页面

    Intent search(String key); // 搜索

    // ---------------- 播放器控制 --------------------
    Intent play(); // 播放

    Intent pause(); //暂停

    Intent replay(); // 重新播放

    Intent next(); // 下一集

    // param=urlencode({"value":"1"})
    Intent choosePlay(String value); // 选集播放

    Intent fastForward(String duration); // 快进，默认 10000ms

    Intent fastBackword(String duration); // 快退

    // param=urlencode({"value":"10000"})
    Intent seekTo(String duration); // 跳转到指定时间

    Intent changeSpeed(String speed); // 切换倍速

    Intent changeRate(String rate); // 切换清晰度

    Intent changeBright(String value); // 修改亮度

    Intent changeVolume(String value); // 修改音量

    Intent skipHead(); // 跳过片头

    Intent skipTile(); // 跳过片尾

    Intent changeFavorite(String value); // 收藏/取消收藏

    Intent changeDanMu(String value); // 开启/关闭弹幕
}
