package cn.booslink.llm.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import cn.booslink.llm.R;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.utils.ScreenAdapter;
import cn.booslink.llm.common.utils.ScreenUtils;
import cn.booslink.llm.service.VoiceAssistantService;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private VoiceAssistantService voiceAssistantService;
    private boolean isBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
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
        super.onCreate(savedInstanceState);
        ScreenUtils.setupFullScreen(getWindow());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        startAIAssistant();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextUtils.isSystemApp(this)) return;
        Intent intent = new Intent(this, VoiceAssistantService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
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
    protected void onResume() {
        super.onResume();
        if (isBound && voiceAssistantService != null) {
            voiceAssistantService.attachActivity(this);
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

    @Override
    public Resources getResources() {
        return ScreenAdapter.adaptWidth(getApplication(), super.getResources(), 1280.0f);
    }

    private void onVoiceServiceConnected() {
        if (voiceAssistantService != null) {
            voiceAssistantService.attachActivity(this);
        }
    }

    private void startAIAssistant() {
        startService(new Intent(this, VoiceAssistantService.class));
        if (ContextUtils.isSystemApp(this)) {
            finish();
        }
    }
}
