package cn.booslink.llm.processor.process.bright;

import androidx.annotation.Nullable;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public interface IBrightProcess {
    boolean shouldBrightProcess(Category category, AIUIIntent intent);
    VoiceResult handleBrightIntent(AIUIIntent intent, @Nullable List<Slot> slots);
}
