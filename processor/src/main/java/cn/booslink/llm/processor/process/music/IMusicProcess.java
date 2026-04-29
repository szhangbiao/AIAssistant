package cn.booslink.llm.processor.process.music;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public interface IMusicProcess {
    boolean shouldMusicProcess(String foregroundPkgName, Category category, AIUIIntent intent);
    boolean handleMusicIntent(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots);
}
