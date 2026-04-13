package cn.booslink.llm.di;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import cn.booslink.llm.common.image.GlideOptionsConfig;

@GlideModule
public class GlideAppModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        GlideOptionsConfig.applyCommonOptions(context, builder);
    }
}
