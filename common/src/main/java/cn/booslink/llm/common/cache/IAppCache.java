package cn.booslink.llm.common.cache;

import androidx.annotation.NonNull;

import org.joda.time.Duration;

import cn.booslink.llm.common.cache.convert.CacheConverter;
import io.reactivex.rxjava3.core.Single;

public interface IAppCache {
    <T> boolean putCache(@NonNull String key, @NonNull T value, @NonNull CacheConverter<T> cacheConverter);

    <T> boolean putCache(@NonNull String key, @NonNull T value, @NonNull Duration duration, @NonNull CacheConverter<T> cacheConverter);

    <T> T getCache(@NonNull String key, @NonNull CacheConverter<T> cacheConverter);

    <T> Single<T> getCacheSingle(@NonNull String key, @NonNull CacheConverter<T> cacheConverter);

    void clearCache();
}
