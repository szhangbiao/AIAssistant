package cn.booslink.llm.downloader;

import cn.booslink.llm.common.model.ApkInfo;
import cn.booslink.llm.common.model.PkgInfo;
import cn.booslink.llm.downloader.model.InstallState;

public interface IAppManager {

    void startDownloadPkg(PkgInfo pkgInfo);

    void install(ApkInfo apkInfo);

    void onAppInstalled(InstallState state, String packageName);
}
