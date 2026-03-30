package cn.booslink.llm.speech.repository;

import android.content.Context;

import com.google.gson.Gson;

import javax.inject.Inject;

import cn.booslink.llm.common.utils.FileUtils;
import cn.booslink.llm.speech.config.AIUIConfig;
import cn.booslink.llm.speech.config.LoginConfig;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;

public class ConfigRepositoryImpl implements IConfigRepository {

    private final String APP_ID = "b7e9e86d";
    private final String APP_KEY = "1c871f468479745d81d486be9852f275";
    private final String API_SECRET = "NjQwZWJjZGUxOTJjOGI3MmE1ODViZWE0";

    private final Context mContext;
    private final Gson mGson;

    @Inject
    public ConfigRepositoryImpl(@ApplicationContext Context context, Gson gson) {
        this.mGson = gson;
        this.mContext = context;
    }

    @Override
    public Single<AIUIConfig> readConfig() {
        return Single.fromCallable(() -> {
            String configJson = FileUtils.readJsonFromAsset(mContext, "cfg/aiui_config.json");
            AIUIConfig config = mGson.fromJson(configJson, AIUIConfig.class);
            LoginConfig loginConfig = new LoginConfig(APP_ID, APP_KEY, API_SECRET);
            // TODO 更新唤醒配置
            return config.newLogin(loginConfig);
        });
    }
}
