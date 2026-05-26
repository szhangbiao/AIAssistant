package cn.booslink.llm.processor.process.music;

import android.app.Instrumentation;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.processor.process.app.IAppProcess;
import timber.log.Timber;

public class NetEaseMusicProcessImpl implements IMusicProcess {

    private static final String TAG = "MusicProcess";

    private static final String NETEASE_APP_NAME = "网易云音乐";
    private static final String NETEASE_PACKAGE_NAME = "com.netease.cloudmusic.tv";

    private static final String KEY_NAME = "name";
    private static final String MUSIC_ARTIST = "artist";
    private static final String MUSIC_SONG = "song";

    private final IAppProcess mAppProcess;

    @Inject
    public NetEaseMusicProcessImpl(IAppProcess appProcess) {
        this.mAppProcess = appProcess;
    }

    @Override
    public boolean shouldMusicProcess(String foregroundPkgName, Category category, AIUIIntent intent) {
        boolean isAppStartup = NETEASE_PACKAGE_NAME.equals(foregroundPkgName);
        return category == Category.MUSIC && (intent == AIUIIntent.RANDOM_MUSIC || intent == AIUIIntent.MUSIC_ADD) ||
                (isAppStartup && ((category == Category.CONTROL && intent == AIUIIntent.EXIT) || // 退出应用
                        (category == Category.APP && intent == AIUIIntent.EXIT) || // 退出具体应用
                        (category == Category.PAGE_CONTROL && intent == AIUIIntent.PAGE_BACK) || // 关闭当前页 or 返回到上一级页面
                        (category == Category.APP_PLUS && intent == AIUIIntent.EXIT_APP))); // 退出应用
    }

    @Override
    public VoiceResult handleMusicIntent(String foregroundPkgName, AIUIIntent aiuiIntent, @NotNull List<Slot> slots) {
        boolean isNetEaseStartup = NETEASE_PACKAGE_NAME.equals(foregroundPkgName);
        if ((aiuiIntent == AIUIIntent.RANDOM_MUSIC || aiuiIntent == AIUIIntent.MUSIC_ADD) && !isNetEaseStartup) {
            return populateMusicAppLaunch(aiuiIntent, slots);
        }
        switch (aiuiIntent) {
            case EXIT:
            case EXIT_APP:
                boolean isExitMatch = parseNameBySlot(slots);
                if (isExitMatch) {
                    simulateHomePress();
                    return VoiceResult.Companion.success("好的");
                } else {
                    return VoiceResult.Companion.failure();
                }
            case PAGE_BACK:
                simulateBackPress();
                return VoiceResult.Companion.success("好的");
        }
        return VoiceResult.Companion.success("当前已处于应用内");
    }

    private boolean parseNameBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return true;
        String name = null;
        for (Slot slot : slots) {
            if (KEY_NAME.equals(slot.getName())) {
                name = slot.getValue();
                break;
            }
        }
        if (TextUtils.isEmpty(name)) return true;
        return NETEASE_APP_NAME.equalsIgnoreCase(name) || NETEASE_APP_NAME.contains(name);
    }

    private VoiceResult populateMusicAppLaunch(AIUIIntent aiuiIntent, List<Slot> slots) {
        switch (aiuiIntent) {
            case RANDOM_MUSIC:
                mAppProcess.launchAppWithIntent(NETEASE_PACKAGE_NAME, null);
                return VoiceResult.Companion.progress();
            case MUSIC_ADD:
                Intent intent = getSupportActionBySlot(slots);
                mAppProcess.launchAppWithIntent(NETEASE_PACKAGE_NAME, intent);
                return VoiceResult.Companion.progress();
        }
        return VoiceResult.Companion.failure();
    }

    private Intent getSupportActionBySlot(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return null;
        String artist = null;
        String song = null;
        for (Slot slot : slots) {
            if (MUSIC_ARTIST.equals(slot.getName())) {
                artist = slot.getValue();
            } else if (MUSIC_SONG.equals(slot.getName())) {
                song = slot.getValue();
            }
        }
        if (TextUtils.isEmpty(artist) && TextUtils.isEmpty(song)) return null;
        Timber.tag(TAG).d("artist = %s, song = %s", artist, song);
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
}
