package cn.booslink.llm.processor.repository;

import java.util.List;

import cn.booslink.llm.common.model.AppSummary;
import cn.booslink.llm.common.model.PkgInfo;
import io.reactivex.rxjava3.core.Single;

public interface IAppRepository {

    Single<List<AppSummary>> getAppSummaryList();
    Single<PkgInfo> getPkgInfo(AppSummary summary);
}
