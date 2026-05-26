package cn.booslink.llm.processor.process.weather;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.UIResponse;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.Category;

public interface IWeatherProcess {

    boolean shouldWeatherProcess(Category category);

    VoiceResult handleWeatherIntent(UIResponse response, @NotNull List<Slot> slots);
}
