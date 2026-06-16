package cn.booslink.llm.downloader.utils;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.booslink.llm.common.model.ApkInfo;
import cn.booslink.llm.common.model.AppInfo;
import cn.booslink.llm.common.utils.ContextUtils;
import timber.log.Timber;

public class PkgUtils {

    public static final String TAG = "PkgUtils";

    public static Map<String, ApkInfo> getApkInfoMapByDir(Context context, String apkDir) {
        File downloadDir = new File(apkDir);
        File[] apkFiles = downloadDir.listFiles();
        Map<String, ApkInfo> apkMap = new HashMap<>();
        if (apkFiles != null) {
            for (File apkFile : apkFiles) {
                ApkInfo apk = getApkInfoByFile(context, apkFile);
                if (apk != null) {
                    apkMap.put(apk.getPkgName(), apk);
                }
            }
        }
        return apkMap;
    }

    public static ApkInfo getApkInfoByFile(Context context, File apkFile) {
        String fileName = apkFile.getName();
        String fileSuffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
        if (apkFile.exists() && apkFile.isFile() && !TextUtils.isEmpty(fileSuffix) && fileSuffix.equalsIgnoreCase(".apk")) {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), 1);
            if (info == null || info.applicationInfo == null) return null;
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkFile.getAbsolutePath();
            appInfo.publicSourceDir = apkFile.getAbsolutePath();
            ApkInfo apk = new ApkInfo();
            apk.setIcon(appInfo.loadIcon(pm));
            apk.setName(appInfo.loadLabel(pm).toString());
            apk.setPath(apkFile.getAbsolutePath());
            apk.setPkgName(appInfo.packageName);
            apk.setVersionName(info.versionName);
            apk.setVersionCode(info.versionCode);
            apk.setSize(Formatter.formatFileSize(context, apkFile.length()));
            apk.setInstalled(ContextUtils.isAppInstalled(context, appInfo.packageName));
            return apk;
        }
        return null;
    }

    public static AppInfo getAppInfo(Context context, String pkgName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
            // Get launch intent to find the main activity
            Intent launchIntent = pm.getLaunchIntentForPackage(pkgName);
            if (launchIntent == null) {
                return null; // No launch activity found
            }
            String launchActivity = launchIntent.getComponent().getClassName();
            String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
            return new AppInfo(pkgName, appName, packageInfo.versionName, packageInfo.versionCode, launchActivity);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.tag(TAG).e(e, "get app info error for package: %s", pkgName);
            return null;
        }
    }

    public static void launchApp(Context context, String pkgName) {
        if (pkgName == null || pkgName.equals(context.getPackageName())) return;
        AppInfo appInfo = getAppInfo(context, pkgName);
        if (appInfo != null) {
            launchApp(context, appInfo);
        }
    }

    public static void launchApp(Context context, AppInfo appInfo) {
        if (appInfo == null || appInfo.getLaunchActivity() == null) {
            Timber.tag(TAG).w("Cannot launch app: invalid app info or missing launch activity");
            return;
        }
        if (appInfo.getPkgName().equals(context.getPackageName())) return;
        try {
            Intent launchIntent = new Intent();
            launchIntent.setClassName(appInfo.getPkgName(), appInfo.getLaunchActivity());
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 针对 Android 4.4 及以下版本，禁用转场动画，防止 SurfaceFlinger 截取到残留图层渲染出残影
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(launchIntent);
            Timber.tag(TAG).i("Successfully launched app: %s", appInfo.getPkgName());
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Failed to launch app: %s", appInfo.getPkgName());
        }
    }

    public static void launchIntent(Context context, Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 针对 Android 4.4 及以下版本，禁用转场动画，防止 SurfaceFlinger 截取到残留图层渲染出残影
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(intent);
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Failed to launch app");
        }
    }

    public static String getForegroundPkgName(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                // 检查是否有 PACKAGE_USAGE_STATS 权限
                if (!hasUsageStatsPermission(context)) {
                    Timber.tag(TAG).w("PACKAGE_USAGE_STATS permission not granted, trying to grant silently...");
                    grantUsageStatsPermissionSilently(context);
                }

                if (!hasUsageStatsPermission(context)) {
                    Timber.tag(TAG).w("PACKAGE_USAGE_STATS permission not granted");
                    return getForegroundPkgNameLegacy(context);
                }

                UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                if (usageStatsManager == null) {
                    Timber.tag(TAG).e("UsageStatsManager is null");
                    return getForegroundPkgNameLegacy(context);
                }

                long endTime = System.currentTimeMillis();

                // 优先方案：使用高精度、实时的 UsageEvents 检测前台 Activity 切换事件（最近5分钟内）
                long beginTime = endTime - 1000L * 60 * 5;
                android.app.usage.UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
                android.app.usage.UsageEvents.Event event = new android.app.usage.UsageEvents.Event();
                String foregroundPackage = null;
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event);
                    if (event.getEventType() == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED || event.getEventType() == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        foregroundPackage = event.getPackageName();
                    }
                }

                if (!TextUtils.isEmpty(foregroundPackage)) {
                    Timber.tag(TAG).d("Found foreground package via UsageEvents: %s", foregroundPackage);
                    return foregroundPackage;
                }

                // 备选方案：如果最近5分钟内无事件，查询最近1小时（原为10秒，极易为空）的 UsageStats 统计数据
                beginTime = endTime - 1000L * 60 * 60;
                List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);
                Timber.tag(TAG).d("queryUsageStats returned %d items", stats != null ? stats.size() : 0);

                if (stats != null && !stats.isEmpty()) {
                    UsageStats recentStats = null;
                    for (UsageStats usageStats : stats) {
                        if (recentStats == null || usageStats.getLastTimeUsed() > recentStats.getLastTimeUsed()) {
                            recentStats = usageStats;
                        }
                    }
                    if (recentStats != null) {
                        String packageName = recentStats.getPackageName();
                        Timber.tag(TAG).d("Found foreground package via queryUsageStats: %s", packageName);
                        return packageName;
                    }
                }

                Timber.tag(TAG).w("UsageStats is empty, trying legacy method");
                return getForegroundPkgNameLegacy(context);
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "Error in getForegroundPkgName with UsageStatsManager");
                return getForegroundPkgNameLegacy(context);
            }
        } else {
            return getForegroundPkgNameLegacy(context);
        }
    }

    /**
     * 检查是否有 PACKAGE_USAGE_STATS 权限
     */
    private static boolean hasUsageStatsPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                android.app.AppOpsManager appOps = (android.app.AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOps == null) return false;
                int mode = appOps.noteOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
                return mode == android.app.AppOpsManager.MODE_ALLOWED;
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "Error checking usage stats permission");
                return false;
            }
        }
        return true;
    }

    /**
     * 系统签名应用通过反射静默授予 PACKAGE_USAGE_STATS 权限
     */
    private static boolean grantUsageStatsPermissionSilently(Context context) {
        try {
            android.app.AppOpsManager appOps = (android.app.AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (appOps == null) return false;
            java.lang.reflect.Method setModeMethod = appOps.getClass().getMethod("setMode", int.class, int.class, String.class, int.class);
            // 43 是 OP_GET_USAGE_STATS 的代号 (即 AppOpsManager.OP_GET_USAGE_STATS)
            int opCode = 43;
            int uid = android.os.Process.myUid();
            String packageName = context.getPackageName();
            // 0 代表 AppOpsManager.MODE_ALLOWED
            setModeMethod.invoke(appOps, opCode, uid, packageName, 0);
            Timber.tag(TAG).d("Successfully granted PACKAGE_USAGE_STATS permission silently");
            return true;
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Failed to grant PACKAGE_USAGE_STATS permission silently");
            return false;
        }
    }

    /**
     * 降级方法：使用 ActivityManager 获取前台应用
     */
    private static String getForegroundPkgNameLegacy(Context context) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                List<ActivityManager.RunningTaskInfo> infoList = manager.getRunningTasks(1);
                if (infoList != null && !infoList.isEmpty()) {
                    ActivityManager.RunningTaskInfo taskInfo = infoList.get(0);
                    if (taskInfo != null && taskInfo.topActivity != null) {
                        String packageName = taskInfo.topActivity.getPackageName();
                        Timber.tag(TAG).d("Legacy method found foreground package: %s", packageName);
                        return packageName;
                    }
                }
            }
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error in legacy getForegroundPkgName method");
        }
        return null;
    }
}
