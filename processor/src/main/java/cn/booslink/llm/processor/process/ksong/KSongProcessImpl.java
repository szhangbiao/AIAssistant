package cn.booslink.llm.processor.process.ksong;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.processor.process.app.IAppProcess;
import dagger.Lazy;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class KSongProcessImpl implements IKSongProcess {

    private static final String BOOSLINK_QM_PACKAGE_NAME = "cn.booslink.kg";
    private static final String DUO_CHANG_PACKAGE_NAME = "com.evideo.kmbox";
    private static final String QUANMIN_PACKAGE_NAME = "com.tencent.karaoketv";
    private static final String LEISHI_PACKAGE_NAME = "cn.jmake.karaoke.box.ott";
    private static final String SMART_PACKAGE_NAME = "com.huiaichang.sdm.desktop";
    private static final String LEIKA_PACKAGE_NAME = "com.huiaichang.mars.desktop";

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
        boolean isKSongAppStartup = BOOSLINK_QM_PACKAGE_NAME.equals(foregroundPkgName) || DUO_CHANG_PACKAGE_NAME.equals(foregroundPkgName) || QUANMIN_PACKAGE_NAME.equals(foregroundPkgName) || SMART_PACKAGE_NAME.equals(foregroundPkgName) || LEIKA_PACKAGE_NAME.equals(foregroundPkgName);
        return (isKSongAppStartup && category == Category.CONTROL && (
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
        )) || (isKSongAppStartup && category == Category.KSONG && (
                intent == AIUIIntent.RANDOM_KSONG ||// 打开应用
                        intent == AIUIIntent.KSONG_ORIGIN ||// 原唱
                        intent == AIUIIntent.KSONG_ACCOM ||// 伴唱
                        intent == AIUIIntent.KSONG_ADD ||// 点歌
                        intent == AIUIIntent.KSONG_REMOVE ||// 移除点歌
                        intent == AIUIIntent.KSONG_TOP ||// 置顶
                        intent == AIUIIntent.OPEN_SCORE ||// 打开评分
                        intent == AIUIIntent.CLOSE_SCORE // 关闭评分);
        ));
    }

    @Override
    public boolean handleKSongIntent(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots) {
        if (intent == AIUIIntent.RANDOM_KSONG || intent == AIUIIntent.KSONG_ADD) {
            return populateKSongEntryPoint(foregroundPkgName);
        }
        Intent actionIntent = getActualIntent(foregroundPkgName, intent, slots);
        if (actionIntent != null) {
            populateKSongIntent(foregroundPkgName, actionIntent);
            return true;
        }
        return false;
    }

    private boolean populateKSongEntryPoint(String foregroundPkgName) {
        boolean isKSongAppStartup = BOOSLINK_QM_PACKAGE_NAME.equals(foregroundPkgName) || DUO_CHANG_PACKAGE_NAME.equals(foregroundPkgName) || QUANMIN_PACKAGE_NAME.equals(foregroundPkgName) || SMART_PACKAGE_NAME.equals(foregroundPkgName) || LEIKA_PACKAGE_NAME.equals(foregroundPkgName);
        if (!isKSongAppStartup) {
            mAppProcess.launchAppWithIntent(BOOSLINK_QM_PACKAGE_NAME, null);
            return true;
        }
        // TODO ksong app already startup
        return false;
    }

    private Intent getActualIntent(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots) {
        IKSongAction songAction = getKSongAction(foregroundPkgName);
        if (songAction == null) return null;
        switch (intent) {
            case RESUME_PLAY:
                return songAction.play();
            case PAUSE:
                return songAction.pause();
            case CHOOSE_NEXT:
                // TODO
                return null;
            case REPLAY:
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
                return songAction.closePage();
            case PAGE_OPEN:
                return getPageIntentBySlot(foregroundPkgName, slots);
            case KSONG_ORIGIN:
                return songAction.originTrack();
            case KSONG_ACCOM:
                return songAction.accompanyTrack();
            case KSONG_ADD:
                // TODO
                return null;// songAction.addSong(slots.get(0).getValue(), slots.get(1).getValue(), false);
            case KSONG_REMOVE:
                return null;
            case KSONG_TOP:
                // TODO
                return null;// songAction.topSong(slots.get(0).getValue(), slots.get(1).getValue());
            case OPEN_SCORE:
                return songAction.openScore();
            case CLOSE_SCORE:
                return songAction.closeScore();
        }
        return null;
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
            case BOOSLINK_QM_PACKAGE_NAME:
                return mBslQmActionLazy.get();
        }
        return null;
    }

    private void populateKSongIntent(String foregroundPkgName, Intent actionIntent) {
        if (QUANMIN_PACKAGE_NAME.equals(foregroundPkgName)) {
            mContext.sendBroadcast(actionIntent);
        } else if (!DUO_CHANG_PACKAGE_NAME.equals(foregroundPkgName)) {
            mContext.startActivity(actionIntent);
        }
    }

    private int getChooseNumBySlot(@NotNull List<Slot> slots) {
        for (Slot slot : slots) {
            if ("number".equals(slot.getName())) {
                return tryParseIntNum(slot.getNormValue());
            }
        }
        return 0;
    }

    private Intent getPageIntentBySlot(String foregroundPkgName, @NotNull List<Slot> slots) {
        for (Slot slot : slots) {
            if ("page".equals(slot.getName())) {
                if (TextUtils.isEmpty(slot.getValue())) return null;
                IKSongAction songAction = getKSongAction(foregroundPkgName);
                if (songAction == null) return null;
                switch (slot.getValue()) {
                    case "收藏":
                        return songAction.openFavorite();
                    case "最近播放":
                        return songAction.openRecent();
                    case "本地":
                        return songAction.openLocal();
                    case "常唱":
                        return songAction.openFrequent();
                }
            }
        }
        return null;
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
}
