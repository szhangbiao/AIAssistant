package cn.booslink.llm.processor.process;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.DeviceInfo;
import cn.booslink.llm.common.model.Semantic;
import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.UIResponse;
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
import cn.booslink.llm.processor.process.weather.IWeatherProcess;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

public class IntentProcessProxy implements IIntentProcess {

    private static final String TAG = "IntentProcess";


    private static final String KEY_OPERATE_OBJECT = "operateObject";
    private static final String NAME_SYSTEM_SETTING = "系统设置";

    private final Context mContext;
    private final DeviceInfo mDevice;
    private final IAppProcess mAppProcess;
    private final IVolumeProcess mVolumeProcess;
    private final IControlProcess mControlProcess;
    private final IMusicProcess mMusicProcess;
    private final IVideoProcess mVideoProcess;
    private final IKSongProcess mKSongProcess;
    private final IWeatherProcess mWeatherProcess;
    private final ISpeechInteraction mSpeechInteraction;

    @Inject
    public IntentProcessProxy(@ApplicationContext Context context, DeviceInfo deviceInfo, IAppProcess appProcess, IControlProcess controlProcess, IVolumeProcess volumeProcess, IMusicProcess musicProcess, IVideoProcess videoProcess, IKSongProcess kSongProcess, IWeatherProcess weatherProcess, ISpeechInteraction speechInteraction) {
        this.mContext = context;
        this.mDevice = deviceInfo;
        this.mAppProcess = appProcess;
        this.mKSongProcess = kSongProcess;
        this.mMusicProcess = musicProcess;
        this.mVideoProcess = videoProcess;
        this.mVolumeProcess = volumeProcess;
        this.mControlProcess = controlProcess;
        this.mWeatherProcess = weatherProcess;
        this.mSpeechInteraction = speechInteraction;
    }

    @Override
    public VoiceResult processIntent(UIResponse response, @Nullable List<Semantic> semantics) {
        if (semantics == null || semantics.isEmpty() || response.getCategory() == Category.UNKNOWN) return VoiceResult.Companion.failure();
        Timber.tag(TAG).d("processIntent, semantic count = %d", semantics.size());
        for (Semantic semantic : semantics) {
            VoiceResult handleResult = processIntent(response, semantic);
            if (handleResult != null && handleResult.getHandled()) {
                if (!handleResult.getIgnoreNlpResponse()) {
                    mSpeechInteraction.customAnswer(handleResult.getResponseText());
                }
                return handleResult;
            }
        }
        return VoiceResult.Companion.failure();
    }

    private VoiceResult processIntent(UIResponse response, Semantic semantic) {
        AIUIIntent intent = semantic.getIntent();
        if (intent == null) return VoiceResult.Companion.failure();
        String foregroundPkgName = PkgUtils.getForegroundPkgName(mContext);
        Category category = response.getCategory();
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
        } else if (mWeatherProcess.shouldWeatherProcess(category)) {
            return mWeatherProcess.handleWeatherIntent(response, semantic.getSlots());
        }
        switch (intent) {
            case EXIT:
            case EXIT_AGENT:
            case SETTING_CLOSE:
                mControlProcess.speechSleep();
                return VoiceResult.Companion.success("好的");
            case LAUNCH:
                String name = parseNameBySlots(semantic.getSlots());
                if (category == Category.CONTROL && NAME_SYSTEM_SETTING.equals(name)) {
                    openSystemSetting();
                    return VoiceResult.Companion.success("好的");
                }
                break;
            case AGENT_VERSION:
                String versionTxt = buildVersionTxt();
                return VoiceResult.Companion.success(versionTxt);
            case AGENT_GUIDE:
                openGuidePage();
                return VoiceResult.Companion.success("好的");
            case BACK_HOME:
                simulateHomePress();
                return VoiceResult.Companion.success("好的");
        }
        return VoiceResult.Companion.failure();
    }

    private String buildVersionTxt() {
        return "应用版本号：\n" + mDevice.getVersionCode() + "\n\n应用版本名：\n" + mDevice.getVersionName();
    }

    private String parseNameBySlots(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return null;
        for (Slot slot : slots) {
            if (KEY_OPERATE_OBJECT.equals(slot.getName())) {
                return slot.getValue();
            }
        }
        return null;
    }

    private void openSystemSetting() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        PkgUtils.launchIntent(mContext, intent);
    }

    private void openGuidePage() {
        Intent intent = new Intent();
        intent.setClassName(mContext, "cn.booslink.llm.ui.GuideActivity");
        PkgUtils.launchIntent(mContext, intent);
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
