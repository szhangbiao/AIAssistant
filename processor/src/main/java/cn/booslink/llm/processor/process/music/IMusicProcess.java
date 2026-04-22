package cn.booslink.llm.processor.process.music;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;

public interface IMusicProcess {
    boolean handleMusicIntent(AIUIIntent intent, @NotNull List<Slot> slots);
}
