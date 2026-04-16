package cn.booslink.llm.processor.process;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import cn.booslink.llm.common.model.Semantic;
import cn.booslink.llm.common.model.enums.Category;

public interface IIntentProcess {

    void processIntent(@Nullable Category category, @Nullable List<Semantic> semantic);
}
