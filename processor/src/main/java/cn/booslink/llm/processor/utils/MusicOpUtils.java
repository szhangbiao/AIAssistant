package cn.booslink.llm.processor.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioManager;
import android.view.KeyEvent;

import java.lang.reflect.Method;

public class MusicOpUtils {
    public static void requestAudioFocusAndPauseOthers(Context context) {
        AudioManager.OnAudioFocusChangeListener mFocusChangeListener = focusChange -> {
            // 监听焦点变化（可选）
        };
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            // AUDIOFOCUS_GAIN_TRANSIENT: 瞬态焦点，表明我只是临时用一下音频（比如语音助手说话或跳转）
            // 绝大多数播放器收到该信号会主动 pause
            int result = am.requestAudioFocus(mFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // 成功抢占焦点，此时其他音乐播放器已经自动暂停了
            }
        }
    }

    public static void sendMediaPauseKey(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            // 模拟按下暂停键
            KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
            am.dispatchMediaKeyEvent(downEvent);
            // 模拟抬起暂停键
            KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE);
            am.dispatchMediaKeyEvent(upEvent);
        }
    }

    public static void forceStopApp(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.setAccessible(true);
            method.invoke(am, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
