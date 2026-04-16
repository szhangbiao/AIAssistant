package cn.booslink.llm.processor.process.volume;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;

public interface IVolumeProcess {
    void volumeControl(AIUIIntent intent,@Nullable List<Slot> slots);
}
