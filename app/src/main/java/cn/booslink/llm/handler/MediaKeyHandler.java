package cn.booslink.llm.handler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import timber.log.Timber;

public class MediaKeyHandler {

    private static final String TAG = "MediaKeyHandler";

    private final Context mContext;
    private AudioManager mAudioManager;
    private MediaSessionCompat mMediaSession;
    private ComponentName mMediaButtonReceiver;

    public MediaKeyHandler(Context context) {
        this.mContext = context;
        setupMediaSession();
        setupAudioManager();
    }

    public void requestAudioFocus() {
        Timber.tag(TAG).d("requestAudioFocus");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setOnAudioFocusChangeListener(focusChange -> {
                        // 处理焦点改变
                        Timber.tag(TAG).d("Audio Focused = %b", focusChange == AudioManager.AUDIOFOCUS_GAIN);
                    })
                    .build();
            mAudioManager.requestAudioFocus(audioFocusRequest);
        } else {
            mAudioManager.requestAudioFocus(focusChange -> {
                // 处理焦点改变
                Timber.tag(TAG).d("Audio Focused = %b", focusChange == AudioManager.AUDIOFOCUS_GAIN);
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
        }
    }

    public void cleanup() {
        if (mAudioManager != null && mMediaButtonReceiver != null) {
            mAudioManager.unregisterMediaButtonEventReceiver(mMediaButtonReceiver);
        }
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
            mMediaSession.release();
        }
    }

    private void setupMediaSession() {
        Timber.tag(TAG).d("setupMediaSession");
        mMediaButtonReceiver = new ComponentName(mContext, "cn.booslink.llm.receiver.VoiceKeyReceiver");
        // 服务常驻后台，不需要PendingIntent，使用null即可
        mMediaSession = new MediaSessionCompat(mContext, TAG, mMediaButtonReceiver, null);
        // 设置MediaSession的标志，确保能接收媒体按键
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // 设置回调处理按键
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                Timber.tag(TAG).d("onMediaButtonEvent");
                KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event != null) {
                    return handleKeyEvent(event);
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        });
        mMediaSession.setActive(true);
    }

    private void setupAudioManager() {
        Timber.tag(TAG).d("setupAudioManager");
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiver);
    }

    private boolean handleKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        Timber.tag(TAG).d("按键事件: Action=%s, KeyCode=%d, Label=%s", action == KeyEvent.ACTION_DOWN ? "DOWN" : "UP", keyCode, KeyEvent.keyCodeToString(keyCode));
        // 检查是否为语音按键
        if (isVoiceKey(keyCode)) {
            if (action == KeyEvent.ACTION_DOWN) {
                onVoiceKeyPressed(keyCode, event);
            } else if (action == KeyEvent.ACTION_UP) {
                onVoiceKeyReleased(keyCode, event);
            }
            return true;
        }
        return false;
    }

    private boolean isVoiceKey(int keyCode) {
        // 语音按键判断逻辑
        return keyCode == KeyEvent.KEYCODE_SEARCH ||
                //keyCode == KeyEvent.KEYCODE_VOICE ||
                //keyCode == KeyEvent.KEYCODE_MIC ||
                keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                keyCode == 166 || // 小米语音键
                keyCode == 167 || // 华为语音键
                keyCode == 168 || // 创维语音键
                keyCode == 169;   // 海信语音键
    }

    private void onVoiceKeyPressed(int keyCode, KeyEvent event) {
        Timber.tag(TAG).d("语音按键按下: %d", keyCode);
        // 启动Service，并传递开始录音参数
    }

    private void onVoiceKeyReleased(int keyCode, KeyEvent event) {
        Timber.tag(TAG).d("语音按键释放: %d", keyCode);
        // 延时1s启动Service，并传递停止录音参数
    }
}
