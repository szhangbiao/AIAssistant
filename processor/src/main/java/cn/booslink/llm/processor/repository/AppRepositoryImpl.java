package cn.booslink.llm.processor.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import javax.inject.Inject;

import cn.booslink.llm.common.model.AppSummary;
import cn.booslink.llm.common.model.PkgInfo;
import cn.booslink.llm.common.model.request.ApkRequest;
import cn.booslink.llm.common.network.ApiService;
import cn.booslink.llm.common.utils.FileUtils;
import cn.booslink.llm.common.utils.TransformerUtil;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;

public class AppRepositoryImpl implements IAppRepository {

    private final Gson mGson;
    private final Context mContext;
    private final ApiService mApiService;

    @Inject
    public AppRepositoryImpl(@ApplicationContext Context context, Gson gson, ApiService apiService) {
        this.mGson = gson;
        this.mContext = context;
        this.mApiService = apiService;
    }

    @Override
    public Single<PkgInfo> getPkgInfo(String pkgName) {
        return mApiService.getApkInfoByPkg(ApkRequest.Companion.create(pkgName))
                .compose(TransformerUtil.singleApiTransformer());
    }

    @Override
    public Single<List<AppSummary>> getAppSummaryList() {
        return getAppSummaryListBackup();
    }

    private Single<List<AppSummary>> getAppSummaryListBackup() {
        return Single.fromCallable(() -> {
            String summaryJson = FileUtils.readJsonFromAsset(mContext, "app_summary.json");
            return mGson.fromJson(summaryJson, new TypeToken<List<AppSummary>>() {}.getType());
        });
    }
}
