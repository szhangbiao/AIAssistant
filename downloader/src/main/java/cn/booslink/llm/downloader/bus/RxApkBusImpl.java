package cn.booslink.llm.downloader.bus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import timber.log.Timber;

public class RxApkBusImpl implements IRxApkBus {

    private final static String TAG = "RxApkBus";

    private final static String TAG_UPDATE_DOWNLOAD = "UPDATE_DOWNLOAD";
    private final ISpeechInteraction mSpeechInteraction;
    private PublishSubject<ApkDownload> mDownloadSubject;
    private final ConcurrentHashMap<String, Disposable> mDisposableMap;

    @Inject
    public RxApkBusImpl(ISpeechInteraction speechInteraction) {
        this.mSpeechInteraction = speechInteraction;
        this.mDownloadSubject = PublishSubject.create();
        this.mDisposableMap = new ConcurrentHashMap<>();
        setupDownloadSubjectSubscribe();
    }

    @Override
    public void post(ApkDownload download) {
        if (download == null) return;
        mDownloadSubject.onNext(download);
    }

    @Override
    public void clear() {
        // 清理Subject
        if (mDownloadSubject != null && !mDownloadSubject.hasComplete()) {
            mDownloadSubject.onComplete();
        }
        // 清理所有订阅
        for (Disposable disposable : mDisposableMap.values()) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        mDisposableMap.clear();
    }

    private void setupDownloadSubjectSubscribe() {
        Disposable disposable = mDownloadSubject
                .debounce(500, TimeUnit.MILLISECONDS)  // 时间节流
                .distinctUntilChanged((previousDownload, afterDownload) -> {
                    // 状态变化立即通过
                    if (previousDownload.getStatus() != afterDownload.getStatus()) {
                        return false; // 不相同，允许通过
                    }
                    // 进度有变化才通过
                    return previousDownload.getProgress() != afterDownload.getProgress();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(download -> {
                    if (download == null) return;
                    // 更新下载状态
                    mSpeechInteraction.downloadUpdate(download.clone());
                }, this::handleErrorAndReSetupSubject);
        mDisposableMap.put(TAG_UPDATE_DOWNLOAD, disposable);
    }

    private void handleErrorAndReSetupSubject(Throwable throwable) {
        Timber.tag(TAG).e(throwable, "download update failed");

        // 先清理旧的订阅
        Disposable disposable = mDisposableMap.get(TAG_UPDATE_DOWNLOAD);
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        // 重新创建Subject（如果已终止）
        if (mDownloadSubject.hasComplete()) {
            mDownloadSubject = PublishSubject.create();
        }
        setupDownloadSubjectSubscribe();
    }
}
