package cn.booslink.llm.downloader.listener;

import cn.booslink.llm.common.model.ApkDownload;

public class SimpleAppManagerListener implements OnAppManagerListener {
    @Override
    public void onAppDownloaded(ApkDownload download) {

    }

    @Override
    public void onAppInstalled(ApkDownload download) {

    }

    @Override
    public void onAppFailed(boolean isDownloadFailed, ApkDownload download) {

    }
}
