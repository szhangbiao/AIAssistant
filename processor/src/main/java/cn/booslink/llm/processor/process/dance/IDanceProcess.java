package cn.booslink.llm.processor.process.dance;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public interface IDanceProcess {

    boolean shouldDanceProcess(Category category, AIUIIntent intent);

    boolean handleDanceIntent(AIUIIntent intent, @NotNull List<Slot> slots);
}
