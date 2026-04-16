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

import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.utils.ScreenAdapter;
import cn.booslink.llm.common.speech.ISpeechAgent;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VoiceAssistantService extends Service {
    private static final String TAG = VoiceAssistantService.class.getSimpleName();
    @Inject
    ISpeechAgent mSpeechAgent;
    @Inject
    ISpeechInteraction mSpeechInteraction;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public VoiceAssistantService getService() {
            return VoiceAssistantService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建交互UIView
        // 把View添加到WindowManager
        if (ContextUtils.isSystemApp(getApplicationContext())) {
            mSpeechInteraction.attachToWindow();
            keepServiceWithNotification();
        }
        mSpeechAgent.createAgent();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeechAgent.destroyAgent();
        if (ContextUtils.isSystemApp(getApplicationContext())) {
            mSpeechInteraction.detachFromWindow();
        }
        mSpeechInteraction.destroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public Resources getResources() {
        return ScreenAdapter.adaptWidth(getApplication(), super.getResources(), 1280.0f);
    }

    // 供Activity调用的公共方法
    public void attachActivity(Activity activity) {
        if (ContextUtils.isSystemApp(getApplicationContext())) return;
        mSpeechInteraction.attachToActivity(activity);
    }

    public void detachActivity(Activity activity) {
        if (ContextUtils.isSystemApp(getApplicationContext())) return;
        mSpeechInteraction.detachFromActivity(activity);
    }

    private void keepServiceWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(VoiceAssistantService.class.getName(), TAG, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager == null) return;
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, VoiceAssistantService.class.getName()).build();
            startForeground(10000, notification);
        } else {
            startForeground(123456, new Notification());
        }
    }
}
