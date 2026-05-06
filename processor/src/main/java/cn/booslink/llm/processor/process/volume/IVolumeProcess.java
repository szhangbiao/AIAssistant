package cn.booslink.llm.processor.process.volume;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public interface IVolumeProcess {

    boolean shouldVolumeProcess(Category category, AIUIIntent intent);

    VoiceResult handleVolumeIntent(AIUIIntent intent, @Nullable List<Slot> slots);
}
