package cn.booslink.llm.processor.process.video;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Pair;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

public class IQiYiVideoAction implements IVideoAction {

    private static final String TAG = "IQiYiVideo";

    private static final String ACTION_BACK = "BACK";
    private static final String ACTION_EXIT_APP = "STOP_APP";
    private static final String ACTION_PLAYBACK = "PLAY_BACK";
    private static final String ACTION_OPEN_LOGIN = "OPEN_LOGIN";
    private static final String ACTION_OPEN_BUY_VIP = "OPEN_BUY_VIP";
    private static final String ACTION_OPEN_HISTORY = "OPEN_HISTORY";
    private static final String ACTION_OPEN_SUBSCRIPT = "OPEN_SUBSCRIPT";
    private static final String ACTION_OPEN_RANK_LIST = "OPEN_RANK_LIST";
    private static final String ACTION_HOME_CHANNEL = "JUMP_HOME";
    private static final String ACTION_OPEN_SEARCH = "OPEN_SEARCH";
    private static final String ACTION_SEARCH = "SEARCH";
    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_REPLAY = "REPLAY";
    private static final String ACTION_NEXT = "NEXT";
    private static final String ACTION_EPISODE = "EPISODE";
    private static final String ACTION_FAST_FORWARD = "FAST_FORWARD";
    private static final String ACTION_FAST_BACKWARD = "FAST_BACKWARD";
    private static final String ACTION_SEEK = "SEEK";
    private static final String ACTION_SPEED = "SPEED";
    private static final String ACTION_CHANGE_RATE = "CHANGE_RATE";
    private static final String ACTION_CHANGE_BRIGHT = "CHANGE_BRIGHT";
    private static final String ACTION_CHANGE_VOLUME = "CHANGE_VOLUME";
    private static final String ACTION_SKIP_VIDEO_HEAD = "SKIP_VIDEO_HEAD";
    private static final String ACTION_SKIP_VIDEO_TILE = "SKIP_VIDEO_TILE";
    private static final String ACTION_CHANGE_FAVOR = "CHANGE_FAVOR";
    private static final String ACTION_CHANGE_DANMU_STATUS = "CHANGE_DANMU_STATUS";

    private static final Map<String, String> mChannelIdMap = new HashMap<>();

    static {
        mChannelIdMap.put("杜比全景声", "-3");
        mChannelIdMap.put("云影院", "-2");
        mChannelIdMap.put("推荐", "-1");
        mChannelIdMap.put("总榜", "-1");
        mChannelIdMap.put("电影", "1");
        mChannelIdMap.put("电视剧", "2");
        mChannelIdMap.put("纪录片", "3");
        mChannelIdMap.put("动漫", "4");
        mChannelIdMap.put("综艺", "6");
        mChannelIdMap.put("儿童", "15");
    }

    @Inject
    public IQiYiVideoAction() {
    }

    @Override
    public Intent exitApp() {
        return getIntent(ACTION_EXIT_APP);
    }

    @Override
    public Intent pageBack() {
        return getIntent(ACTION_BACK);
    }

    @Override
    public Intent playBack(String params) {
        // "tvid":"1","aid":"1","cid":"1"}
        return null;
    }

    @Override
    public Intent openLogin() {
        return getIntent(ACTION_OPEN_LOGIN);
    }

    @Override
    public Intent openBuyVip() {
        return getIntent(ACTION_OPEN_BUY_VIP);
    }

    @Override
    public Intent openHistory() {
        return getIntent(ACTION_OPEN_HISTORY);
    }

    @Override
    public Intent openFavorite() {
        return getIntent(ACTION_OPEN_SUBSCRIPT);
    }

    @Override
    public Intent openRanking(String channel) {
        // 打开${频道}的${榜单}页面
        if (TextUtils.isEmpty(channel)) {
            Timber.tag(TAG).w("openRanking: channel parameter is null or empty");
            return null;
        }
        String cid = mChannelIdMap.get(channel);
        if (TextUtils.isEmpty(cid)) {
            Timber.tag(TAG).w("openRanking: no channel mapping found for channel: %s", channel);
            return null;
        }
        String encodeCid = encodeParam("cid", cid);
        return getIntentWithParams(ACTION_OPEN_RANK_LIST, encodeCid);
    }

    @Override
    public Intent openHomeChannel(String channel) {
        // 打开${主页}的${频道}页面
        if (TextUtils.isEmpty(channel)) {
            Timber.tag(TAG).w("openHomeChannel: channel parameter is null or empty");
            return null;
        }
        String cid = mChannelIdMap.get(channel);
        if (TextUtils.isEmpty(cid)) {
            Timber.tag(TAG).w("openHomeChannel: no channel mapping found for channel: %s", channel);
            return null;
        }
        String encodeCid = encodeParam("cid", cid);
        return getIntentWithParams(ACTION_HOME_CHANNEL, encodeCid);
    }

    @Override
    public Intent openSearch(String key) {
        if (TextUtils.isEmpty(key)) {
            Timber.tag(TAG).w("openSearch: key parameter is null or empty");
            return null;
        }
        String encodeKey = encodeParam("key", key);
        return getIntentWithParams(ACTION_OPEN_SEARCH, encodeKey);
    }

    @Override
    public Intent search(String key) {
        if (TextUtils.isEmpty(key)) {
            Timber.tag(TAG).w("search: key parameter is null or empty");
            return null;
        }
        String encodeKey = encodeParam("key", key);
        return getIntentWithParams(ACTION_SEARCH, encodeKey);
    }

    @Override
    public Intent play() {
        return getIntent(ACTION_PLAY);
    }

    @Override
    public Intent pause() {
        return getIntent(ACTION_PAUSE);
    }

    @Override
    public Intent replay() {
        return getIntent(ACTION_REPLAY);
    }

    @Override
    public Intent next() {
        return getIntent(ACTION_NEXT);
    }

    /**
     * @param value 取值有 选集序号或 START:第一集，END:最后一集，DOWN:下一集
     * @return 意图
     */
    @Override
    public Intent choosePlay(String value) {
        String encodeParams = encodeParam("value", value);
        return getIntentWithParams(ACTION_EPISODE, encodeParams);
    }

    /**
     * 快进
     *
     * @param duration 快进时间，默认 10000ms
     * @return 意图
     */
    @Override
    public Intent fastForward(String duration) {
        String encodeDuration = encodeParam("value", duration);
        return getIntentWithParams(ACTION_FAST_FORWARD, encodeDuration);
    }

    @Override
    public Intent fastBackword(String duration) {
        String encodeDuration = encodeParam("value", duration);
        return getIntentWithParams(ACTION_FAST_BACKWARD, encodeDuration);
    }

    /**
     * 相对当前进度的时间，跳转到xx毫秒
     *
     * @param duration 跳转时间
     * @return 意图
     */
    @Override
    public Intent seekTo(String duration) {
        String encodeDuration = encodeParams(Pair.create("value", duration), Pair.create("allowOverflow", "1"));
        return getIntentWithParams(ACTION_SEEK, encodeDuration);
    }

    /**
     * 切换倍速
     *
     * @param value 取值有 75，100，125，150，200，300，UP，DOWN
     * @return 意图
     */
    @Override
    public Intent changeSpeed(String value) {
        String encodeValue = encodeParam("value", value);
        return getIntentWithParams(ACTION_SPEED, encodeValue);
    }

    /**
     * 切换清晰度
     *
     * @param rate 480，720，1080，4k，UP，DOWN
     * @return 意图
     */
    @Override
    public Intent changeRate(String rate) {
        String encodeRate = encodeParams(Pair.create("rate", rate), Pair.create("autoPick", "1"));
        return getIntentWithParams(ACTION_CHANGE_RATE, encodeRate);
    }

    /**
     * 修改亮度
     *
     * @param value 亮度值 0-225 或 UP，DOWN
     * @return 意图
     */
    @Override
    public Intent changeBright(String value) {
        String encodeValue = encodeParam("value", value);
        return getIntentWithParams(ACTION_CHANGE_BRIGHT, encodeValue);
    }

    /**
     * 修改音量
     *
     * @param value 取值 0-500或 UP，DOWN
     * @return 意图
     */
    @Override
    public Intent changeVolume(String value) {
        String encodeValue = encodeParam("value", value);
        return getIntentWithParams(ACTION_CHANGE_VOLUME, encodeValue);
    }

    @Override
    public Intent skipHead() {
        return getIntent(ACTION_SKIP_VIDEO_HEAD);
    }

    @Override
    public Intent skipTile() {
        return getIntent(ACTION_SKIP_VIDEO_TILE);
    }

    /**
     * 切换收藏状态
     *
     * @param value true or false
     * @return 意图
     */
    @Override
    public Intent changeFavorite(String value) {
        String encodeValue = encodeParam("value", value);
        return getIntentWithParams(ACTION_CHANGE_FAVOR, encodeValue);
    }

    /**
     * 开启/关闭弹幕
     *
     * @param value true or false
     * @return 意图
     */
    @Override
    public Intent changeDanMu(String value) {
        String encodeValue = encodeParam("value", value);
        return getIntentWithParams(ACTION_CHANGE_DANMU_STATUS, encodeValue);
    }

    private String encodeParam(String key, String value) {
        try {
            JSONObject object = new JSONObject();
            object.put(key, value);
            String encodeCid;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                encodeCid = URLEncoder.encode(object.toString(), StandardCharsets.UTF_8);
            } else {
                encodeCid = URLEncoder.encode(object.toString(), "utf-8");
            }
            return encodeCid;
        } catch (Exception e) {
            return null;
        }
    }

    @SafeVarargs
    private String encodeParams(Pair<String, String>... params) {
        try {
            JSONObject object = new JSONObject();
            for (Pair<String, String> param : params) {
                object.put(param.first, param.second);
            }
            String encodeCid;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                encodeCid = URLEncoder.encode(object.toString(), StandardCharsets.UTF_8);
            } else {
                encodeCid = URLEncoder.encode(object.toString(), "utf-8");
            }
            return encodeCid;
        } catch (Exception e) {
            return null;
        }
    }

    private Intent getIntent(String command) {
        return getIntentWithParams(command, null);
    }

    private Intent getIntentWithParams(String command, String params) {
        StringBuilder deeplink = new StringBuilder("iqiyi://com.qiyi.video.iv/v1/app?command=").append(command);
        if (!TextUtils.isEmpty(params)) {
            deeplink.append("&param=").append(params);
        }
        Uri uri = Uri.parse(deeplink.toString());
        Timber.tag(TAG).d("getIntentWithParams: %s", deeplink.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
