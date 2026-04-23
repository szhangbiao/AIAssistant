package cn.booslink.llm.processor.process.video;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public interface IVideoProcess {
    boolean shouldVideoProcess(Category category, AIUIIntent intent);
    boolean handleVideoIntent(AIUIIntent intent, @NotNull List<Slot> slots);
}
