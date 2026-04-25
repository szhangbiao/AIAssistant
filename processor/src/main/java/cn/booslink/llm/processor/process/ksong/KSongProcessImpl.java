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
import cn.booslink.llm.downloader.utils.PkgUtils;
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
    public boolean shouldKSongProcess(Category category, AIUIIntent intent) {
        String foregroundPackage = PkgUtils.getForegroundPkgName(mContext);
        boolean isKSongAppStartup = BOOSLINK_QM_PACKAGE_NAME.equals(foregroundPackage) || DUO_CHANG_PACKAGE_NAME.equals(foregroundPackage) || QUANMIN_PACKAGE_NAME.equals(foregroundPackage) || SMART_PACKAGE_NAME.equals(foregroundPackage) || LEIKA_PACKAGE_NAME.equals(foregroundPackage);
        return category == Category.KSONG || (isKSongAppStartup && category == Category.CONTROL && (
                intent == AIUIIntent.RESUME_PLAY || // 播放
                        intent == AIUIIntent.PAUSE ||// 暂停
                        //intent == AIUIIntent.XXX ||// 原唱
                        //intent == AIUIIntent.XXX ||// 伴唱
                        intent == AIUIIntent.CHOOSE_NEXT ||// 下一曲, 下一页
                        intent == AIUIIntent.REPLAY ||// 重播
                        //intent == AIUIIntent.XXX ||// 点歌
                        //intent == AIUIIntent.XXX ||// 移除点歌
                        //intent == AIUIIntent.XXX ||// 置顶
                        //intent == AIUIIntent.XXX ||// 打开评分
                        //intent == AIUIIntent.XXX ||// 关闭评分
                        intent == AIUIIntent.SCREEN_FULL ||// 全屏
                        intent == AIUIIntent.EXIT_SCREEN_FULL ||// 退出全屏
                        intent == AIUIIntent.PLAYLIST_OPEN || // 打开播放列表
                        intent == AIUIIntent.CHOOSE_WHICH || // 选择第几首
                        intent == AIUIIntent.CHOOSE_PREVIOUS || // 上一页
                        intent == AIUIIntent.EXIT // 关闭当前页
                //intent == AIUIIntent.XXX || // 打开最近播放
                //intent == AIUIIntent.XXX || // 打开收藏
                //intent == AIUIIntent.XXX || // 打开本地
                //intent == AIUIIntent.XXX || // 打开常唱
        ));
    }

    @Override
    public boolean handleKSongIntent(AIUIIntent intent, @NotNull List<Slot> slots) {
        if (intent == AIUIIntent.RANDOM_KSONG) {
            return populateKSongEntryPoint();
        }
        Intent actionIntent = getActualIntent(intent, slots);
        if (actionIntent != null) {
            populateKSongIntent(actionIntent);
            return true;
        }
        return false;
    }

    private boolean populateKSongEntryPoint() {
        String foregroundPackage = PkgUtils.getForegroundPkgName(mContext);
        boolean isKSongAppStartup = BOOSLINK_QM_PACKAGE_NAME.equals(foregroundPackage) || DUO_CHANG_PACKAGE_NAME.equals(foregroundPackage) || QUANMIN_PACKAGE_NAME.equals(foregroundPackage) || SMART_PACKAGE_NAME.equals(foregroundPackage) || LEIKA_PACKAGE_NAME.equals(foregroundPackage);
        if (!isKSongAppStartup) {
            mAppProcess.launchAppWithIntent(BOOSLINK_QM_PACKAGE_NAME, null);
            return true;
        }
        // TODO ksong app already startup
        return false;
    }

    private Intent getActualIntent(AIUIIntent intent, @NotNull List<Slot> slots) {
        IKSongAction songAction = getKSongAction();
        if (songAction == null) return null;
        switch (intent) {
            case RESUME_PLAY:
                return songAction.play();
            case PAUSE:
                return songAction.pause();
            case CHOOSE_NEXT:
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
            case EXIT:
                return songAction.closePage();
        }
        return null;
    }

    @Nullable
    private IKSongAction getKSongAction() {
        String foregroundPackage = PkgUtils.getForegroundPkgName(mContext);
        if (TextUtils.isEmpty(foregroundPackage)) {
            return mBslQmActionLazy.get();
        }
        switch (foregroundPackage) {
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

    private void populateKSongIntent(Intent actionIntent) {
        String foregroundPackage = PkgUtils.getForegroundPkgName(mContext);
        if (QUANMIN_PACKAGE_NAME.equals(foregroundPackage)) {
            mContext.sendBroadcast(actionIntent);
        } else if (!DUO_CHANG_PACKAGE_NAME.equals(foregroundPackage)) {
            mContext.startActivity(actionIntent);
        }
    }

    private int getChooseNumBySlot(@NotNull List<Slot> slots) {
        // TODO
        return 0;
    }
}
