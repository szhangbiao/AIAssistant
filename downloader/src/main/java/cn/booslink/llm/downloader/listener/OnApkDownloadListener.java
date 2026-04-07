package cn.booslink.llm.downloader.listener;

import cn.booslink.llm.common.model.ApkDownload;

public interface OnApkDownloadListener {
    void onDownloadUpdate(String apkPath, ApkDownload downloadItem);

    void onRetryDownload(ApkDownload downloadItem);

    void onDownloadFailed(ApkDownload downloadItem);
}
