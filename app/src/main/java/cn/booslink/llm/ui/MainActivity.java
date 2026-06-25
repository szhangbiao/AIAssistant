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
    @Inject
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
        Timber.tag(TAG).d("onCreate");

        if (mDevice.isSystemApp() && mSpeechAgent.isAgentCreated()) {
            Intent intent = new Intent(this, GuideActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        startServiceDirectly();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.tag(TAG).d("onNewIntent");
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
}
