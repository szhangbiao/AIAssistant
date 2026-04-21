package cn.booslink.llm.downloader;

import cn.booslink.llm.common.model.ApkInfo;
import cn.booslink.llm.common.model.PkgInfo;
import cn.booslink.llm.downloader.listener.OnAppManagerListener;
import cn.booslink.llm.downloader.model.InstallState;

public interface IAppManager {

    void startDownloadPkg(PkgInfo pkgInfo);

    void downloadPkgOnly(PkgInfo pkgInfo);

    void install(ApkInfo apkInfo);

    boolean isPkgDownloading();

    boolean isPkgInstalling();

    void onAppInstalled(InstallState state, String packageName);

    void registerListener(OnAppManagerListener listener);

    void release();
}
