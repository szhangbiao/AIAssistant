package cn.booslink.llm.processor.process.app;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.ApkInfo;
import cn.booslink.llm.common.model.AppInfo;
import cn.booslink.llm.common.model.AppSummary;
import cn.booslink.llm.common.model.PkgInfo;
import cn.booslink.llm.common.model.Slot;
import cn.booslink.llm.common.model.VoiceQuery;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIIntent;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.common.model.enums.QueryState;
import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.common.ui.IToast;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.utils.RxUtil;
import cn.booslink.llm.downloader.IAppManager;
import cn.booslink.llm.downloader.listener.SimpleAppManagerListener;
import cn.booslink.llm.downloader.utils.PkgUtils;
import cn.booslink.llm.processor.IEventProcessor;
import cn.booslink.llm.processor.repository.IAppRepository;
import dagger.Lazy;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import timber.log.Timber;

public class AppProcessImpl implements IAppProcess {

    private final static String TAG = "AppProcess";

    private final IToast mToast;
    private final Context mContext;
    private final IAppRepository mAppRepository;
    private final Lazy<IAppManager> mAppManagerLazy;
    private final Lazy<ISpeechAgent> mSpeechAgentLazy;
    private final ISpeechInteraction mSpeechInteraction;
    private final CompositeDisposable mCompositeDisposable;
    private final Lazy<IEventProcessor> mEventProcessorLazy;

    @Inject
    public AppProcessImpl(@ApplicationContext Context context, IToast toast, IAppRepository appRepository, Lazy<IAppManager> appManagerLazy, ISpeechInteraction speechInteraction, Lazy<ISpeechAgent> speechAgentLazy, Lazy<IEventProcessor> eventProcessorLazy) {
        this.mToast = toast;
        this.mContext = context;
        this.mAppRepository = appRepository;
        this.mAppManagerLazy = appManagerLazy;
        this.mSpeechAgentLazy = speechAgentLazy;
        this.mSpeechInteraction = speechInteraction;
        this.mEventProcessorLazy = eventProcessorLazy;
        this.mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public boolean shouldAppProcess(Category category, AIUIIntent intent) {
        return category == Category.APP && (intent == AIUIIntent.LAUNCH || intent == AIUIIntent.DOWNLOAD || intent == AIUIIntent.INSTALL);
    }

    @Override
    public VoiceResult handleAppIntent(AIUIIntent intent, @NotNull List<Slot> slots) {
        if (slots.isEmpty()) return VoiceResult.Companion.failure();
        String appName = null;
        for (Slot slot : slots) {
            if ("name".equals(slot.getName()) && !TextUtils.isEmpty(slot.getValue())) {
                appName = slot.getValue();
                break;
            }
        }
        if (TextUtils.isEmpty(appName)) return VoiceResult.Companion.failure();
        findMatchApp(intent, appName);
        return VoiceResult.Companion.success("正在处理");
    }

    @Override
    public void launchAppWithIntent(String pkgName, @Nullable Intent intent) {
        Timber.tag(TAG).d("launchAppWithIntent, package = %s", pkgName);
        Disposable disposable = Single.just(pkgName)
                .flatMap((Function<String, SingleSource<PkgInfo>>) deliveryPkgName -> {
                    AppInfo appInfo = PkgUtils.getAppInfo(mContext, deliveryPkgName);
                    if (appInfo != null) {
                        if (intent == null) {
                            PkgUtils.launchApp(mContext, deliveryPkgName);
                            mSpeechInteraction.nlpAnswer("已为你打开应用");
                        } else {
                            PkgUtils.launchIntent(mContext, intent);
                            mSpeechInteraction.nlpAnswer("好的");
                        }
                        return Single.just(PkgInfo.ignore());
                    }
                    return mAppRepository.getPkgInfo(deliveryPkgName);
                })
                .map(pkgInfo -> {
                    if (pkgInfo.isIgnore()) return pkgInfo;
                    File parentFile = ContextUtils.getDownloadParentFile(mContext);
                    Map<String, ApkInfo> apkMap = PkgUtils.getApkInfoMapByDir(mContext, parentFile.getPath());
                    ApkInfo apkInfo = apkMap.get(pkgInfo.getPkgName());
                    if (apkInfo != null) {
                        pkgInfo.setDownloaded(true);
                        pkgInfo.setLocalPath(apkInfo.getPath());
                    }
                    return pkgInfo;
                })
                .compose(RxUtil.singleOnMain())
                .subscribe(pkgInfo -> populateAppLaunchWithPkgInfo(pkgInfo, intent), this::populateProcessError);
        addDisposable(disposable);
    }

    private void findMatchApp(AIUIIntent intent, String appName) {
        Disposable disposable = mAppRepository.getAppSummaryList()
                .flatMap((Function<List<AppSummary>, SingleSource<PkgInfo>>)
                        appSummaries -> Observable.fromIterable(appSummaries)
                                .filter(summary -> summary.findMatch(appName))
                                .first(AppSummary.Companion.empty())
                                .flatMap(summary -> {
                                    if (summary.isEmpty()) return Single.just(PkgInfo.empty());
                                    if (populateAppInstalledWithSummary(intent, summary)) return Single.just(PkgInfo.ignore());
                                    return mAppRepository.getPkgInfo(summary.getPkgName());
                                }))
                .map(pkgInfo -> {
                    if (pkgInfo.isEmpty() || pkgInfo.isIgnore()) return pkgInfo;
                    File parentFile = ContextUtils.getDownloadParentFile(mContext);
                    Map<String, ApkInfo> apkMap = PkgUtils.getApkInfoMapByDir(mContext, parentFile.getPath());
                    ApkInfo apkInfo = apkMap.get(pkgInfo.getPkgName());
                    if (apkInfo != null) {
                        pkgInfo.setDownloaded(true);
                        pkgInfo.setLocalPath(apkInfo.getPath());
                    }
                    return pkgInfo;
                })
                .compose(RxUtil.singleOnMain())
                .subscribe(pkgInfo -> processIntentWithPkgInfo(intent, pkgInfo), this::populateProcessError);
        addDisposable(disposable);
    }

    private boolean populateAppInstalledWithSummary(AIUIIntent intent, AppSummary summary) {
        String foregroundPkgName = PkgUtils.getForegroundPkgName(mContext);
        if (foregroundPkgName.equals(summary.getPkgName())) return true;
        AppInfo appInfo = PkgUtils.getAppInfo(mContext, summary.getPkgName());
        if (appInfo != null) {
            mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.DONE));
            switch (intent) {
                case DOWNLOAD:
                    mSpeechInteraction.nlpAnswer("设备已有" + summary.getName() + "，无需下载");
                    break;
                case INSTALL:
                    mSpeechInteraction.nlpAnswer("设备已有" + summary.getName() + "，无需安装");
                    break;
                case LAUNCH:
                    PkgUtils.launchApp(mContext, appInfo);
                    mSpeechInteraction.nlpAnswer("已为你打开应用");
                    break;
            }
            return true;
        }
        return false;
    }

    private void processIntentWithPkgInfo(AIUIIntent intent, PkgInfo pkgInfo) {
        if (pkgInfo.isIgnore()) return;
        if (pkgInfo.isEmpty()) {
            mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.EMPTY));
            mSpeechInteraction.nlpAnswer("未找到匹配的应用");
        } else {
            switch (intent) {
                case DOWNLOAD:
                    populateAppDownload(pkgInfo);
                    break;
                case INSTALL:
                    populateAppInstall(pkgInfo);
                    break;
                case LAUNCH:
                    populateAppLaunchWithPkgInfo(pkgInfo, null);
                    break;
            }
        }
    }

    private void populateProcessError(Throwable throwable) {
        mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.FAILED));
        mSpeechInteraction.nlpAnswer("处理过程出错了");
    }

    private void populateAppDownload(PkgInfo pkgInfo) {
        if (pkgInfo.isDownloaded()) {
            ApkInfo apkInfo = PkgUtils.getApkInfoByFile(mContext, new File(pkgInfo.getLocalPath()));
            if (apkInfo != null) {
                mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.DONE));
                mSpeechInteraction.downloadUpdate(ApkDownload.createFromApkInfo(apkInfo));
            }
        } else {
            IAppManager downloadManager = mAppManagerLazy.get();
            if (downloadManager == null) return;
            if (downloadManager.isPkgDownloading()) {
                mToast.showMessage("正在下载应用，请稍候再试");
                return;
            }
            downloadManager.registerListener(new SimpleAppManagerListener() {
                @Override
                public void onAppFailed(boolean isDownloadFailed, ApkDownload download) {
                    super.onAppFailed(isDownloadFailed, download);
                    Timber.tag(TAG).d("populateAppDownload onAppFailed");
                    IEventProcessor eventProcessor = mEventProcessorLazy.get();
                    if (eventProcessor != null && !eventProcessor.isProcessActive()) return;
                    mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.FAILED));
                    mSpeechInteraction.nlpAnswer(download.getFailedReason());
                }
            });
            downloadManager.downloadPkgOnly(pkgInfo);
        }
    }

    private void populateAppInstall(PkgInfo pkgInfo) {
        IAppManager downloadManager = mAppManagerLazy.get();
        if (downloadManager == null) return;
        if (downloadManager.isPkgDownloading() || downloadManager.isPkgInstalling()) {
            mToast.showMessage("正在进行应用的下载安装，请稍候再试");
            return;
        }
        downloadManager.registerListener(new SimpleAppManagerListener() {
            @Override
            public void onAppInstalled(ApkDownload download) {
                super.onAppInstalled(download);
                Timber.tag(TAG).d("populateAppInstall onAppInstalled");
                IEventProcessor eventProcessor = mEventProcessorLazy.get();
                if (eventProcessor != null && !eventProcessor.isProcessActive()) return;
                mSpeechInteraction.nlpAnswer("安装" + pkgInfo.getName() + "成功");
                mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.DONE));
                populateWakeupAfterAppInstall();
            }

            @Override
            public void onAppFailed(boolean isDownloadFailed, ApkDownload download) {
                super.onAppFailed(isDownloadFailed, download);
                Timber.tag(TAG).d("populateAppInstall onAppFailed");
                IEventProcessor eventProcessor = mEventProcessorLazy.get();
                if (eventProcessor != null && !eventProcessor.isProcessActive()) return;
                mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.FAILED));
                mSpeechInteraction.nlpAnswer(download.getFailedReason());
            }
        });
        if (pkgInfo.isDownloaded()) {
            ApkInfo apkInfo = PkgUtils.getApkInfoByFile(mContext, new File(pkgInfo.getLocalPath()));
            if (apkInfo != null) {
                downloadManager.install(apkInfo);
            }
        } else {
            downloadManager.startDownloadPkg(pkgInfo);
        }
    }

    private void populateAppLaunchWithPkgInfo(PkgInfo pkgInfo, @Nullable Intent intent) {
        if (pkgInfo.isIgnore()) return;
        IAppManager downloadManager = mAppManagerLazy.get();
        if (downloadManager == null) return;
        if (downloadManager.isPkgDownloading() || downloadManager.isPkgInstalling()) {
            mToast.showMessage("正在进行应用的下载安装，请稍候再试");
            return;
        }
        downloadManager.registerListener(new SimpleAppManagerListener() {
            @Override
            public void onAppFailed(boolean isDownloadFailed, ApkDownload download) {
                super.onAppFailed(isDownloadFailed, download);
                Timber.tag(TAG).d("populateAppLaunchWithPkgInfo onAppFailed");
                IEventProcessor eventProcessor = mEventProcessorLazy.get();
                if (eventProcessor != null && !eventProcessor.isProcessActive()) return;
                mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.FAILED));
                mSpeechInteraction.nlpAnswer(download.getFailedReason());
            }

            @Override
            public void onAppInstalled(ApkDownload download) {
                super.onAppInstalled(download);
                Timber.tag(TAG).d("populateAppLaunchWithPkgInfo onAppInstalled");
                IEventProcessor eventProcessor = mEventProcessorLazy.get();
                if (eventProcessor != null && !eventProcessor.isProcessActive()) return;
                if (intent == null) {
                    PkgUtils.launchApp(mContext, download.getPkgName());
                    mSpeechInteraction.nlpAnswer("已为你打开应用");
                } else {
                    PkgUtils.launchIntent(mContext, intent);
                    mSpeechInteraction.nlpAnswer("好的");
                }
                mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.DONE));
                populateWakeupAfterAppInstall();
            }
        });
        if (pkgInfo.isDownloaded()) {
            ApkInfo apkInfo = PkgUtils.getApkInfoByFile(mContext, new File(pkgInfo.getLocalPath()));
            if (apkInfo != null) {
                downloadManager.install(apkInfo);
            }
        } else {
            downloadManager.startDownloadPkg(pkgInfo);
        }
    }

    private void populateWakeupAfterAppInstall(){
        ISpeechAgent speechAgent = mSpeechAgentLazy.get();
        if (speechAgent == null) return;
        speechAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, null, null));
    }

    private void addDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    private void clear() {
        mCompositeDisposable.clear();
    }
}
