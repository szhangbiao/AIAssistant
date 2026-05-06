package cn.booslink.llm.processor.process.volume;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

public class VolumeProcessImpl implements IVolumeProcess {

    private static final String TAG = "VolumeProcess";

    private final AudioManager mAudioManager;

    private final int mMaxVolume;
    private final int mMinVolume;

    @Inject
    public VolumeProcessImpl(@ApplicationContext Context context) {
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        this.mMinVolume = getMinVolumeCompat();
    }

    @Override
    public boolean shouldVolumeProcess(Category category, AIUIIntent intent) {
        return category == Category.CONTROL && (intent == AIUIIntent.VOLUME_MAX ||
                intent == AIUIIntent.VOLUME_MIN ||
                intent == AIUIIntent.VOLUME_PLUS ||
                intent == AIUIIntent.VOLUME_MINUS ||
                intent == AIUIIntent.MUTE ||
                intent == AIUIIntent.UNMUTE
        );
    }

    @Override
    public VoiceResult handleVolumeIntent(AIUIIntent intent, @Nullable List<Slot> slots) {
        switch (intent) {
            case VOLUME_MAX:
            case VOLUME_MIN:
                return volumeMaxOrMin(intent == AIUIIntent.VOLUME_MAX);
            case VOLUME_PLUS:
            case VOLUME_MINUS:
                int volumeNum = slots != null && !slots.isEmpty() ? parseSlotValue(slots) : 1;
                return volumeChange(intent == AIUIIntent.VOLUME_PLUS ? volumeNum : -volumeNum);
            case MUTE:
            case UNMUTE:
                return volumeMuteOrUnmute(intent == AIUIIntent.MUTE);
        }
        return VoiceResult.Companion.failure();
    }

    private VoiceResult volumeChange(int volumeNum) {
        int currentVolume = getVolume();
        if ((currentVolume == mMaxVolume && volumeNum > 0) || (currentVolume == mMinVolume && volumeNum < 0)) {
            return VoiceResult.Companion.success("当前已是" + (volumeNum > 0 ? "最大" : "最小") + "音量");
        }
        int newVolume = currentVolume + volumeNum;
        if (newVolume < mMinVolume) {
            newVolume = mMinVolume;
        } else if (newVolume > mMaxVolume) {
            newVolume = mMaxVolume;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        Timber.tag(TAG).d("volumeChange, num = %d, volume = %d", volumeNum, getVolume());
        return VoiceResult.Companion.success("已为你" + (volumeNum > 0 ? "增大" : "减小") + "音量");
    }

    private VoiceResult volumeMaxOrMin(boolean isVolumeMax) {
        int currentVolume = getVolume();
        if ((currentVolume == mMaxVolume && isVolumeMax) || (currentVolume == mMinVolume && !isVolumeMax)) {
            return VoiceResult.Companion.success("当前已是" + (isVolumeMax ? "最大" : "最小") + "音量");
        }
        int targetVolume = isVolumeMax ? mMaxVolume : mMinVolume;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0);
        Timber.tag(TAG).d("volumeMaxOrMin, volume = %d", getVolume());
        return VoiceResult.Companion.success("已调整音量为" + (isVolumeMax ? "最大" : "最小"));
    }

    private VoiceResult volumeMuteOrUnmute(boolean isMute) {
        int currentVolume = getVolume();
        if (isMute) {
            if (currentVolume == mMinVolume) {
                return VoiceResult.Companion.success("当前已是静音");
            }
            // For STREAM_MUSIC, mute means setting volume to 0
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        } else {
            // Unmute - restore to a reasonable volume (50% of max)
            if (currentVolume != mMinVolume) {
                return VoiceResult.Companion.success("当前不是静音状态");
            }
            int restoreVolume = mMaxVolume / 2;
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, restoreVolume, 0);
        }
        Timber.tag(TAG).d("volumeMuteOrUnmute, volume = %d", getVolume());
        return VoiceResult.Companion.success(isMute ? "已静音" : "已解除静音");
    }

    private int parseSlotValue(List<Slot> slots) {
        for (Slot slot : slots) {
            String value = slot.getValue();
            if (!TextUtils.isEmpty(value)) {
                return tryParseIntNum(value);
            }
        }
        return 1;
    }

    private int tryParseIntNum(String value) {
        int intNum;
        try {
            intNum = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            intNum = 1;
        }
        return intNum;
    }

    private int getVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Compatible method to get minimum volume
     * getStreamMinVolume() is only available since API 28
     */
    private int getMinVolumeCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // API 28+ - use the official method
            return mAudioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
        } else {
            // Pre-API 28 - assume minimum volume is 0 for STREAM_MUSIC
            // For most devices, STREAM_MUSIC minimum volume is 0
            return 0;
        }
    }
}
