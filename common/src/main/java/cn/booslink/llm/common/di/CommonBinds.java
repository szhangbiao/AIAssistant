package cn.booslink.llm.common.di;

import javax.inject.Singleton;

import cn.booslink.llm.common.image.ImageLoader;
import cn.booslink.llm.common.image.ImageLoaderImpl;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.common.ui.SpeechInteractionImpl;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public interface CommonBinds {
    @Binds
    @Singleton
    ISpeechInteraction bindSpeechInteraction(SpeechInteractionImpl speechInteraction);

    @Binds
    @Singleton
    ImageLoader bindImageLoader(ImageLoaderImpl imageLoaderImpl);
}
