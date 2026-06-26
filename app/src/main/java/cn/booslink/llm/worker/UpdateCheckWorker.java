package cn.booslink.llm.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.booslink.llm.common.model.AppUpgrade;
import cn.booslink.llm.common.model.DeviceInfo;
import cn.booslink.llm.downloader.AppUpgradeManager;
import cn.booslink.llm.speech.repository.IUpgradeRepository;
import dagger.Lazy;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import timber.log.Timber;

@HiltWorker
public class UpdateCheckWorker extends Worker {

    private static final String TAG = "UpdateCheckWorker";
    private static final String PERIODIC_WORK_NAME = "DailyUpdateCheck";
    private static final String STARTUP_WORK_NAME = "StartupUpdateCheck";

    private final DeviceInfo mDevice;
    private final IUpgradeRepository mUpgradeRepository;
    private final Lazy<AppUpgradeManager> mAppUpgradeManagerLazy;

    @AssistedInject
    public UpdateCheckWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters workerParams, DeviceInfo deviceInfo, IUpgradeRepository upgradeRepository, Lazy<AppUpgradeManager> appUpgradeManagerLazy) {
        super(context, workerParams);
        this.mDevice = deviceInfo;
        this.mUpgradeRepository = upgradeRepository;
        this.mAppUpgradeManagerLazy = appUpgradeManagerLazy;
    }

    /**
     * 调度检查更新任务
     * 1. 立即执行一次 OneTimeWork
     * 2. 调度每 24 小时的 PeriodicWork
     */
    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        // 1. 立即强制执行一次检查
        OneTimeWorkRequest startupRequest = new OneTimeWorkRequest.Builder(UpdateCheckWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                STARTUP_WORK_NAME,
                ExistingWorkPolicy.REPLACE, // 每次启动都替换旧的，确保执行
                startupRequest
        );
        // 2. 调度每 24 小时的周期性检查
        PeriodicWorkRequest periodicRequest = new PeriodicWorkRequest.Builder(UpdateCheckWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        // 保持 24 小时周期不变
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, periodicRequest);
        Timber.tag(TAG).d("Enqueued startup check and scheduled 24h periodic check");
    }

    @NonNull
    @Override
    public Result doWork() {
        Timber.tag(TAG).d("Starting update check task...");

        try {
            boolean hasNewVersion = checkUpgrade();
            Timber.tag(TAG).d("Update check task finished, hasNewVersion: %b", hasNewVersion);
            return Result.success();
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error during update check");
            return Result.retry();
        }
    }

    private boolean checkUpgrade() {
        AppUpgrade upgrade = mUpgradeRepository.getAppUpgrade().blockingGet();
        int remoteVersion = upgrade.getVersion();
        int currentVersion = mDevice.getVersionCode();
        if (remoteVersion > currentVersion) {
            Timber.tag(TAG).i("New version found, starting download...");
            AppUpgradeManager upgradeManager = mAppUpgradeManagerLazy.get();
            if (upgradeManager == null) return false;
            upgradeManager.startDownload(upgrade);
            return true;
        }
        return false;
    }
}
