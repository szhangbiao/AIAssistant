package cn.booslink.llm.processor.process.dance;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.downloader.utils.PkgUtils;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class DanceProcessImpl implements IDanceProcess {

    private static final String VLT_DANCE_PACKAGE_NAME = "com.newtv.vltdance";

    private final Context mContext;

    public DanceProcessImpl(@ApplicationContext Context context) {
        this.mContext = context;
    }

    @Override
    public boolean shouldDanceProcess(Category category, AIUIIntent intent) {
        String foregroundPackage = PkgUtils.getForegroundPkgName(mContext);
        return category == Category.DANCE || (VLT_DANCE_PACKAGE_NAME.equals(foregroundPackage) && category == Category.CONTROL && (
                intent == AIUIIntent.BRIGHT_DOWN
        ));

    }

    @Override
    public boolean handleDanceIntent(AIUIIntent intent, @NotNull List<Slot> slots) {
        return false;
    }
}
