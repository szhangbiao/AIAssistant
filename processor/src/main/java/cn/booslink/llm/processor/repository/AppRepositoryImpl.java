package cn.booslink.llm.processor.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.Duration;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import cn.booslink.llm.common.cache.IAppCache;
import cn.booslink.llm.common.cache.convert.AppSummaryListConverter;
import cn.booslink.llm.common.model.AppSummary;
import cn.booslink.llm.common.model.PkgInfo;
import cn.booslink.llm.common.model.request.ApkRequest;
import cn.booslink.llm.common.network.ApiService;
import cn.booslink.llm.common.utils.FileUtils;
import cn.booslink.llm.common.utils.TransformerUtil;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;

public class AppRepositoryImpl implements IAppRepository {

    private static final String URL_APP_SUMMARY = "http://config.ottboxer.cn/llm/package_config.json";

    private static final String KEY_APP_SUMMARY = "app_summary";
    private final Gson mGson;
    private final Context mContext;
    private final IAppCache mAppCache;
    private final ApiService mApiService;

    @Inject
    public AppRepositoryImpl(@ApplicationContext Context context, Gson gson, IAppCache appCache, ApiService apiService) {
        this.mGson = gson;
        this.mContext = context;
        this.mAppCache = appCache;
        this.mApiService = apiService;
    }

    @Override
    public Single<PkgInfo> getPkgInfo(String pkgName) {
        return mApiService.getApkInfoByPkg(ApkRequest.Companion.create(pkgName))
                .compose(TransformerUtil.singleApiTransformer());
    }

    @Override
    public Single<List<AppSummary>> getAppSummaryList() {
        Single<List<AppSummary>> cacheSingle = mAppCache.getCacheSingle(KEY_APP_SUMMARY, new AppSummaryListConverter())
                .onErrorReturn(throwable -> Collections.emptyList());
        Single<List<AppSummary>> serverSingle = getAppSummaryListByServer();
        Single<List<AppSummary>> backupSingle = getAppSummaryListBackup();
        return Single.concat(cacheSingle, serverSingle, backupSingle)
                .filter(summaryList -> !summaryList.isEmpty())
                .firstElement()
                .toSingle()
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof NoSuchElementException) {
                        return Single.just(Collections.emptyList());
                    }
                    return Single.error(throwable);
                });
    }

    private Single<List<AppSummary>> getAppSummaryListByServer() {
        return mApiService.getPackageConfigs(URL_APP_SUMMARY)
                .doOnSuccess(appSummaries -> {
                    // Update cache
                    Duration duration = Duration.standardDays(1);
                    mAppCache.putCache(KEY_APP_SUMMARY, appSummaries, duration, new AppSummaryListConverter());
                })
                .onErrorResumeWith(getAppSummaryListBackup());
    }

    private Single<List<AppSummary>> getAppSummaryListBackup() {
        return Single.fromCallable(() -> {
            String summaryJson = FileUtils.readJsonFromAsset(mContext, "app_summary.json");
            return mGson.fromJson(summaryJson, new TypeToken<List<AppSummary>>() {}.getType());
        });
    }
}
