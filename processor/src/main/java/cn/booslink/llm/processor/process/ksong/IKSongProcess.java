package cn.booslink.llm.processor.process.ksong;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public interface IKSongProcess {
    boolean shouldKSongProcess(String foregroundPkgName, Category category, AIUIIntent intent);

    boolean handleKSongIntent(String foregroundPkgName, AIUIIntent intent, @NotNull List<Slot> slots);
}
