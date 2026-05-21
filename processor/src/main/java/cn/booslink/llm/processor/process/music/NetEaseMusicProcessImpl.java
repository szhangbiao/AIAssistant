package cn.booslink.llm.processor.process.music;

import android.content.Intent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.processor.process.app.IAppProcess;

public class NetEaseMusicProcessImpl implements IMusicProcess {
    private static final String NETEASE_PACKAGE_NAME = "com.netease.cloudmusic.tv";

    private static final String MUSIC_ARTIST = "artist";
    private static final String MUSIC_SONG = "song";

    private final IAppProcess mAppProcess;

    @Inject
    public NetEaseMusicProcessImpl(IAppProcess appProcess) {
        this.mAppProcess = appProcess;
    }

    @Override
    public boolean shouldMusicProcess(String foregroundPkgName, Category category, AIUIIntent intent) {
        return category == Category.MUSIC && (intent == AIUIIntent.RANDOM_SEARCH || intent == AIUIIntent.PLAY);
    }

    @Override
    public VoiceResult handleMusicIntent(String foregroundPkgName, AIUIIntent aiuiIntent, @NotNull List<Slot> slots) {
        boolean isNetEaseStartup = NETEASE_PACKAGE_NAME.equals(foregroundPkgName);
        if ((aiuiIntent == AIUIIntent.RANDOM_SEARCH || aiuiIntent == AIUIIntent.PLAY) && isNetEaseStartup) {
            return VoiceResult.Companion.success("当前已处于应用内");
        }
        switch (aiuiIntent) {
            case RANDOM_SEARCH:
                mAppProcess.launchAppWithIntent(NETEASE_PACKAGE_NAME, null);
                return VoiceResult.Companion.progress();
            case PLAY:
                Intent intent = getSupportActionBySlot(slots);
                mAppProcess.launchAppWithIntent(NETEASE_PACKAGE_NAME, intent);
                return VoiceResult.Companion.progress();
        }
        return VoiceResult.Companion.failure();
    }

    private Intent getSupportActionBySlot(@NotNull List<Slot> slots) {
        // TODO 根据 slot 里的值获取填充数据的Intent
        return null;
    }
}
