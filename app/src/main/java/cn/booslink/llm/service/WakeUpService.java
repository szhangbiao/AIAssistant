package cn.booslink.llm.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class WakeUpService extends Service {

    private static final String WAKEUP_ACTION = "cn.booslink.llm.service.WakeUpService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // 获取传递的参数
            String action = intent.getAction();
            String key = intent.getStringExtra("key");
            // 处理语音按键事件
            if (WAKEUP_ACTION.equals(action) && key != null) {
                if ("down".equals(key)) {
                    // 按下语音键
                    handleVoiceKeyDown();
                } else if ("up".equals(key)) {
                    // 松开语音键
                    handleVoiceKeyUp();
                }
            } else {
                // 处理其他类型的启动
                handleOtherActions(action, intent);
            }
        }

        // 返回值决定Service被系统杀死后的行为
        return START_STICKY; // Service被杀死后会自动重启
    }

    private void handleVoiceKeyDown() {
        // 处理按下语音键逻辑
        android.util.Log.d("WakeUpService", "Voice key pressed down");
        // 开始录音、显示UI等操作
    }

    private void handleVoiceKeyUp() {
        // 处理松开语音键逻辑
        android.util.Log.d("WakeUpService", "Voice key released");
        // 停止录音、处理语音数据等操作
    }

    private void handleOtherActions(String action, Intent intent) {
        // 处理其他类型的Action
        android.util.Log.d("WakeUpService", "Handle other action: " + action);
    }
}
