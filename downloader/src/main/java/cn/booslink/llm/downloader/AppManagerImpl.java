package cn.booslink.llm.downloader;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.ApkInfo;
import cn.booslink.llm.common.model.PkgInfo;
import cn.booslink.llm.common.model.enums.ApkStatus;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.utils.FileUtils;
import cn.booslink.llm.downloader.bus.IRxApkBus;
import cn.booslink.llm.downloader.listener.OnApkDownloadListener;
import cn.booslink.llm.downloader.listener.SimpleDownloadListener;
import cn.booslink.llm.downloader.model.InstallState;
import cn.booslink.llm.downloader.observer.PackageInstallObserver;
import cn.booslink.llm.downloader.receiver.PkgInstallBroadcastReceiver;
import cn.booslink.llm.downloader.utils.ApkInstallUtils;
import cn.booslink.llm.downloader.utils.InstallStateUtils;
import cn.booslink.llm.downloader.utils.PkgUtils;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class AppManagerImpl implements IAppManager {

    private static final String TAG = "AppManager";

    public final static String TASK_TAG_REPADDING = "RE-PADDING";
    private final static int DELAY_DOWNLOAD_RETRY = 200;

    private final Context mContext;
    private final IRxApkBus mRxApkBus;
    private final CompositeDisposable mCompositeDisposable;
    private final ConcurrentHashMap<String, ApkDownload> mApkDownloadMap;
    private final PkgInstallBroadcastReceiver mPkgInstallBroadcastReceiver;

    private DownloadTask mDownloadingTask;
    private volatile String currentPackageName = null;

    @Inject
    public AppManagerImpl(@ApplicationContext Context context, IRxApkBus rxApkBus, PkgInstallBroadcastReceiver receiver) {
        this.mContext = context;
        this.mRxApkBus = rxApkBus;
        this.mPkgInstallBroadcastReceiver = receiver;
        this.mApkDownloadMap = new ConcurrentHashMap<>();
        this.mCompositeDisposable = new CompositeDisposable();
        registerPackageInstallBroadcast();
    }

    @Override
    public void startDownloadPkg(PkgInfo pkgInfo) {
        Object taskTag = mDownloadingTask != null ? mDownloadingTask.getTag() : null;
        if (pkgInfo.getPkgName().equals(taskTag) && OkDownload.with().downloadDispatcher().isRunning(mDownloadingTask)) return;
        ApkDownload currentDownload = mApkDownloadMap.get(pkgInfo.getPkgName());
        if (currentDownload != null) return;
        ApkDownload download = ApkDownload.createFromPkgInfo(pkgInfo);
        startDownloadApkIfNeed(download);
        mRxApkBus.post(download);
    }

    @Override
    public void install(ApkInfo apkInfo) {
        ApkDownload download = ApkDownload.createFromApkInfo(apkInfo, false);
        install(download);
    }

    @Override
    public boolean isPkgDownloading() {
        return !mApkDownloadMap.isEmpty() && mDownloadingTask != null && OkDownload.with().downloadDispatcher().isRunning(mDownloadingTask);
    }

    @Override
    public boolean isPkgInstalling() {
        return !mApkDownloadMap.isEmpty() && mDownloadingTask == null;
    }

    @Override
    public void onAppInstalled(InstallState state, String packageName) {
        boolean isInstallSuccess = state == InstallState.SUCCESS;
        // 如果packageName和installingPackageName都为空，则说明发生了安装错误
        final String currentPkgName = !TextUtils.isEmpty(packageName) ? packageName : (!TextUtils.isEmpty(currentPackageName) ? currentPackageName : "");
        Timber.tag(TAG).d("app install %s and package: %s", isInstallSuccess ? "success" : "fail", currentPkgName);
        Disposable disposable = Single.just(currentPkgName)
                .map(deliveryPkgName -> handleApkDownloadAfterInstall(isInstallSuccess, state, deliveryPkgName))
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(apkDownload -> {
                    // 安装失败Toast失败原因
                    if (!isInstallSuccess && apkDownload.getRetryCount() == ApkDownload.FLAG_APK_FAIL) {
                        if (state == InstallState.INSUFFICIENT_STORAGE) {
                            // TODO mToast.get().showMessage(state.getMessage(), 10);
                        }
                    }
                    return apkDownload;
                })
                .observeOn(Schedulers.io())
                .subscribe(installedApk -> {
                    handlePaddingInstallList(installedApk);
                    currentPackageName = null;
                    //mRxApkBus.post(installedApk);
                });
        addDisposable(disposable);
    }

    private void registerPackageInstallBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mPkgInstallBroadcastReceiver, filter);
    }

    private void startDownloadApkIfNeed(ApkDownload download) {
        if (mDownloadingTask == null) {
            buildTaskAndDownloadApk(download);
        } else {
            mApkDownloadMap.put(download.getPkgName(), download);
        }
    }

    private void buildTaskAndDownloadApk(ApkDownload download) {
        DownloadTask task = buildDownloadTask(download);
        Timber.tag(TAG).d("buildTaskAndDownloadApk, apkId: %s, pkgName: %s, taskId: %s", download.getApkId(), download.getPkgName(), task.getId());
        task.enqueue(new SimpleDownloadListener(download, generateApkDownloadListener()));
        mDownloadingTask = task;
        currentPackageName = download.getPkgName();
        mApkDownloadMap.put(download.getPkgName(), download);
    }

    private DownloadTask buildDownloadTask(ApkDownload download) {
        // 使用新的file name并兼容旧的命名方式
        File parentFile = ContextUtils.getDownloadParentFile(mContext);
        File oldFile = new File(parentFile, download.getFileName());
        String fileName = oldFile.exists() ? download.getFileName() : download.getNewFileName();
        DownloadTask task = new DownloadTask.Builder(download.getDownloadUrl(), parentFile) //设置下载地址和下载目录，这两个是必须的参数
                .setFilename(fileName)//设置下载文件名，没提供的话先看 response header ，再看 url path(即启用下面那项配置)
                //.setFilenameFromResponse(false)//是否使用 response header or url path 作为文件名，此时会忽略指定的文件名，默认false
                .setPassIfAlreadyCompleted(false)//如果文件已经下载完成，再次下载时，是否忽略下载，默认为true(忽略)，设为false会从头下载
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
                if (downloadItem.getStatus() == ApkStatus.INSTALL_PADDING) {
                    install(downloadItem);
                } else if (downloadItem.isDownloadFail()) {
                    // TODO mToast.get().showMessage(downloadItem.getFailedReason(), 10);
                }
                mRxApkBus.post(downloadItem);
            }

            @Override
            public void onRetryDownload(ApkDownload downloadItem) {
                cancelCurrentAndStartNewDownload(downloadItem, true);
            }

            @Override
            public void onDownloadFailed(ApkDownload downloadItem) {
                mRxApkBus.post(downloadItem);
                // TODO mToast.get().showMessage(downloadItem.getFailedReason(), 10);
            }
        };
    }

    private void cancelCurrentAndStartNewDownload(ApkDownload download, boolean isRetryDownload) {
        boolean haveDownloadTask = mDownloadingTask != null && OkDownload.with().downloadDispatcher().isRunning(mDownloadingTask);
        Disposable disposable = Single.just(download)
                .map(apkDownload -> {
                    String downloadPkg = mDownloadingTask != null ? mDownloadingTask.getTag().toString() : "";
                    cancelDownloadingIfRunning(downloadPkg, TASK_TAG_REPADDING);
                    return apkDownload;
                })
                .delay(haveDownloadTask ? DELAY_DOWNLOAD_RETRY : 0, TimeUnit.MILLISECONDS)
                .subscribe(apkDownload -> {
                    // 在Delay期间可能start其他的下载
                    String downloadPkg = mDownloadingTask != null ? mDownloadingTask.getTag().toString() : "";
                    ApkDownload downloadingApk = mApkDownloadMap.get(downloadPkg);
                    boolean haveDownloadRunning = mDownloadingTask != null && downloadingApk != null && downloadingApk.getStatus() == ApkStatus.DOWNLOAD_PROGRESS;
                    Timber.tag(TAG).d("pauseCurrentAndStartNewDownload, pkgName: %s,current running: %s", downloadPkg, haveDownloadRunning ? "true" : "false");
                    if (haveDownloadRunning) {
                        // 快速点击造成下载盒子有两个item的status为download progress
                        if (apkDownload.getStatus() == ApkStatus.DOWNLOAD_PROGRESS) {
                            apkDownload.setStatus(ApkStatus.DOWNLOAD_PADDING);
                            mRxApkBus.post(apkDownload);
                        }
                        return;
                    }
                    // 在Delay期间可能会被移除下载队列或者正常的开始下载
                    boolean retryDownloadStillExist = isRetryDownload && mApkDownloadMap.containsKey(apkDownload.getPkgName());
                    if (!isRetryDownload || retryDownloadStillExist) {
                        buildTaskAndDownloadApk(apkDownload);
                    }
                    Timber.tag(TAG).d("pauseCurrentAndStartNewDownload, pkgName: %s, exist: %s, retry count: %d", apkDownload.getPkgName(), retryDownloadStillExist ? "true" : "false", apkDownload.getRetryCount());
                });
        addDisposable(disposable);
    }

    private synchronized void cancelDownloadingIfRunning(@Nullable String pkgName, String cancelTag) {
        Timber.tag(TAG).d("cancelCurrentDownloading, pkgName: %s, tag: %s", pkgName, cancelTag);
        if (apkIsDownloading(pkgName)) {
            mDownloadingTask.setTag(!TextUtils.isEmpty(cancelTag) ? cancelTag : pkgName);
            mDownloadingTask.cancel();
        }
        mDownloadingTask = null;
        OkDownload.with().downloadDispatcher().cancelAll();
    }

    private boolean apkIsDownloading(String pkgName) {
        return mDownloadingTask != null && mDownloadingTask.getTag().equals(pkgName) && OkDownload.with().downloadDispatcher().isRunning(mDownloadingTask);
    }

    private ApkDownload handleApkDownloadAfterInstall(boolean isInstallSuccess, InstallState state, String installPackage) {
        //为了让流程走下去就取安装队列里的第一个
        String installPkgName = !TextUtils.isEmpty(installPackage) ? installPackage : currentPackageName;
        ApkDownload apkDownload = mApkDownloadMap.get(installPkgName);
        if (apkDownload != null && apkDownload.shouldRemoveFromInstallList()) {
            apkDownload.installResult(isInstallSuccess, state != InstallState.INSUFFICIENT_STORAGE);
            mRxApkBus.post(apkDownload);
        }
        Timber.tag(TAG).d("update apk install success, and download is %s", apkDownload == null ? "null" : "not null");
        return apkDownload == null || !apkDownload.shouldRemoveFromInstallList() ? ApkDownload.empty() : apkDownload;
    }

    private void handlePaddingInstallList(ApkDownload installedApk) {
        if (!installedApk.isEmpty()) {
            if (installedApk.getStatus() == ApkStatus.INSTALL_SUCCESS || installedApk.isInstallFail()) {
                //安装成功后删除本地apk文件,安装失败并重试失败则不删除
                if (installedApk.getStatus() == ApkStatus.INSTALL_SUCCESS) {
                    FileUtils.deleteFile(installedApk.isLocalApkInstall() ? new File(installedApk.getApkPath()) : ContextUtils.getDownloadFile(mContext, installedApk.getFileName(), installedApk.getNewFileName()));
                }
                boolean flag = mApkDownloadMap.remove(installedApk.getPkgName()) != null;
                Timber.tag(TAG).d("remove apk %s it %s padding list", installedApk.getPkgName(), flag ? "in" : "not in");
            } else {
                Timber.tag(TAG).d("install fail and retry count = %d", installedApk.getRetryCount());
            }
        }
    }

    private void install(ApkDownload downloadApk) {
        ApkDownload paddingDownload = mApkDownloadMap.get(downloadApk.getPkgName());
        if (paddingDownload == null) {
            downloadApk.installRePadding();
            mApkDownloadMap.put(downloadApk.getPkgName(), downloadApk);
        }
        if (ContextUtils.isSystemApp(mContext)) {
            installDownloadApk(downloadApk);
        } else {
            mApkDownloadMap.remove(downloadApk.getPkgName());
            currentPackageName = null;
            installRandomApk(downloadApk);
        }
        mRxApkBus.post(downloadApk);
    }

    private void installDownloadApk(ApkDownload downloadItem) {
        Disposable disposable = Single.just(downloadItem)
                .map(apkDownload -> {
                    apkDownload.setStatus(ApkStatus.INSTALL_GOING);
                    mRxApkBus.post(apkDownload);
                    return apkDownload;
                })
                .delay(500, TimeUnit.MILLISECONDS)
                .flatMap((Function<ApkDownload, SingleSource<?>>) apkDownload -> {
                    String apkPath = apkDownload.isLocalApkInstall() ? apkDownload.getApkPath() : ContextUtils.getDownloadFilePath(mContext, apkDownload.getFileName(), apkDownload.getNewFileName());
                    String pkgName = apkDownload.getPkgName();
                    return install(apkPath, pkgName);
                })
                .observeOn(Schedulers.io())
                .subscribe();
        addDisposable(disposable);
    }

    private Single<Boolean> install(String apkPath, String pkgName) {
        return Single.fromCallable(() -> {
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mDevice.isMTKDevice())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ApkInstallUtils.installerInstall(mContext, apkPath);
                    } else {
                        ApkInstallUtils.customInstall(mContext, apkPath, new PackageInstallObserver() {
                            /**
                             * @param packageName 包名
                             * @param returnCode 请参考lib下的classes_secure.jar中PackageManager的常量声明
                             * @throws RemoteException 远程调用异常
                             */
                            @Override
                            public void packageInstalled(String packageName, int returnCode) throws RemoteException {
                                super.packageInstalled(packageName, returnCode);
                                InstallState installState = InstallStateUtils.checkInstallReturnCode(returnCode);
                                String installPkgName = TextUtils.isEmpty(packageName) ? pkgName : packageName;
                                Timber.tag(TAG).d("custom install code: %s, packageName: %s", returnCode, installPkgName);
                                onAppInstalled(installState, installPkgName);
                            }
                        });
                    }
                    return true;
                })
                .onErrorReturn(throwable -> {
                    ApkInfo apkInfo = PkgUtils.getApkInfoByFile(mContext, new File(apkPath));
                    if (apkInfo != null) {
                        InstallState state = "Requested internal only, but not enough space".equals(throwable.getMessage()) ? InstallState.INSUFFICIENT_STORAGE : InstallState.UNKNOWN;
                        onAppInstalled(state, apkInfo.getPkgName());
                    }
                    return false;
                });
    }

    private void installRandomApk(ApkDownload downloadApk) {
        downloadApk.installRandom();
        if (shouldRandomInstallApp()) {
            // 如果是本地安装，则延迟800毫秒
            Disposable disposable = Single.just(downloadApk)
                    .delay(downloadApk.isLocalApkInstall() ? 800 : 0, TimeUnit.MILLISECONDS)
                    .subscribe(apkDownloadDTO -> {
                        Timber.tag(TAG).d("Direct install start : %s", apkDownloadDTO.getPkgName());
                        installRandom(apkDownloadDTO);
                    });
            addDisposable(disposable);
        }
    }

    private boolean shouldRandomInstallApp() {
        if (!checkInstallPermission()) {
            return false;
        }
        // return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || mDevice.isAppActive();
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    private boolean checkInstallPermission() {
        // 如果是非系统app则先判断是否授予相应的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !mContext.getPackageManager().canRequestPackageInstalls()) {
            Uri packageURI = Uri.parse("package:" + mContext.getPackageName());
            //注意这个是8.0新API
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return false;
        }
        return true;
    }

    private void installRandom(ApkDownload downloadApk) {
        Disposable disposable = Single.just(downloadApk)
                .map(apkDownload -> {
                    String apkPath = downloadApk.isLocalApkInstall() ? downloadApk.getApkPath() : ContextUtils.getDownloadFilePath(mContext, downloadApk.getFileName(), downloadApk.getNewFileName());
                    Timber.tag(TAG).d("installRandom apkPath: %s", apkPath);
                    ApkInstallUtils.install(mContext, apkPath);
                    return true;
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe();
        addDisposable(disposable);
    }

    private void addDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }
}
