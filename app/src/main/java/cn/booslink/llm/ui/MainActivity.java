package cn.booslink.llm.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.IBinder;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import cn.booslink.llm.R;
import cn.booslink.llm.common.model.DeviceInfo;
import cn.booslink.llm.common.ui.BaseActivity;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.downloader.IAppManager;
import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.service.VoiceAssistantService;
import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class MainActivity extends BaseActivity {

    @Inject
    DeviceInfo mDevice;
    @Inject
    IAppManager mAppManager;
    @Inject
    ISpeechAgent mSpeechAgent;

    ISpeechInteraction mSpeechInteraction;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 100;

    private VoiceAssistantService voiceAssistantService;
    private boolean isBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Timber.tag(TAG).d("onServiceConnected");
            VoiceAssistantService.LocalBinder binder = (VoiceAssistantService.LocalBinder) service;
            voiceAssistantService = binder.getService();
            isBound = true;
            onVoiceServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            voiceAssistantService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AIAssistant);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        logIntentDetails(intent, "onCreate");
        // 判断是否是从桌面启动
        boolean isFromLauncher = isFromLauncher(intent);
        Timber.tag(TAG).d("isFromLauncher = %b", isFromLauncher);
        if (isFromLauncher && mDevice.isSystemApp() && mSpeechAgent.isAgentCreated()) {
            startActivity(new Intent(this, GuideActivity.class));
            finish();
            return;
        }
        startServiceDirectly();
    }

    private void startServiceDirectly() {
        if (mDevice.isSystemApp()) {
            startService(new Intent(this, VoiceAssistantService.class));
            finish();
            return;
        }
        checkAndRequestPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.tag(TAG).d("onStart");
        if (mDevice.isSystemApp()) return;
        // 检查权限后再绑定Service
        if (hasRecordAudioPermission()) {
            bindVoiceAssistantService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.tag(TAG).d("onResume");
        onVoiceServiceConnected();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.tag(TAG).d("onPause");
    }

    @Override
    protected void onStop() {
        Timber.tag(TAG).d("onStop");
        super.onStop();
        if (isBound) {
            if (voiceAssistantService != null) {
                voiceAssistantService.detachActivity(this);
            }
            unbindService(connection);
            isBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound && voiceAssistantService != null) {
            voiceAssistantService.detachActivity(this);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    private void onVoiceServiceConnected() {
        if (isBound && voiceAssistantService != null) {
            voiceAssistantService.attachActivity(this);
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                // 权限已有，直接启动Service
                startVoiceAssistantService();
            }
        } else {
            // Android 6.0以下，直接启动Service
            startVoiceAssistantService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.tag(TAG).d("录音权限已授权");
                // 权限申请成功，启动和绑定Service
                startVoiceAssistantService();
                bindVoiceAssistantService();
            } else {
                Timber.tag(TAG).d("录音权限被拒绝");
            }
        }
    }

    private boolean hasRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android 6.0以下默认有权限
    }

    private void startVoiceAssistantService() {
        if (!mDevice.isSystemApp()) {
            startService(new Intent(this, VoiceAssistantService.class));
        }
    }

    private void bindVoiceAssistantService() {
        Intent intent = new Intent(this, VoiceAssistantService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    /**
     * 记录Intent的详细信息
     */
    private void logIntentDetails(Intent intent, String source) {
        if (intent == null) {
            Timber.tag(TAG).d("[%s] Intent is null", source);
            return;
        }

        Timber.tag(TAG).d("[%s] Intent details:", source);
        Timber.tag(TAG).d("  Action: %s", intent.getAction());
        Timber.tag(TAG).d("  Categories: %s", intent.getCategories());
        Timber.tag(TAG).d("  Flags: 0x%X", intent.getFlags());
        Timber.tag(TAG).d("  Data: %s", intent.getData());
        Timber.tag(TAG).d("  Extras: %s", intent.getExtras());
        Timber.tag(TAG).d("  Component: %s", intent.getComponent());
        Timber.tag(TAG).d("  Package: %s", intent.getPackage());
        Timber.tag(TAG).d("  Type: %s", intent.getType());
    }

    /**
     * 判断是否是从桌面启动器启动的
     */
    private boolean isFromLauncher(Intent intent) {
        if (intent == null) {
            return false;
        }
        // 方式1: 检查标准的启动器Intent
        boolean hasMainAction = Intent.ACTION_MAIN.equals(intent.getAction());
        boolean hasLauncherCategory = intent.hasCategory(Intent.CATEGORY_LAUNCHER);
        // 方式2: 检查Flags
        boolean hasNewTaskFlag = (intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0;
        // 方式3: 如果activity是被重新带到前台的，也可能是从最近任务进入的
        boolean isLaunchedFromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
        Timber.tag(TAG).d("isFromLauncher check: hasMainAction=%b, hasLauncherCategory=%b, hasNewTaskFlag=%b, isLaunchedFromHistory=%b", hasMainAction, hasLauncherCategory, hasNewTaskFlag, isLaunchedFromHistory);
        // 标准的桌面启动应该是ACTION_MAIN + CATEGORY_LAUNCHER
        // 但有时从最近任务或其他方式启动时，可能没有这些
        return (hasMainAction && hasLauncherCategory) || (hasNewTaskFlag && intent.getAction() == null) || isLaunchedFromHistory;
    }
}
