package cn.booslink.llm.common.di;

import android.content.Context;

import com.google.gson.Gson;

import javax.inject.Singleton;

import cn.booslink.llm.common.model.Device;
import cn.booslink.llm.common.utils.GsonProvider;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class CommonModule {

    @Singleton
    @Provides
    public Device provideDevice(@ApplicationContext Context context) {
        String channel = ""; // TODO channel
        String version = ""; // TODO version
        return Device.of(context, channel, version);
    }

    @Singleton
    @Provides
    public Gson provideGson() {
        return GsonProvider.instance();
    }
}
