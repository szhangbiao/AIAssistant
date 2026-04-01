package cn.booslink.llm.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;

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
}
