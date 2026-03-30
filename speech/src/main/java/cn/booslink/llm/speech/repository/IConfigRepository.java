package cn.booslink.llm.speech.repository;

import cn.booslink.llm.speech.config.AIUIConfig;
import io.reactivex.rxjava3.core.Single;

public interface IConfigRepository {
    Single<AIUIConfig> readConfig();
}
