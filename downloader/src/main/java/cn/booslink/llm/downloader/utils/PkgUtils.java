package cn.booslink.llm.downloader.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.booslink.llm.common.model.ApkInfo;
import cn.booslink.llm.common.model.AppInfo;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.utils.DrawableUtils;
import timber.log.Timber;

public class PkgUtils {

    public static final String TAG = "PkgUtils";

    public static List<AppInfo> getAppsWithoutLauncher(Context context) {
        List<AppInfo> appList = new ArrayList<>();
        List<ResolveInfo> allApps = getAllApps(context);
        List<ResolveInfo> homeApps = getHomeApps(context);
        if (!allApps.isEmpty()) {
            for (int i = 0; i < allApps.size(); ++i) {
                ResolveInfo res = allApps.get(i);
                String pkgName = res.activityInfo.packageName;
                boolean needRemove = needRemove(context, pkgName, homeApps);
                if (!needRemove) {
                    appList.add(getAppInfo(context, res));
                }
            }
        }
        return appList;
    }

    public static List<AppInfo> getCanUninstallApps(Context context) {
        List<AppInfo> appList = new ArrayList<>();
        List<ResolveInfo> allApps = getAllApps(context);
        List<ResolveInfo> homeApps = getHomeApps(context);
        if (!allApps.isEmpty()) {
            for (ResolveInfo resolveInfo : allApps) {
                String pkgName = resolveInfo.activityInfo.packageName;
                boolean homeOrSystemApp = needRemove(context, pkgName, homeApps) || isSystemApp(resolveInfo.activityInfo.applicationInfo);
                if (!homeOrSystemApp) {
                    appList.add(getAppInfo(context, resolveInfo));
                }
            }
        }
        return appList;
    }

    public static List<ApkInfo> getApkInfoByDir(Context context, String apkDir) {
        File downloadDir = new File(apkDir);
        File[] apkFiles = downloadDir.listFiles();
        List<ApkInfo> apkList = new ArrayList<>();
        if (apkFiles != null) {
            for (File apkFile : apkFiles) {
                ApkInfo apk = getApkInfoByFile(context, apkFile);
                if (apk != null) {
                    apkList.add(apk);
                }
            }
        }
        return apkList;
    }

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
            if(info == null || info.applicationInfo == null) return null;
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

    private static boolean needRemove(Context context, String pkgName, List<ResolveInfo> homeApps) {
        if (!TextUtils.isEmpty(pkgName) && ContextUtils.checkAppExits(context, pkgName)) {
            if (pkgName.equals(context.getPackageName())) {
                return true;
            } else {
                if (homeApps != null && !homeApps.isEmpty()) {
                    for (int i = 0; i < homeApps.size(); ++i) {
                        String homePkgName = homeApps.get(i).activityInfo.packageName;
                        if (pkgName.equals(homePkgName)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } else {
            return true;
        }
    }


    public static AppInfo getAppInfo(Context context, String pkgName) {
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

    public static AppInfo getUninstallAppInfo(Context context, String pkgName) {
        List<ResolveInfo> allApps = getAllApps(context);
        List<ResolveInfo> homeApps = getHomeApps(context);
        if (!allApps.isEmpty()) {
            for (ResolveInfo info : allApps) {
                if (info.activityInfo.packageName.equals(pkgName)) {
                    boolean homeOrSystemApp = needRemove(context, pkgName, homeApps) || isSystemApp(info.activityInfo.applicationInfo);
                    return homeOrSystemApp ? null : getAppInfo(context, info);
                }
            }
        }
        return null;
    }

    private static AppInfo getAppInfo(Context context, ResolveInfo res) {
        final String pkgName = res.activityInfo.packageName;
        String appName = res.loadLabel(context.getPackageManager()).toString();
        Drawable appIcon = res.loadIcon(context.getPackageManager());
        if (appIcon == null) {
            appIcon = DrawableUtils.getAppIcon(context, pkgName);
        }
        String appSize = "";
        int versionCode = 0;
        String versionName = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            File file = new File(packageInfo.applicationInfo.sourceDir);
            appSize = Formatter.formatFileSize(context, file.length());
            versionCode = packageInfo.versionCode;
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.tag(TAG).e(e, "get app info error");
        }
        return new AppInfo(pkgName, appName, appIcon, versionName, versionCode, appSize);
    }

    private static boolean isSystemApp(ApplicationInfo appInfo) {
        boolean isSysApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        boolean isSysUpd = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        return isSysApp || isSysUpd;
    }

    private static List<ResolveInfo> getHomeApps(Context context) {
        Intent homeIntent = new Intent("android.intent.action.MAIN");
        homeIntent.addCategory("android.intent.category.HOME");
        homeIntent.addCategory("android.intent.category.LAUNCHER");
        return context.getPackageManager().queryIntentActivities(homeIntent, 0);
    }

    private static List<ResolveInfo> getAllApps(Context context) {
        Intent appIntent = new Intent("android.intent.action.MAIN");
        appIntent.setAction("android.intent.action.MAIN");
        appIntent.addCategory("android.intent.category.LAUNCHER");
        return context.getPackageManager().queryIntentActivities(appIntent, 0);
    }
}
