package cn.booslink.llm.processor.process;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import cn.booslink.llm.common.model.Semantic;
import cn.booslink.llm.common.model.UIResponse;
import cn.booslink.llm.common.model.VoiceResult;

public interface IIntentProcess {

    VoiceResult processIntent(@Nullable UIResponse response, @Nullable List<Semantic> semantic);
}
