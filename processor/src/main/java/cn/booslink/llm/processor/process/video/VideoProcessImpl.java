package cn.booslink.llm.processor.process.video;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public class VideoProcessImpl implements IVideoProcess {

    @Inject
    public VideoProcessImpl() {

    }

    @Override
    public boolean shouldVideoProcess(Category category, AIUIIntent intent) {
        return category == Category.VIDEO || (category == Category.CONTROL && (
                intent == AIUIIntent.PAUSE || intent == AIUIIntent.RESUME_PLAY // TODO
        ));
    }

    @Override
    public boolean handleVideoIntent(AIUIIntent intent, @NotNull List<Slot> slots) {
        return false;
    }
}
