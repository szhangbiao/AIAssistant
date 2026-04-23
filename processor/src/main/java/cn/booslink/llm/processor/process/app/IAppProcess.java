package cn.booslink.llm.processor.process.app;

import android.content.Intent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;

public interface IAppProcess {

    boolean handleAppIntent(AIUIIntent intent, @NotNull List<Slot> slots);

    void launchAppWithIntent(String pkgName, @Nullable Intent intent);
}
