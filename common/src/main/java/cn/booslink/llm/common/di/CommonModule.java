package cn.booslink.llm.common.di;

import android.content.Context;

import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import cn.booslink.llm.common.model.CBMSemantic;
import cn.booslink.llm.common.model.CBMTidy;
import cn.booslink.llm.common.model.CBMToolPK;
import cn.booslink.llm.common.model.Device;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.CBMSub;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.common.network.adapter.AIUIIntentAdapter;
import cn.booslink.llm.common.network.adapter.CBMSemanticAdapter;
import cn.booslink.llm.common.network.adapter.CBMSubAdapter;
import cn.booslink.llm.common.network.adapter.CBMTidyAdapter;
import cn.booslink.llm.common.network.adapter.CBMToolPKAdapter;
import cn.booslink.llm.common.network.adapter.CategoryAdapter;
import cn.booslink.llm.common.network.adapter.DateTimeAdapter;
import cn.booslink.llm.common.utils.GsonProvider;
import cn.booslink.llm.common.utils.HttpEngine;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

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
                .registerTypeAdapter(Category.class, new CategoryAdapter())
                .registerTypeAdapter(DateTime.class, new DateTimeAdapter())
                .registerTypeAdapter(CBMToolPK.class, new CBMToolPKAdapter())
                .registerTypeAdapter(AIUIIntent.class, new AIUIIntentAdapter())
                .registerTypeAdapter(CBMSemantic.class, new CBMSemanticAdapter())
                .create();
    }

    @Singleton
    @Provides
    public OkHttpClient provideApiOkHttpClient() {
        final OkHttpClient.Builder builder = HttpEngine.createClientBuilder(false, 10 * 1000, 10 * 1000);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").d(message));
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(interceptor);
        builder.retryOnConnectionFailure(true);
        builder.connectionPool(new ConnectionPool(5, 2, TimeUnit.MINUTES));
        return builder.build();
    }
}
