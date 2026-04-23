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
    private static final String SMART_PACKAGE_NAME = "com.huiaichang.sdm.desktop";

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
        return category == Category.KSONG || (category == Category.CONTROL && (
                intent == AIUIIntent.RESUME_PLAY || intent == AIUIIntent.PAUSE // TODO
        ));
    }

    @Override
    public boolean handleKSongIntent(AIUIIntent intent, @NotNull List<Slot> slots) {
        Intent actionIntent = getActualIntent(intent, slots);
        if (actionIntent != null) {
            mAppProcess.launchAppWithIntent(getTargetPkgName(), actionIntent);
            return true;
        }
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
            // TODO other action
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
                return mSmartActionLazy.get();
            case BOOSLINK_QM_PACKAGE_NAME:
                return mBslQmActionLazy.get();
        }
        return null;
    }

    private String getTargetPkgName() {
        String foregroundPackage = PkgUtils.getForegroundPkgName(mContext);
        if (TextUtils.isEmpty(foregroundPackage)) {
            return BOOSLINK_QM_PACKAGE_NAME;
        }
        switch (foregroundPackage) {
            case QUANMIN_PACKAGE_NAME:
                return QUANMIN_PACKAGE_NAME;
            case DUO_CHANG_PACKAGE_NAME:
                return DUO_CHANG_PACKAGE_NAME;
            case SMART_PACKAGE_NAME:
                return SMART_PACKAGE_NAME;
            case BOOSLINK_QM_PACKAGE_NAME:
                return BOOSLINK_QM_PACKAGE_NAME;
        }
        return null;
    }
}
