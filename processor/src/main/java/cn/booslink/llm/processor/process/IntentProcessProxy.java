package cn.booslink.llm.processor.process;

import android.content.Context;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Semantic;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.downloader.utils.PkgUtils;
import cn.booslink.llm.processor.process.app.IAppProcess;
import cn.booslink.llm.processor.process.control.IControlProcess;
import cn.booslink.llm.processor.process.ksong.IKSongProcess;
import cn.booslink.llm.processor.process.music.IMusicProcess;
import cn.booslink.llm.processor.process.video.IVideoProcess;
import cn.booslink.llm.processor.process.volume.IVolumeProcess;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class IntentProcessProxy implements IIntentProcess {

    private final Context mContext;
    private final IAppProcess mAppProcess;
    private final IVolumeProcess mVolumeProcess;
    private final IControlProcess mControlProcess;
    private final IMusicProcess mMusicProcess;
    private final IVideoProcess mVideoProcess;
    private final IKSongProcess mKSongProcess;
    private final ISpeechInteraction mSpeechInteraction;

    @Inject
    public IntentProcessProxy(@ApplicationContext Context context, IAppProcess appProcess, IControlProcess controlProcess, IVolumeProcess volumeProcess, IMusicProcess musicProcess, IVideoProcess videoProcess, IKSongProcess kSongProcess, ISpeechInteraction speechInteraction) {
        this.mContext = context;
        this.mAppProcess = appProcess;
        this.mKSongProcess = kSongProcess;
        this.mMusicProcess = musicProcess;
        this.mVideoProcess = videoProcess;
        this.mVolumeProcess = volumeProcess;
        this.mControlProcess = controlProcess;
        this.mSpeechInteraction = speechInteraction;
    }

    @Override
    public void processIntent(Category category, @Nullable List<Semantic> semantics) {
        if (semantics == null || semantics.isEmpty()) return;
        for (Semantic semantic : semantics) {
            VoiceResult handleResult = processIntent(category, semantic);
            if (handleResult.getHandled()) {
                mSpeechInteraction.nlpAnswer(handleResult.getResponseText());
            }
        }
    }

    private VoiceResult processIntent(Category category, Semantic semantic) {
        AIUIIntent intent = semantic.getIntent();
        if (intent == null) return VoiceResult.Companion.failure();
        String foregroundPkgName = PkgUtils.getForegroundPkgName(mContext);
        if (mAppProcess.shouldAppProcess(category, intent)) {
            return mAppProcess.handleAppIntent(intent, semantic.getSlots());
        } else if (mMusicProcess.shouldMusicProcess(foregroundPkgName, category, intent)) {
            return mMusicProcess.handleMusicIntent(foregroundPkgName, intent, semantic.getSlots());
        } else if (mVideoProcess.shouldVideoProcess(foregroundPkgName, category, intent)) {
            return mVideoProcess.handleVideoIntent(foregroundPkgName, intent, semantic.getSlots());
        } else if (mKSongProcess.shouldKSongProcess(foregroundPkgName, category, intent)) {
            return mKSongProcess.handleKSongIntent(foregroundPkgName, intent, semantic.getSlots());
        } else if (mVolumeProcess.shouldVolumeProcess(category, intent)) {
            return mVolumeProcess.handleVolumeIntent(intent, semantic.getSlots());
        }
        switch (intent) {
            case EXIT:
                mControlProcess.speechSleep();
                return VoiceResult.Companion.success("好的");
        }
        return VoiceResult.Companion.failure();
    }
}
