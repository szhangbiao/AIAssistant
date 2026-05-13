package cn.booslink.llm.speech.repository;

import javax.inject.Inject;

import cn.booslink.llm.common.model.AppUpgrade;
import cn.booslink.llm.common.model.DeviceInfo;
import cn.booslink.llm.common.network.ApiService;
import io.reactivex.rxjava3.core.Single;

public class UpgradeRepositoryImpl implements IUpgradeRepository {

    private final static String APP_UPGRADE_URL = "http://config.ottboxer.cn/llm/%s/upgrade.json";
    
    private final DeviceInfo mDevice;

    private final ApiService mApiService;

    @Inject
    public UpgradeRepositoryImpl(DeviceInfo deviceInfo, ApiService apiService) {
        this.mDevice = deviceInfo;
        this.mApiService = apiService;
    }

    @Override
    public Single<AppUpgrade> getAppUpgrade() {
        // 测试环境下用test
        String upgradeChannel = mDevice.isDevMode() ? "test" : mDevice.getChannel().getChannel();
        String requestUrl = String.format(APP_UPGRADE_URL, upgradeChannel);
        return mApiService.getAppUpgradeInfo(requestUrl);
    }
}
