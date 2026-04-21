package cn.booslink.llm.downloader.listener;

import cn.booslink.llm.common.model.ApkDownload;

public interface OnAppManagerListener {
    void onAppDownloaded(ApkDownload download);

    void onAppInstalled(ApkDownload download);

    void onAppFailed(boolean isDownloadFailed, ApkDownload download);
}
