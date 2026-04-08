package cn.booslink.llm.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;

import java.io.File;

public class ContextUtils {

    public static boolean isSystemApp(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            boolean isSysApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
            boolean isSysUpdate = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
            return isSysApp || isSysUpdate || appInfo.uid <= Process.SYSTEM_UID;
        } catch (PackageManager.NameNotFoundException e) {
            // 应用未找到异常，可能是因为参数传递错误或者其他原因
            e.printStackTrace();
            return false;
        }
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static File getDownloadParentFile(Context context) {
        File parentDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            parentDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        } else {
            parentDir = new File(context.getFilesDir(), Environment.DIRECTORY_DOWNLOADS);
        }
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        return parentDir;
    }

    public static String getDownloadFilePath(Context context, String oldFileName, String newFileName) {
        File parentFile = getDownloadParentFile(context);
        File newFile = new File(parentFile, newFileName);
        return newFile.exists() ? newFile.getAbsolutePath() : new File(parentFile, oldFileName).getAbsolutePath();
    }

    public static File getDownloadFile(Context context, String oldFileName, String newFileName) {
        File parentFile = getDownloadParentFile(context);
        File newFile = new File(parentFile, newFileName);
        return newFile.exists() ? newFile : new File(parentFile, oldFileName);
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean checkAppExits(Context context, String pkgName) {
        if (pkgName != null && !TextUtils.isEmpty(pkgName)) {
            try {
                context.getPackageManager().getApplicationInfo(pkgName, 8192);
                return true;
            } catch (PackageManager.NameNotFoundException var3) {
                return false;
            }
        } else {
            return false;
        }
    }
}
