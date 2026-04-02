package cn.booslink.llm.ui;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import timber.log.Timber;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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
    protected void attachBaseContext(Context newBase) {
        Context context = ScreenUtils.fixFontScale(newBase);
        Context appContext = context.getApplicationContext();
        if (appContext instanceof Application) {
            ScreenAdapter.adaptWidth((Application) appContext, context.getResources(), 1280.0f);
        }
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.tag(TAG).d("onCreate");
        super.onCreate(savedInstanceState);
        ScreenUtils.setupFullScreen(getWindow());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        startAIAssistant();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.tag(TAG).d("onStart");
        if (ContextUtils.isSystemApp(this)) return;
        Intent intent = new Intent(this, VoiceAssistantService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
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

    private void startAIAssistant() {
        startService(new Intent(this, VoiceAssistantService.class));
        if (ContextUtils.isSystemApp(this)) {
            finish();
        }
    }
}
