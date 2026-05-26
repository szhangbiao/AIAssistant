package cn.booslink.llm.processor.process.ksong;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.processor.process.app.IAppProcess;
import dagger.Lazy;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

public class KSongProcessImpl implements IKSongProcess {

    private static final String TAG = "KSongProcess";

    private static final String BOOSLINK_QM_APP_NAME = "全民K歌";
    private static final String BOOSLINK_QM_PACKAGE_NAME = "cn.booslink.kg";
    private static final String DUO_CHANG_PACKAGE_NAME = "com.evideo.kmbox";
    private static final String QUANMIN_PACKAGE_NAME = "com.tencent.karaoketv";
    private static final String LEISHI_PACKAGE_NAME = "cn.jmake.karaoke.box.ott";
    private static final String SMART_PACKAGE_NAME = "com.huiaichang.sdm.desktop";
    private static final String LEIKA_PACKAGE_NAME = "com.huiaichang.mars.desktop";

    private static final String KEY_NAME = "name";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_PAGE = "page";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_SONG = "song";

    private static final String PAGE_FAVORITE = "收藏";
    private static final String PAGE_RECENT = "最近播放";
    private static final String PAGE_LOCAL = "本地";
    private static final String PAGE_FREQUENT = "常唱";
    private static final String PAGE_PLAYLIST = "播放列表";
    private static final String PAGE_FREQUENT_2 = "尝尝";

    @Inject
    @Named("quanmin")
    Lazy<IKSongAction> mQuanMinActionLazy;
    @Inject
    @Named("duochang")
    Lazy<IKSongAction> mDuoChangActionLazy;
    @Inject
    @Named("smart")
    Lazy<IKSongAction> mSmartActionLazy;
    @Inject
    @Named("bslqm")
    Lazy<IKSongAction> mBslQmActionLazy;

    private final Context mContext;
    private final IAppProcess mAppProcess;

    @Inject
    public KSongProcessImpl(@ApplicationContext Context context, IAppProcess appProcess) {
        this.mContext = context;
        this.mAppProcess = appProcess;
    }

    @Override
    public boolean shouldKSongProcess(String foregroundPkgName, Category category, AIUIIntent intent) {
        boolean isKSongAppStartup = BOOSLINK_QM_PACKAGE_NAME.equals(foregroundPkgName) || DUO_CHANG_PACKAGE_NAME.equals(foregroundPkgName) || QUANMIN_PACKAGE_NAME.equals(foregroundPkgName) || SMART_PACKAGE_NAME.equals(foregroundPkgName) || LEIKA_PACKAGE_NAME.equals(foregroundPkgName) || LEISHI_PACKAGE_NAME.equals(foregroundPkgName);
        return (category == Category.KSONG && (
                intent == AIUIIntent.RANDOM_KSONG ||// 打开应用
                        intent == AIUIIntent.KSONG_ADD// 点歌
        )) || (isKSongAppStartup && category == Category.CONTROL && (
                intent == AIUIIntent.EXIT || // 退出应用
                        intent == AIUIIntent.RESUME_PLAY || // 播放
                        intent == AIUIIntent.PAUSE ||// 暂停
                        intent == AIUIIntent.CHOOSE_NEXT ||// 下一曲, 下一页
                        intent == AIUIIntent.REPLAY ||// 重播
                        intent == AIUIIntent.SCREEN_FULL ||// 全屏
                        intent == AIUIIntent.EXIT_SCREEN_FULL ||// 退出全屏
                        intent == AIUIIntent.PLAYLIST_OPEN || // 打开播放列表
                        intent == AIUIIntent.CHOOSE_WHICH || // 选择第几首
                        intent == AIUIIntent.CHOOSE_PREVIOUS // 上一页
        )) || (isKSongAppStartup && category == Category.PAGE_CONTROL && (
                intent == AIUIIntent.PAGE_OPEN || //打开最近播放,收藏,本地,常唱
                        intent == AIUIIntent.PAGE_BACK // 关闭当前页 or 返回到上一级页面
        )) || (isKSongAppStartup && (
                intent == AIUIIntent.KSONG_ORIGIN || intent == AIUIIntent.CLOSE_ACCOM ||// 原唱
                        intent == AIUIIntent.KSONG_ACCOM || intent == AIUIIntent.CLOSE_ORIGIN || // 伴唱
                        intent == AIUIIntent.KSONG_REPLAY ||// 重唱
                        intent == AIUIIntent.KSONG_ADD ||// 点歌
                        intent == AIUIIntent.KSONG_REMOVE ||// 移除点歌
                        intent == AIUIIntent.KSONG_TOP ||// 置顶
                        intent == AIUIIntent.OPEN_SCORE ||// 打开评分
                        intent == AIUIIntent.CLOSE_SCORE ||// 关闭评分
                        intent == AIUIIntent.EXIT ||// 关闭全民K歌
                        intent == AIUIIntent.EXIT_APP // 关闭应用
        ));
    }

    @Override
    public VoiceResult handleKSongIntent(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots) {
        if (intent == AIUIIntent.RANDOM_KSONG || intent == AIUIIntent.KSONG_ADD) {
            boolean isKSongAppStartup = BOOSLINK_QM_PACKAGE_NAME.equals(foregroundPkgName) || DUO_CHANG_PACKAGE_NAME.equals(foregroundPkgName) || QUANMIN_PACKAGE_NAME.equals(foregroundPkgName) || SMART_PACKAGE_NAME.equals(foregroundPkgName) || LEIKA_PACKAGE_NAME.equals(foregroundPkgName) || LEISHI_PACKAGE_NAME.equals(foregroundPkgName);
            if (!isKSongAppStartup) {
                Intent launchIntent = null;
                if (intent == AIUIIntent.KSONG_ADD) {
                    IKSongAction songAction = getKSongAction(foregroundPkgName);
                    Pair<String, String> addSongPair = parseArtistAndSongBySlot(slots);
                    if (addSongPair != null && songAction != null) {
                        launchIntent = songAction.addSong(addSongPair.first, addSongPair.second, false);
                    }
                }
                mAppProcess.launchAppWithIntent(BOOSLINK_QM_PACKAGE_NAME, launchIntent);
                return VoiceResult.Companion.progress();
            } else if (intent == AIUIIntent.RANDOM_KSONG) {
                return VoiceResult.Companion.success("当前已处于应用内");
            }
        }
        Intent actionIntent = getActualIntent(foregroundPkgName, intent, slots);
        if (actionIntent != null) {
            populateKSongIntent(foregroundPkgName, actionIntent);
            return VoiceResult.Companion.success("好的");
        }
        return VoiceResult.Companion.failure();
    }

    private Intent getActualIntent(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots) {
        IKSongAction songAction = getKSongAction(foregroundPkgName);
        if (songAction == null) return null;
        switch (intent) {
            case EXIT:
            case EXIT_APP:
                boolean isExitMatch = parseNameBySlot(foregroundPkgName, slots);
                if (isExitMatch) {
                    Intent exitIntent = songAction.exit();
                    if (isEmptyIntent(exitIntent)) {
                        simulateHomePress();
                    }
                    return exitIntent;
                }
                break;
            case RESUME_PLAY:
                return songAction.play();
            case PAUSE:
                return songAction.pause();
            case CHOOSE_NEXT:
                return songAction.next();
            case REPLAY:
            case KSONG_REPLAY:
                return songAction.replay();
            case SCREEN_FULL:
                return songAction.fullScreen();
            case EXIT_SCREEN_FULL:
                return songAction.exitFullScreen();
            case PLAYLIST_OPEN:
                return songAction.openPlaylist();
            case CHOOSE_WHICH:
                int num = getChooseNumBySlot(slots);
                return songAction.select(num);
            case CHOOSE_PREVIOUS:
                return songAction.previousPage();
            case PAGE_BACK:
                Intent backIntent = songAction.closePage();
                if (isEmptyIntent(backIntent)) {
                    simulateBackPress();
                }
                return backIntent;
            case PAGE_OPEN:
                return getPageIntentBySlot(foregroundPkgName, slots);
            case KSONG_ORIGIN:
            case CLOSE_ACCOM:
                return songAction.originTrack();
            case KSONG_ACCOM:
            case CLOSE_ORIGIN:
                return songAction.accompanyTrack();
            case KSONG_ADD:
                Pair<String, String> addSongPair = parseArtistAndSongBySlot(slots);
                if (addSongPair == null) return null;
                return songAction.addSong(addSongPair.first, addSongPair.second, false);
            case KSONG_REMOVE:
                // TODO
                return null;
            case KSONG_TOP:
                Pair<String, String> topSongPair = parseArtistAndSongBySlot(slots);
                if (topSongPair == null) return null;
                return songAction.topSong(topSongPair.first, topSongPair.second);
            case OPEN_SCORE:
                return songAction.openScore();
            case CLOSE_SCORE:
                return songAction.closeScore();
        }
        return null;
    }

    private boolean parseNameBySlot(String foregroundPkgName, @NotNull List<Slot> slots) {
        if (slots.isEmpty()) return true;
        String name = null;
        for (Slot slot : slots) {
            if (KEY_NAME.equals(slot.getName())) {
                name = slot.getValue();
                break;
            }
        }
        if (TextUtils.isEmpty(name)) return true;
        String appName = getKSongAppName(foregroundPkgName);
        return !TextUtils.isEmpty(appName) && (appName.equalsIgnoreCase(name) || appName.contains(name));
    }

    private Pair<String, String> parseArtistAndSongBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return null;
        String artist = null;
        String song = null;
        for (Slot slot : slots) {
            if (KEY_ARTIST.equals(slot.getName())) {
                artist = slot.getValue();
            } else if (KEY_SONG.equals(slot.getName())) {
                song = slot.getValue();
            }
        }
        if (TextUtils.isEmpty(artist) && TextUtils.isEmpty(song)) return null;
        return new Pair<>(artist, song);
    }

    @Nullable
    private IKSongAction getKSongAction(String foregroundPkgName) {
        if (TextUtils.isEmpty(foregroundPkgName)) {
            return mBslQmActionLazy.get();
        }
        switch (foregroundPkgName) {
            case QUANMIN_PACKAGE_NAME:
                return mQuanMinActionLazy.get();
            case DUO_CHANG_PACKAGE_NAME:
                return mDuoChangActionLazy.get();
            case SMART_PACKAGE_NAME:
            case LEIKA_PACKAGE_NAME:
                return mSmartActionLazy.get();
//            case LEISHI_PACKAGE_NAME:
//                return null;
            case BOOSLINK_QM_PACKAGE_NAME:
            default:
                return mBslQmActionLazy.get();
        }
    }

    private String getKSongAppName(String foregroundPkgName) {
        if (TextUtils.isEmpty(foregroundPkgName)) {
            return BOOSLINK_QM_APP_NAME;
        }
        switch (foregroundPkgName) {
            case DUO_CHANG_PACKAGE_NAME:
                return "多唱K歌";
            case SMART_PACKAGE_NAME:
            case LEIKA_PACKAGE_NAME:
                return "智能K歌";
            case QUANMIN_PACKAGE_NAME:
            case BOOSLINK_QM_PACKAGE_NAME:
                return BOOSLINK_QM_APP_NAME;
            case LEISHI_PACKAGE_NAME:
                return "雷石K歌";
        }
        return null;
    }

    private void populateKSongIntent(String foregroundPkgName, Intent actionIntent) {
        if (isEmptyIntent(actionIntent)) return;
        if (QUANMIN_PACKAGE_NAME.equals(foregroundPkgName)) {
            mContext.sendBroadcast(actionIntent);
        } else if (!DUO_CHANG_PACKAGE_NAME.equals(foregroundPkgName)) {
            mContext.startActivity(actionIntent);
        }
    }

    private int getChooseNumBySlot(@NotNull List<Slot> slots) {
        for (Slot slot : slots) {
            if (KEY_NUMBER.equals(slot.getName())) {
                return tryParseIntNum(slot.getNormValue());
            }
        }
        return 0;
    }

    private Intent getPageIntentBySlot(String foregroundPkgName, @NotNull List<Slot> slots) {
        for (Slot slot : slots) {
            if (KEY_PAGE.equals(slot.getName())) {
                if (TextUtils.isEmpty(slot.getValue())) return null;
                IKSongAction songAction = getKSongAction(foregroundPkgName);
                if (songAction == null) return null;
                switch (slot.getValue()) {
                    case PAGE_FAVORITE:
                        return songAction.openFavorite();
                    case PAGE_RECENT:
                        return songAction.openRecent();
                    case PAGE_PLAYLIST:
                        return songAction.openPlaylist();
                    case PAGE_LOCAL:
                        return songAction.openLocal();
                    case PAGE_FREQUENT:
                    case PAGE_FREQUENT_2:
                        return songAction.openFrequent();
                }
            }
        }
        return null;
    }

    private void simulateBackPress() {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        } catch (Exception e) {
            // 记录错误日志
            Timber.tag(TAG).e(e, "Failed to simulate back press");
        }
    }

    private void simulateHomePress() {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
        } catch (Exception e) {
            // 记录错误日志
            Timber.tag(TAG).e(e, "Failed to simulate back press");
        }
    }

    private int tryParseIntNum(String value) {
        int intNum;
        try {
            intNum = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            intNum = 0;
        }
        return intNum;
    }

    private boolean isEmptyIntent(Intent intent) {
        if (intent == null) return true;
        // 如果没有action、component或package，基本上就是空Intent
        return TextUtils.isEmpty(intent.getAction()) &&
                intent.getComponent() == null &&
                TextUtils.isEmpty(intent.getPackage());
    }
}
