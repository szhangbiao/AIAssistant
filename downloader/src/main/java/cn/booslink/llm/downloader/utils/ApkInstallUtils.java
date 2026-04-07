package cn.booslink.llm.downloader.utils;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;

import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.booslink.llm.downloader.observer.PackageInstallObserver;
import cn.booslink.llm.downloader.service.SilentInstallService;
import timber.log.Timber;

public class ApkInstallUtils {

    @WorkerThread
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static void installerInstall(Context context, String apkPath) throws Exception {
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) return;
        PackageInstaller.Session session = null;
        try {
            PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setSize(apkFile.length());
            //创建一个Session
            int sessionId = packageInstaller.createSession(params);
            //建立和PackageManager的socket通道，Android中的通信不仅仅有Binder还有很多其它的
            session = packageInstaller.openSession(sessionId);
            //将App的内容通过session传输
            addApkToInstallSession(apkFile, session);
            // 创建一个安装接收器
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), "com.yunqinglai.manager.service.AppSilentService"));
            intent.setAction(SilentInstallService.ACTION_SILENT_INSTALL);
            int flags;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
            } else {
                flags = PendingIntent.FLAG_UPDATE_CURRENT;
            }
            PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, flags);
            session.commit(pendingIntent.getIntentSender());
        } catch (IOException | RuntimeException e) {
            Timber.tag("Install").e(e, "Install failed");
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static void addApkToInstallSession(File apkFile, PackageInstaller.Session session) throws IOException {
        FileInputStream in = null;
        OutputStream out = null;
        try {
            out = session.openWrite("base.apk", 0L, apkFile.length());
            in = new FileInputStream(apkFile);
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            session.fsync(out);
        } catch (IOException e) {
            Timber.tag("Install").e(e, "Session write to file failed");
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * 由系统提供的api进行安装
     *
     * @param context 上下文
     * @param apkPath 安装包路径
     */
    @WorkerThread
    public static void customInstall(Context context, String apkPath, PackageInstallObserver observer) {
        // context.getPackageManager().installPackage(Uri.parse("file://" + apkPath), observer, 2, context.getPackageName());
        Uri uri = Uri.parse("file://" + apkPath);
        try {
            Method method = PackageManager.class.getMethod("installPackage", Uri.class, IPackageInstallObserver.class, int.class, String.class);
            method.invoke(context.getPackageManager(), uri, observer, 2, context.getPackageName());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @WorkerThread
    public static void install(Context context, String apkPath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= 29) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", new File(apkPath));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else if (Build.VERSION.SDK_INT >= 24) {
            StrictMode.VmPolicy.Builder strictBuilder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(strictBuilder.build());
            uri = Uri.fromFile(new File(apkPath));
        } else {
            uri = Uri.fromFile(new File(apkPath));
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
