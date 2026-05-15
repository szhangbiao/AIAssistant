package cn.booslink.llm.common.utils;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

public class LruCacheUtils {
    private final LruCache<String, String> mLruCache;

    public LruCacheUtils() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        mLruCache = new LruCache<String, String>((int) (maxMemory / 8)) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull String value) {
                return value.getBytes().length;
            }
        };
    }

    public LruCacheUtils(int maxSize) {
        mLruCache = new LruCache<String, String>(maxSize) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull String value) {
                return value.getBytes().length;
            }
        };
    }

    public void putCache(@NonNull String key, @NonNull String value) {
        mLruCache.put(key, value);
    }


    public String getCache(@NonNull String key) {
        return mLruCache.get(key);
    }


    public void removeCache(@NonNull String key) {
        mLruCache.remove(key);
    }

    public void clearCache() {
        if (mLruCache.size() > 0) {
            mLruCache.evictAll();
        }
    }
}
