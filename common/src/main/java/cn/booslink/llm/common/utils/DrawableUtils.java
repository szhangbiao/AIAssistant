package cn.booslink.llm.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.io.File;

public class DrawableUtils {

    public static Drawable getApkIcon(Context context, String apkPath) {
        File apkFile = new File(apkPath);
        if (apkFile.exists() && apkFile.isFile() && apkFile.getName().endsWith(".apk")) {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            if (info != null && info.applicationInfo != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                appInfo.sourceDir = apkFile.getAbsolutePath();
                appInfo.publicSourceDir = apkFile.getAbsolutePath();
                return appInfo.loadIcon(pm);
            }
        }
        return null;
    }

    public static Drawable getAppIcon(Context context, String pkgName) {
        if (pkgName != null && !TextUtils.isEmpty(pkgName)) {
            try {
                return context.getPackageManager().getApplicationIcon(pkgName);
            } catch (PackageManager.NameNotFoundException var3) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
