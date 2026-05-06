package cn.booslink.llm.processor.process.bright;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class BrightProcessImpl implements IBrightProcess {

    public BrightProcessImpl(@ApplicationContext Context context) {
        
    }

    @Override
    public boolean shouldBrightProcess(Category category, AIUIIntent intent) {
        return category == Category.CONTROL && (intent == AIUIIntent.BRIGHT_MAX ||
                intent == AIUIIntent.BRIGHT_MIN ||
                intent == AIUIIntent.BRIGHT_UP ||
                intent == AIUIIntent.BRIGHT_DOWN);
    }

    @Override
    public VoiceResult handleBrightIntent(AIUIIntent intent, @Nullable List<Slot> slots) {
        switch (intent) {
            case BRIGHT_DOWN:
            case BRIGHT_UP:
                int brightNum = slots != null && !slots.isEmpty() ? parseSlotValue(slots) : 1;
                return brightChange(intent == AIUIIntent.BRIGHT_UP ? brightNum : -brightNum);
            case BRIGHT_MAX:
            case BRIGHT_MIN:
                return brightMaxOrMin(intent == AIUIIntent.BRIGHT_MAX);
        }
        return VoiceResult.Companion.failure();
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

    private VoiceResult brightChange(int brightNum) {
        return null;
    }

    private VoiceResult brightMaxOrMin(boolean isBrightMax) {
        return null;
    }
}
