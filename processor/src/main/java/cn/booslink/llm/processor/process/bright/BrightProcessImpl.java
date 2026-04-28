package cn.booslink.llm.processor.process.bright;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;

public class BrightProcessImpl implements IBrightProcess {

    @Override
    public boolean shouldBrightProcess(Category category, AIUIIntent intent) {
        return false;
    }

    @Override
    public boolean handleBrightIntent(AIUIIntent intent, @NotNull List<Slot> slots) {
        return false;
    }
}
