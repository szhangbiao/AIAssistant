package cn.booslink.llm.common.loader;

import android.os.Build;
import android.util.LruCache;

import org.libpag.PAGFile;

import javax.inject.Inject;
import timber.log.Timber;

public class PAGLoaderImpl implements IPAGLoader {

    private static final String TAG = "PAGLoader";
    private static final int MAX_CACHE_SIZE = 3;
    private final LruCache<String, PAGFile> mPagFiles;

    @Inject
    public PAGLoaderImpl() {
        mPagFiles = new LruCache<>(MAX_CACHE_SIZE) {
            @Override
            protected void entryRemoved(boolean evicted, String key, PAGFile oldValue, PAGFile newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                if (evicted) {
                    Timber.tag(TAG).d("触碰容量上限！表情 [%s] 的 Native 内存已被 LRU 机制淘汰释放", key);
                    // oldValue 失去了强引用，Dalvik GC 稍后会回收它，C++ 底层的巨量内存也会随之释放
                }
            }
        };
    }

    @Override
    public PAGFile getPagFile(String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;
        return mPagFiles.get(name);
    }

    @Override
    public void putPagFile(String name, PAGFile pagFile) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        if (pagFile != null) {
            mPagFiles.put(name, pagFile);
        }
    }

    @Override
    public void release() {
        mPagFiles.evictAll(); // 休眠时一键清空所有缓存，释放彻底
    }
}
