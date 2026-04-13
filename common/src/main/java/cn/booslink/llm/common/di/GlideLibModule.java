package cn.booslink.llm.common.di;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.LibraryGlideModule;

import java.io.InputStream;

import dagger.hilt.android.EntryPointAccessors;
import okhttp3.OkHttpClient;

@GlideModule
public class GlideLibModule extends LibraryGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        Context appContext = context.getApplicationContext();
        CommonEntryPoint hiltEntryPoint = EntryPointAccessors.fromApplication(appContext, CommonEntryPoint.class);
        OkHttpClient okHttpClient = hiltEntryPoint.okHttpClient();
        // 使用 OkHttpClient 构建 Glide 的 HttpUrlLoader
        OkHttpUrlLoader.Factory loadFactory = new OkHttpUrlLoader.Factory(okHttpClient);
        // 如果想对OkHttp做更多设置可以在这里覆盖
        registry.replace(GlideUrl.class, InputStream.class, loadFactory);
    }
}
