package cn.booslink.llm.processor.process;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Semantic;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.processor.process.control.IControlProcess;

public class IntentProcessProxy implements IIntentProcess {

    private final IControlProcess mControlProcess;

    @Inject
    public IntentProcessProxy(IControlProcess controlProcess) {
        this.mControlProcess = controlProcess;
    }

    @Override
    public void processIntent(Category category, @Nullable List<Semantic> semantics) {
        if (semantics == null || semantics.isEmpty()) return;
        for (Semantic semantic : semantics) {
            if (semantic.getIntent() != null) {
                boolean handled = processIntent(category, semantic.getIntent());
            }
        }
    }

    private boolean processIntent(Category category, AIUIIntent intent) {
        switch (intent) {
            case EXIT:
                mControlProcess.speechSleep();
                return true;
        }
        return false;
    }
}
