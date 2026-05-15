package cn.booslink.llm.common.cache.convert;

import androidx.annotation.NonNull;

public interface CacheConverter<T> {
    T deserialize(@NonNull String rawString);

    String serialize(@NonNull T t);
}
