package cn.booslink.llm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import timber.log.Timber;

public class VoiceKeyReceiver extends BroadcastReceiver {

    private static final String TAG = "VoiceKeyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.tag(TAG).d("onReceive");
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null) {
                Timber.tag(TAG).d("收到媒体按键事件: Action=%s, KeyCode=%d", event.getAction() == KeyEvent.ACTION_DOWN ? "DOWN" : "UP", event.getKeyCode());
                // 处理按键事件
                handleMediaKeyEvent(context, event);
            }
        }
    }

    private void handleMediaKeyEvent(Context context, KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        // 检查是否为语音按键
        if (isVoiceKey(keyCode)) {
            if (action == KeyEvent.ACTION_DOWN) {
                onVoiceKeyPressed(context, keyCode, event);
            } else if (action == KeyEvent.ACTION_UP) {
                onVoiceKeyReleased(context, keyCode, event);
            }
        }
    }

    private boolean isVoiceKey(int keyCode) {
        // 语音按键判断逻辑
        return keyCode == KeyEvent.KEYCODE_SEARCH ||
                //keyCode == KeyEvent.KEYCODE_VOICE ||
                //keyCode == KeyEvent.KEYCODE_MIC ||
                keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                keyCode == 166 || // 小米语音键
                keyCode == 167 || // 小米语音键
                keyCode == 168 || // 小米语音键
                keyCode == 169;   // 小米语音键
    }

    private void onVoiceKeyPressed(Context context, int keyCode, KeyEvent event) {
        Timber.tag(TAG).d("语音按键按下: %d", keyCode);
        // 启动Service，并传递开始录音参数

    }

    private void onVoiceKeyReleased(Context context, int keyCode, KeyEvent event) {
        Timber.tag(TAG).d("语音按键释放: %d", keyCode);
        // 延时1s启动Service，并传递停止录音参数
    }
}
