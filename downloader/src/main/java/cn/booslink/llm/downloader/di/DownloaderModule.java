package cn.booslink.llm.downloader.di;

import javax.inject.Singleton;

import cn.booslink.llm.downloader.AppManagerImpl;
import cn.booslink.llm.downloader.IAppManager;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public interface DownloaderModule {

    @Binds
    @Singleton
    IAppManager bindAppManager(AppManagerImpl appManagerImpl);
}
