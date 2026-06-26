package cn.booslink.llm.downloader;

import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.ApkInfo;
import cn.booslink.llm.common.model.AppUpgrade;
import cn.booslink.llm.common.model.DeviceInfo;
import cn.booslink.llm.common.model.PkgInfo;
import cn.booslink.llm.common.model.enums.ApkStatus;
import cn.booslink.llm.common.storage.ISpeechStorage;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.utils.FileUtils;
import cn.booslink.llm.downloader.listener.OnApkDownloadListener;
import cn.booslink.llm.downloader.listener.SimpleDownloadListener;
import cn.booslink.llm.downloader.model.InstallState;
import cn.booslink.llm.downloader.observer.PackageInstallObserver;
import cn.booslink.llm.downloader.utils.ApkInstallUtils;
import cn.booslink.llm.downloader.utils.InstallStateUtils;
import cn.booslink.llm.downloader.utils.PkgUtils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class AppUpgradeManager {

    private static final String TAG = "UpgradeManager";

    private final Context mContext;
    private final DeviceInfo mDevice;

    private Disposable mInstallDisposable;
    private DownloadTask mDownloadingTask;

    public AppUpgradeManager(Context context, DeviceInfo deviceInfo) {
        this.mContext = context;
        this.mDevice = deviceInfo;
    }

    public void startDownload(AppUpgrade upgrade) {
        PkgInfo pkgInfo = PkgInfo.createForAppUpgrade(upgrade, mDevice);
        Object taskTag = mDownloadingTask != null ? mDownloadingTask.getTag() : null;
        if (pkgInfo.getPkgName().equals(taskTag) && OkDownload.with().downloadDispatcher().isRunning(mDownloadingTask)) return;
        ApkDownload download = ApkDownload.createFromPkgInfo(pkgInfo, false);
        buildTaskAndDownloadApk(download);
    }

    private void buildTaskAndDownloadApk(ApkDownload download) {
        DownloadTask task = buildDownloadTask(download);
        Timber.tag(TAG).d("buildTaskAndDownloadApk, apkId: %s, pkgName: %s, taskId: %s", download.getApkId(), download.getPkgName(), task.getId());
        task.enqueue(new SimpleDownloadListener(download, generateApkDownloadListener()));
        mDownloadingTask = task;
    }

    private DownloadTask buildDownloadTask(ApkDownload download) {
        // 使用新的file name并兼容旧的命名方式
        File parentFile = ContextUtils.getDownloadParentFile(mContext);
        File oldFile = new File(parentFile, download.getFileName());
        String fileName = oldFile.exists() ? download.getFileName() : download.getNewFileName();
        DownloadTask task = new DownloadTask.Builder(download.getDownloadUrl(), parentFile) //设置下载地址和下载目录，这两个是必须的参数
                .setFilename(fileName)//设置下载文件名，没提供的话先看 response header ，再看 url path(即启用下面那项配置)
                //.setFilenameFromResponse(false)//是否使用 response header or url path 作为文件名，此时会忽略指定的文件名，默认false
                .setPassIfAlreadyCompleted(true)//如果文件已经下载完成，再次下载时，是否忽略下载，默认为true(忽略)，设为false会从头下载
                .setConnectionCount(1)  //需要用几个线程来下载文件，默认根据文件大小确定；如果文件已经 split block，则设置后无效
                //.setPreAllocateLength(false) //在获取资源长度后，设置是否需要为文件预分配长度，默认false
                .setMinIntervalMillisCallbackProcess(1000) //通知调用者的频率，避免anr，默认3000
                //.setWifiRequired(false)//是否只允许wifi下载，默认为false
                .setAutoCallbackToUIThread(false) //是否在主线程通知调用者，默认为true
                //.setHeaderMapFields(new HashMap<String, List<String>>())//设置请求头
                //.addHeader(String key, String value)//追加请求头
                .setPriority(0)//设置优先级，默认值是0，值越大下载优先级越高
                //.setReadBufferSize(4096)//设置读取缓存区大小，默认4096
                //.setFlushBufferSize(16384)//设置写入缓存区大小，默认16384
                //.setSyncBufferSize(65536)//写入到文件的缓冲区大小，默认65536
                //.setSyncBufferIntervalMillis(2000) //写入文件的最小时间间隔，默认2000
                .build();
        task.setTag(download.getPkgName());
        return task;
    }

    private OnApkDownloadListener generateApkDownloadListener() {
        return new OnApkDownloadListener() {
            @Override
            public void onDownloadUpdate(String apkPath, ApkDownload downloadItem) {
                Timber.tag(TAG).d("onDownloadUpdate");
                if (downloadItem.getStatus() == ApkStatus.INSTALL_PADDING) {
                    installRandom(downloadItem);
                } else if (downloadItem.isDownloadError()) {
                    Timber.tag(TAG).d("onDownloadUpdate, download fail");
                }
                mDownloadingTask = null;
            }

            @Override
            public void onRetryDownload(ApkDownload downloadItem) {
                Timber.tag(TAG).d("onDownloadFailed");
                mDownloadingTask = null;
            }

            @Override
            public void onDownloadFailed(ApkDownload downloadItem) {
                Timber.tag(TAG).d("onDownloadFailed");
                mDownloadingTask = null;
            }
        };
    }

    private void installRandom(ApkDownload downloadApk) {
        mInstallDisposable = Single.just(downloadApk)
                .map(apkDownload -> {
                    String apkPath = downloadApk.isLocalApkInstall() ? downloadApk.getApkPath() : ContextUtils.getDownloadFilePath(mContext, downloadApk.getFileName(), downloadApk.getNewFileName());
                    Timber.tag(TAG).d("installRandom apkPath: %s", apkPath);
                    ApkInstallUtils.install(mContext, apkPath);
                    return true;
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void clear() {
        if (mInstallDisposable != null && !mInstallDisposable.isDisposed()) {
            mInstallDisposable.dispose();
        }
        mInstallDisposable = null;
    }
}
