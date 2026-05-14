package cn.booslink.llm.di;

import android.content.Context;

import javax.inject.Singleton;

import cn.booslink.llm.BuildConfig;
import cn.booslink.llm.common.model.DeviceInfo;
import cn.booslink.llm.common.model.enums.Channel;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.downloader.AppUpgradeManager;
import cn.booslink.llm.record.IVoiceInput;
import cn.booslink.llm.record.VoiceInputImpl;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Singleton
    @Provides
    public DeviceInfo provideDeviceInfo(@ApplicationContext Context context) {
        String channel = ContextUtils.getManifestChannel(context);
        boolean isSystemApp = ContextUtils.isSystemApp(context);
        String appName = ContextUtils.getAppName(context);
        String pkgName = context.getPackageName();
        String versionName = ContextUtils.getVersionName(context);
        int versionCode = ContextUtils.getVersionCode(context);
        boolean isDevMode = BuildConfig.DEBUG_MODE;
        return new DeviceInfo(isDevMode, isSystemApp, appName, pkgName, versionName, versionCode, Channel.fromChannel(channel));
    }

    @Singleton
    @Provides
    public AppUpgradeManager provideAppUpgradeManager(@ApplicationContext Context context, DeviceInfo deviceInfo) {
        return new AppUpgradeManager(context, deviceInfo);
    }

    @Module
    @InstallIn(SingletonComponent.class)
    public interface AppBinds {

        @Binds
        @Singleton
        IVoiceInput bindVoiceInput(VoiceInputImpl voiceInputImpl);
    }
}
