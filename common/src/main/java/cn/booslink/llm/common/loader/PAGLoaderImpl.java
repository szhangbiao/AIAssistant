package cn.booslink.llm.common.loader;

import android.content.Context;
import android.os.Build;

import org.libpag.PAGFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import cn.booslink.llm.common.utils.RxUtil;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

public class PAGLoaderImpl implements IPAGLoader {

    private final Context mContext;
    private final Map<String, PAGFile> mPagFiles;
    private final List<String> mPagFileNames;

    private Disposable mLoadDisposable;

    @Inject
    public PAGLoaderImpl(@ApplicationContext Context context) {
        this.mContext = context;
        this.mPagFileNames = Arrays.asList("pag_wink.pag", "pag_crying.pag", "pag_laughing.pag", "pag_thinking.pag", "pag_hello.pag", "pag_loading.pag");
        this.mPagFiles = new HashMap<>();
    }

    @Override
    public void loadPagFiles() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        mLoadDisposable = Single.fromCallable(() -> {
                    for (String name : mPagFileNames) {
                        mPagFiles.put(name, PAGFile.Load(mContext.getAssets(), name));
                    }
                    return true;
                })
                .compose(RxUtil.singleOnMain())
                .subscribe();
    }

    @Override
    public PAGFile getPagFile(String name) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;
        if (mPagFileNames.contains(name)) {
            PAGFile pagFile = mPagFiles.get(name);
            if (pagFile == null) {
                pagFile = PAGFile.Load(mContext.getAssets(), name);
                mPagFiles.put(name, pagFile);
            }
            return pagFile;
        }
        return null;
    }

    @Override
    public void release() {
        if (mLoadDisposable != null && !mLoadDisposable.isDisposed()) {
            mLoadDisposable.dispose();
        }
        mLoadDisposable = null;
    }
}
