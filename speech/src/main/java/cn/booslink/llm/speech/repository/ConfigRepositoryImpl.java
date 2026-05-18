package cn.booslink.llm.speech.repository;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.File;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Device;
import cn.booslink.llm.common.model.DeviceInfo;
import cn.booslink.llm.common.network.ApiService;
import cn.booslink.llm.common.network.exception.DeviceAuthException;
import cn.booslink.llm.common.storage.ISpeechStorage;
import cn.booslink.llm.common.utils.FileUtils;
import cn.booslink.llm.common.utils.StringUtils;
import cn.booslink.llm.common.utils.TransformerUtil;
import cn.booslink.llm.speech.config.AIUIConfig;
import cn.booslink.llm.speech.config.LoginConfig;
import cn.booslink.util.NetworkUtils;
import dagger.hilt.android.qualifiers.ApplicationContext;
import hu.akarnokd.rxjava3.bridge.RxJavaBridge;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class ConfigRepositoryImpl implements IConfigRepository {

    private final String APP_ID = "b7e9e86d";
    private final String APP_KEY = "1c871f468479745d81d486be9852f275";
    private final String API_SECRET = "NjQwZWJjZGUxOTJjOGI3MmE1ODViZWE0";

    private final String AUTH_PATH = "/ai_ktv/auth/llm";
    private final String AUTH_SALT = "llm-20260512";

    private final Gson mGson;
    private final Device mDevice;
    private final Context mContext;
    private final DeviceInfo mDeviceInfo;
    private final ApiService mApiService;
    private final ISpeechStorage mSpeechStorage;

    @Inject
    public ConfigRepositoryImpl(@ApplicationContext Context context, Gson gson, Device device, DeviceInfo deviceInfo, ApiService apiService, ISpeechStorage speechStorage) {
        this.mGson = gson;
        this.mDevice = device;
        this.mContext = context;
        this.mDeviceInfo = deviceInfo;
        this.mApiService = apiService;
        this.mSpeechStorage = speechStorage;
    }

    @Override
    public Single<Boolean> deviceAuth() {
        return getServer()
                .flatMap((Function<String, Single<Boolean>>) host -> {
                    String url = "http://" + host + AUTH_PATH;
                    long tm = System.currentTimeMillis();
                    String sign = StringUtils.md5(AUTH_SALT + mDevice.serialNo + mDevice.wifiMac + tm);
                    return mApiService.deviceAuth(url, tm, sign, mDevice)
                            .compose(upstream -> upstream.map(apiResponse -> {
                                if (apiResponse.getCode() == 200) {
                                    return true;
                                }
                                throw new DeviceAuthException(apiResponse.getMessage());
                            }))
                            .compose(TransformerUtil.singleApiRetry());
                });
    }

    @Override
    public Single<AIUIConfig> readConfig() {
        return Single.fromCallable(() -> {
            String configJson = FileUtils.readJsonFromAsset(mContext, "cfg/aiui_config.json");
            AIUIConfig config = mGson.fromJson(configJson, AIUIConfig.class);
            config.fixIvwResourcePath(mContext);
            config.updateLogConfig(mDeviceInfo.isDevMode());
            LoginConfig loginConfig = new LoginConfig(APP_ID, APP_KEY, API_SECRET);
            File vtnFile = new File(config.getIvw().getResPath());
            if (!vtnFile.exists()) {
                FileUtils.copyAssetFolder(mContext, "ivw", mContext.getFilesDir().getAbsolutePath() + "/ivw");
            }
            return config.newLogin(loginConfig);
        });
    }

    private Single<String> getServer() {
        String authHost = mSpeechStorage.getAuthHost();
        if (!TextUtils.isEmpty(authHost)) {
            return Single.just(authHost);
        }
        return RxJavaBridge.toV3Single(NetworkUtils.getServer("auth", "120.25.70.125:8088"))
                .doOnSuccess(host -> {
                    if (!TextUtils.isEmpty(host)) {
                        mSpeechStorage.setAuthHost(host);
                    }
                });
    }
}
