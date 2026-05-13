package cn.booslink.llm.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import cn.booslink.llm.record.IVoiceInput;
import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class WakeUpService extends Service {

    private static final String TAG = "WakeUpService";

    private static final String WAKEUP_ACTION_MAIN = "cn.booslink.llm.WakeUpService";
    private static final String REMOTE_CONTROL_ACTION = "com.booslink.aiui.voiceremote.RemoteControlService";

    private static final String PARAMS_KEY = "key";
    private static final String KEY_DOWN = "down";
    private static final String KEY_UP = "up";

    @Inject
    IVoiceInput mVoiceInput;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.tag(TAG).d("onStartCommand");
        if (intent != null) {
            // 获取传递的参数
            String action = intent.getAction();
            String key = intent.getStringExtra(PARAMS_KEY);
            // 处理语音按键事件
            if ((WAKEUP_ACTION_MAIN.equals(action) || REMOTE_CONTROL_ACTION.equals(action)) && key != null) {
                if (KEY_DOWN.equals(key)) {
                    // 按下语音键
                    handleVoiceKeyDown();
                } else if (KEY_UP.equals(key)) {
                    // 松开语音键
                    handleVoiceKeyUp();
                }
            } else {
                // 处理其他类型的启动
                handleOtherActions(action, intent);
            }
        }
        // 返回值决定Service被系统杀死后的行为
        return START_NOT_STICKY; // Service被杀死后会自动重启
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.tag(TAG).d("onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVoiceInput.release();
        Timber.tag(TAG).d("onDestroy");
    }

    private void handleVoiceKeyDown() {
        // 处理按下语音键逻辑
        Timber.tag(TAG).d("Voice key pressed down");
        mVoiceInput.startVoice();
    }

    private void handleVoiceKeyUp() {
        // 处理松开语音键逻辑
        Timber.tag(TAG).d("Voice key released");
        mVoiceInput.stopVoice();
    }

    private void handleOtherActions(String action, Intent intent) {
        // 处理其他类型的Action
        Timber.tag(TAG).d("Handle other action: %s", action);
    }
}
