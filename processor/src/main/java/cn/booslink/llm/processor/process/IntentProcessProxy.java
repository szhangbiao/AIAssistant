package cn.booslink.llm.processor.process;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Semantic;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.processor.process.control.IControlProcess;
import cn.booslink.llm.processor.process.volume.IVolumeProcess;

public class IntentProcessProxy implements IIntentProcess {

    private final IVolumeProcess mVolumeProcess;
    private final IControlProcess mControlProcess;

    @Inject
    public IntentProcessProxy(IControlProcess controlProcess, IVolumeProcess volumeProcess) {
        this.mVolumeProcess = volumeProcess;
        this.mControlProcess = controlProcess;
    }

    @Override
    public void processIntent(Category category, @Nullable List<Semantic> semantics) {
        if (semantics == null || semantics.isEmpty()) return;
        for (Semantic semantic : semantics) {
            boolean handled = processIntent(category, semantic);
        }
    }

    private boolean processIntent(Category category, Semantic semantic) {
        AIUIIntent intent = semantic.getIntent();
        if (intent == null) return false;
        switch (intent) {
            case EXIT:
                mControlProcess.speechSleep();
                return true;
            case VOLUME_MAX:
            case VOLUME_MIN:
            case VOLUME_PLUS:
            case VOLUME_MINUS:
            case MUTE:
            case UNMUTE:
                mVolumeProcess.volumeControl(intent, semantic.getSlots());
                return true;
        }
        return false;
    }
}
