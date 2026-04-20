package cn.booslink.llm.processor.process.app;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;

public interface IAppProcess {
    boolean handleAppAction(AIUIIntent intent, @NotNull List<Slot> slots);
}
