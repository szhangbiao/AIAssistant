package cn.booslink.llm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import timber.log.Timber;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 监听多种开机事件
        if (intent != null) {
            String action = intent.getAction();
            Timber.tag(TAG).d("Received broadcast: %s", action);
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                    // || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                    || "android.intent.action.QUICKBOOT_POWERON".equals(action)
                    || "com.htc.intent.action.QUICKBOOT_POWERON".equals(action)) {
                Timber.tag(TAG).d("Device boot completed or app upgraded, starting app auto-start process");
                // 延迟启动，确保系统完全启动
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startMainActivity(context);
                }, 2000); // 延迟2秒
            }
        }
    }

    private void startMainActivity(Context context) {
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(launchIntent);
                Timber.tag(TAG).d("Main activity started successfully");
            } else {
                Timber.tag(TAG).e("Launch intent not found");
            }
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Failed to start main activity");
        }
    }
}
