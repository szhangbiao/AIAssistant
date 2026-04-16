package cn.booslink.llm.common.di;

import cn.booslink.llm.common.image.ImageLoader;
import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.common.storage.ISpeechStorage;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import dagger.Lazy;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;

@EntryPoint
@InstallIn(SingletonComponent.class)
public interface CommonEntryPoint {

    OkHttpClient okHttpClient();

    ImageLoader imageLoader();

    ISpeechAgent speechAgent();

    ISpeechStorage speechStorage();

    Lazy<ISpeechAgent> lazySpeechAgent();

    Lazy<ISpeechStorage> lazySpeechStorage();

    Lazy<ISpeechInteraction> lazySpeechInteraction();
}
