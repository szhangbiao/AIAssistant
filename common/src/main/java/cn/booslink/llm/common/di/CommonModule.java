package cn.booslink.llm.common.di;

import android.content.Context;

import com.google.gson.Gson;

import javax.inject.Singleton;

import cn.booslink.llm.common.model.CBMSemantic;
import cn.booslink.llm.common.model.CBMTidy;
import cn.booslink.llm.common.model.CBMToolPK;
import cn.booslink.llm.common.model.Device;
import cn.booslink.llm.common.model.enums.CBMSub;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.common.network.adapter.CBMSemanticAdapter;
import cn.booslink.llm.common.network.adapter.CBMSubAdapter;
import cn.booslink.llm.common.network.adapter.CBMTidyAdapter;
import cn.booslink.llm.common.network.adapter.CBMToolPKAdapter;
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
        return GsonProvider.builder()
                .registerTypeAdapter(CBMSub.class, new CBMSubAdapter())
                .registerTypeAdapter(CBMTidy.class, new CBMTidyAdapter())
                .registerTypeAdapter(CBMToolPK.class, new CBMToolPKAdapter())
                .registerTypeAdapter(CBMSemantic.class, new CBMSemanticAdapter())
                .create();
    }
}
