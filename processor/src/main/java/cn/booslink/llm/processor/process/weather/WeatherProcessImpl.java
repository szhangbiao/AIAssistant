package cn.booslink.llm.processor.process.weather;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.UIResponse;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.common.ui.ISpeechInteraction;

public class WeatherProcessImpl implements IWeatherProcess {

    private static final String QUERY_FOCUS = "subfocus";
    private static final String FOCUS_WEATHER = "天气状态";

    private final ISpeechInteraction mSpeechInteraction;

    @Inject
    public WeatherProcessImpl(ISpeechInteraction speechInteraction) {
        this.mSpeechInteraction = speechInteraction;
    }

    @Override
    public boolean shouldWeatherProcess(Category category) {
        return category == Category.WEATHER;
    }

    @Override
    public VoiceResult handleWeatherIntent(UIResponse response, @NotNull List<Slot> slots) {
        boolean shouldShowWeatherList = parseQueryFocusBySlots(slots);
        if (shouldShowWeatherList) {
            mSpeechInteraction.semanticAnswer(response);
            return VoiceResult.Companion.ignore();
        } else {
            return VoiceResult.Companion.failure();
        }
    }

    private boolean parseQueryFocusBySlots(@NotNull List<Slot> slots) {
        if (slots.isEmpty()) return true;
        for (Slot slot : slots) {
            if (QUERY_FOCUS.equals(slot.getName())) {
                return FOCUS_WEATHER.equals(slot.getValue());
            }
        }
        return true;
    }
}
