package cn.booslink.llm.processor.process.drama;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public interface IDramaProcess {

    boolean shouldDramaProcess(Category category, AIUIIntent intent);

    boolean handleDramaIntent(AIUIIntent intent, @NotNull List<Slot> slots);
}
