package cn.booslink.llm.speech.repository;

import cn.booslink.llm.common.model.AppUpgrade;
import io.reactivex.rxjava3.core.Single;

public interface IUpgradeRepository {
    Single<AppUpgrade> getAppUpgrade();
}
