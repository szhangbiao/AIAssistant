package cn.booslink.llm.common.di;

import cn.booslink.llm.common.image.ImageLoader;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;

@EntryPoint
@InstallIn(SingletonComponent.class)
public interface CommonEntryPoint {

    OkHttpClient okHttpClient();

    ImageLoader imageLoader();
}
