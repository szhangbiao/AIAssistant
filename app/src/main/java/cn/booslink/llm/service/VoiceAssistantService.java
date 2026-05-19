package cn.booslink.llm.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import cn.booslink.llm.R;
import cn.booslink.llm.common.cache.IAppCache;
import cn.booslink.llm.common.model.DeviceInfo;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.common.utils.ScreenAdapter;
import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.worker.UpdateCheckWorker;
import dagger.Lazy;
import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class VoiceAssistantService extends Service {
    private static final String TAG = VoiceAssistantService.class.getSimpleName();

    @Inject
    DeviceInfo mDevice;
    @Inject
    ISpeechAgent mSpeechAgent;
    @Inject
    ISpeechInteraction mSpeechInteraction;
    @Inject
    Lazy<IAppCache> mAppCacheLazy;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public VoiceAssistantService getService() {
            return VoiceAssistantService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Timber.tag(TAG).d("onBind");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.tag(TAG).d("onCreate");
        // 创建交互UIView
        // 把View添加到WindowManager
        if (mDevice.isSystemApp()) {
            mSpeechInteraction.attachToWindow();
            keepServiceWithNotification();
        }
        mSpeechAgent.createAgent();
        // 调度每日检查更新任务
        UpdateCheckWorker.schedule(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.tag(TAG).d("onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Timber.tag(TAG).d("onConfigurationChanged");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.tag(TAG).d("onDestroy");
        mSpeechAgent.destroyAgent();
        if (mDevice.isSystemApp()) {
            mSpeechInteraction.detachFromWindow();
        }
        mSpeechInteraction.destroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Timber.tag(TAG).d("onLowMemory");
        IAppCache appCache = mAppCacheLazy.get();
        if (appCache != null) {
            appCache.clearCache();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Timber.tag(TAG).d("onTrimMemory");
    }

    @Override
    public Resources getResources() {
        return ScreenAdapter.adaptWidth(getApplication(), super.getResources(), 1280.0f);
    }

    // 供Activity调用的公共方法
    public void attachActivity(Activity activity) {
        if (mDevice.isSystemApp()) return;
        mSpeechInteraction.attachToActivity(activity);
    }

    public void detachActivity(Activity activity) {
        if (mDevice.isSystemApp()) return;
        mSpeechInteraction.detachFromActivity(activity);
    }

    private void keepServiceWithNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(VoiceAssistantService.class.getName(), TAG, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            builder = new Notification.Builder(this, VoiceAssistantService.class.getName());
        } else {
            builder = new Notification.Builder(this);
        }
        Notification notification = builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(10000, notification);
    }
}
