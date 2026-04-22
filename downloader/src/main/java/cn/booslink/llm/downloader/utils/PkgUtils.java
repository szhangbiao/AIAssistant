package cn.booslink.llm.downloader.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

    public static AppInfo getAppInfo2(Context context, String pkgName) {
        List<ResolveInfo> allApps = getAllApps(context);
        if (!allApps.isEmpty()) {
            for (ResolveInfo info : allApps) {
                if (info.activityInfo.packageName.equals(pkgName)) {
                    return getAppInfo(context, info);
                }
            }
        }
        return null;
    }

    private static AppInfo getAppInfo(Context context, ResolveInfo res) {
        String launchActivity = res.activityInfo.name;
        String pkgName = res.activityInfo.packageName;
        String appName = res.loadLabel(context.getPackageManager()).toString();
        int versionCode = 0;
        String versionName = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            versionCode = packageInfo.versionCode;
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.tag(TAG).e(e, "get app info error");
        }
        return new AppInfo(pkgName, appName, versionName, versionCode, launchActivity);
    }

    private static List<ResolveInfo> getAllApps(Context context) {
        Intent appIntent = new Intent("android.intent.action.MAIN");
        appIntent.setAction("android.intent.action.MAIN");
        appIntent.addCategory("android.intent.category.LAUNCHER");
        return context.getPackageManager().queryIntentActivities(appIntent, 0);
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
        if (pkgName.equals(context.getPackageName())) return;
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
            context.startActivity(launchIntent);
            Timber.tag(TAG).i("Successfully launched app: %s", appInfo.getPkgName());
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Failed to launch app: %s", appInfo.getPkgName());
        }
    }

    public static void launchIntent(Context context, Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Failed to launch app");
        }
    }
}
