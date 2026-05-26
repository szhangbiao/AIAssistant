package cn.booslink.llm.common.cache;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;

import cn.booslink.llm.common.cache.convert.CacheConverter;
import cn.booslink.llm.common.utils.LruCacheUtils;
import cn.booslink.llm.common.utils.NetworkUtils;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;
import timber.log.Timber;

public class MemoryCacheImpl implements IAppCache {

    public static final String TAG = "Memory";
    private static final String FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";
    private static final String EXPIRE_TIME_SUFFIX = "_expire_time";

    private final Context mContext;
    private final LruCacheUtils mCacheUtils;

    @Inject
    public MemoryCacheImpl(@ApplicationContext Context context) {
        this.mContext = context;
        this.mCacheUtils = new LruCacheUtils();
    }

    @Override
    public <T> boolean putCache(@NonNull String key, @NonNull T value, @NonNull CacheConverter<T> cacheConverter) {
        if (TextUtils.isEmpty(key)) return false;
        String jsonValue = cacheConverter.serialize(value);
        Timber.tag(TAG).d("putCache: key: %s", key);
        mCacheUtils.putCache(key, jsonValue);
        return true;
    }

    @Override
    public <T> boolean putCache(@NonNull String key, @NonNull T value, @NonNull Duration duration, @NonNull CacheConverter<T> cacheConverter) {
        if (TextUtils.isEmpty(key)) return false;
        String jsonValue = cacheConverter.serialize(value);
        Timber.tag(TAG).d("putCache: key: %s", key);
        mCacheUtils.putCache(key, jsonValue);
        mCacheUtils.putCache(key + EXPIRE_TIME_SUFFIX, DateTime.now().plusMillis((int) duration.getMillis()).toString(FORMAT_TIME));
        return true;
    }

    @Override
    public <T> T getCache(@NonNull String key, @NonNull CacheConverter<T> cacheConverter) {
        if (TextUtils.isEmpty(key) || !NetworkUtils.isConnected(mContext)) return null;
        Timber.tag(TAG).d("getCache: key: %s", key);
        String jsonValue = mCacheUtils.getCache(key);
        if (jsonValue != null) {
            Timber.tag(TAG).d("getCache: key: %s, value: %s", key, jsonValue);
            String expireTimeStr = mCacheUtils.getCache(key + EXPIRE_TIME_SUFFIX);
            if (expireTimeStr == null || TextUtils.isEmpty(expireTimeStr) || DateTimeFormat.forPattern(FORMAT_TIME).parseDateTime(expireTimeStr).isAfterNow()) {
                return cacheConverter.deserialize(jsonValue);
            }
        }
        return null;
    }

    @Override
    public <T> Single<T> getCacheSingle(@NonNull String key, @NonNull CacheConverter<T> cacheConverter) {
        return Single.create(emitter -> {
            if (TextUtils.isEmpty(key) || !NetworkUtils.isConnected(mContext)) {
                emitter.onSuccess(null);
                return;
            }
            Timber.tag(TAG).d("getCacheSingle: key: %s", key);
            String jsonValue = mCacheUtils.getCache(key);
            String expireTimeStr = mCacheUtils.getCache(key + EXPIRE_TIME_SUFFIX);
            boolean isNotExpired = expireTimeStr == null || TextUtils.isEmpty(expireTimeStr) || DateTimeFormat.forPattern(FORMAT_TIME).parseDateTime(expireTimeStr).isAfterNow();
            if (isNotExpired && jsonValue != null && !TextUtils.isEmpty(jsonValue)) {
                T t = cacheConverter.deserialize(jsonValue);
                Timber.tag(TAG).d("getCacheSingle: key: %s, value: %s", key, jsonValue);
                emitter.onSuccess(t);
            } else {
                emitter.onSuccess(null);
            }
        });
    }

    @Override
    public void clearCache() {
        Timber.tag(TAG).d("clearCache");
        mCacheUtils.clearCache();
    }
}
