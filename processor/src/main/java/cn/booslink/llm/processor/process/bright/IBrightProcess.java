package cn.booslink.llm.processor.process.bright;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public interface IBrightProcess {
    boolean shouldBrightProcess(Category category, AIUIIntent intent);
    boolean handleBrightIntent(AIUIIntent intent, @NotNull List<Slot> slots);
}
