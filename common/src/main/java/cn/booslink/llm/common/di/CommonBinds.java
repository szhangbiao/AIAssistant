package cn.booslink.llm.common.di;

import javax.inject.Singleton;

import cn.booslink.llm.common.image.ImageLoader;
import cn.booslink.llm.common.image.ImageLoaderImpl;
import cn.booslink.llm.common.storage.ISpeechStorage;
import cn.booslink.llm.common.storage.SpeechStorageImpl;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.common.ui.IToast;
import cn.booslink.llm.common.ui.SpeechInteractionImpl;
import cn.booslink.llm.common.ui.ToastImpl;
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

    @Binds
    @Singleton
    ISpeechStorage bindSpeechStorage(SpeechStorageImpl speechStorageImpl);

    @Binds
    @Singleton
    IToast bindToast(ToastImpl toastImpl);
}
