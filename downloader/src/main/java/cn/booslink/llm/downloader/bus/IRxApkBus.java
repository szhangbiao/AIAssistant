package cn.booslink.llm.downloader.bus;

import cn.booslink.llm.common.model.ApkDownload;

public interface IRxApkBus {
    void post(ApkDownload downloadDTO);

    void clear();
}
