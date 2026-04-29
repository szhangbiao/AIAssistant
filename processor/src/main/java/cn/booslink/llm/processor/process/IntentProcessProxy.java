package cn.booslink.llm.processor.process;

import android.content.Context;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Semantic;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.downloader.utils.PkgUtils;
import cn.booslink.llm.processor.process.app.IAppProcess;
import cn.booslink.llm.processor.process.control.IControlProcess;
import cn.booslink.llm.processor.process.ksong.IKSongProcess;
import cn.booslink.llm.processor.process.music.IMusicProcess;
import cn.booslink.llm.processor.process.video.IVideoProcess;
import cn.booslink.llm.processor.process.volume.IVolumeProcess;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class IntentProcessProxy implements IIntentProcess {

    private Context mContext;
    private final IAppProcess mAppProcess;
    private final IVolumeProcess mVolumeProcess;
    private final IControlProcess mControlProcess;
    private final IMusicProcess mMusicProcess;
    private final IVideoProcess mVideoProcess;
    private final IKSongProcess mKSongProcess;

    @Inject
    public IntentProcessProxy(@ApplicationContext Context context, IAppProcess appProcess, IControlProcess controlProcess, IVolumeProcess volumeProcess, IMusicProcess musicProcess, IVideoProcess videoProcess, IKSongProcess kSongProcess) {
        this.mContext = context;
        this.mAppProcess = appProcess;
        this.mMusicProcess = musicProcess;
        this.mVolumeProcess = volumeProcess;
        this.mControlProcess = controlProcess;
        this.mVideoProcess = videoProcess;
        this.mKSongProcess = kSongProcess;
    }

    @Override
    public void processIntent(Category category, @Nullable List<Semantic> semantics) {
        if (semantics == null || semantics.isEmpty()) return;
        for (Semantic semantic : semantics) {
            boolean handled = processIntent(category, semantic);
        }
    }

    private boolean processIntent(Category category, Semantic semantic) {
        AIUIIntent intent = semantic.getIntent();
        if (intent == null) return false;
        String foregroundPkgName = PkgUtils.getForegroundPkgName(mContext);
        if (mMusicProcess.shouldMusicProcess(foregroundPkgName, category, intent))
            return mMusicProcess.handleMusicIntent(foregroundPkgName, intent, semantic.getSlots());
        if (mVideoProcess.shouldVideoProcess(foregroundPkgName, category, intent))
            return mVideoProcess.handleVideoIntent(foregroundPkgName, intent, semantic.getSlots());
        if (mKSongProcess.shouldKSongProcess(foregroundPkgName, category, intent))
            return mKSongProcess.handleKSongIntent(foregroundPkgName, intent, semantic.getSlots());
        switch (intent) {
            case EXIT:
                mControlProcess.speechSleep();
                return true;
            case VOLUME_MAX:
            case VOLUME_MIN:
            case VOLUME_PLUS:
            case VOLUME_MINUS:
            case MUTE:
            case UNMUTE:
                mVolumeProcess.volumeControl(intent, semantic.getSlots());
                return true;
            case LAUNCH:
            case DOWNLOAD:
            case INSTALL:
                return mAppProcess.handleAppIntent(intent, semantic.getSlots());
        }
        return false;
    }
}
