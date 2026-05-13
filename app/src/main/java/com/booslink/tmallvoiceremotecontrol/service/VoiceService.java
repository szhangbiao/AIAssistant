package com.booslink.tmallvoiceremotecontrol.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Nullable;

import cn.booslink.llm.service.WakeUpService;
import timber.log.Timber;

public class VoiceService extends Service {

    private static final String TAG = "VoiceService";
    private static final String PARAMS_KEY = "key";
    private static final String KEY_DOWN = "down";
    private static final String KEY_UP = "up";

    private static final int MSG_KEY_UP = 1;

    // 动态超时策略：
    // 1. 首发延迟（第一发到第二发之间大约 500ms），给予 550ms 裕量
    // 2. 连发延迟（后续大约 100ms 一发），给予 150ms 裕量
    private static final long KEY_UP_TIMEOUT_INITIAL_MS = 550;
    private static final long KEY_UP_TIMEOUT_REPEAT_MS = 150;

    private boolean isKeyDown = false;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_KEY_UP) {
                    handleKeyUp();
                }
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Timber.tag(TAG).d("onStartCommand: Framework called VoiceService");
        // 每次收到调用，取消之前的 UP 计时
        if (mHandler != null) {
            mHandler.removeMessages(MSG_KEY_UP);
        }
        long currentTimeout;
        if (!isKeyDown) {
            isKeyDown = true;
            Timber.tag(TAG).d("Detected KEY DOWN (first call)");
            forwardToWakeUpService(KEY_DOWN);
            // 第一次呼叫，给予较长的等待时间（跨越 500ms 间隙）
            currentTimeout = KEY_UP_TIMEOUT_INITIAL_MS;
        } else {
            // 后续的连发调用，说明已经进入 100ms 的高频连发阶段
            // 我们大幅缩短超时时间，这样松手时能更快触发 UP
            currentTimeout = KEY_UP_TIMEOUT_REPEAT_MS;
        }
        // 重新开始计时
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_KEY_UP, currentTimeout);
        }
        return START_NOT_STICKY;
    }

    private void handleKeyUp() {
        if (isKeyDown) {
            isKeyDown = false;
            Timber.tag(TAG).d("Detected KEY UP (timeout)");
            forwardToWakeUpService(KEY_UP);
        }
    }

    private void forwardToWakeUpService(String keyAction) {
        Intent targetIntent = new Intent(this, WakeUpService.class);
        targetIntent.setAction("com.booslink.aiui.voiceremote.RemoteControlService");
        targetIntent.putExtra(PARAMS_KEY, keyAction);
        startService(targetIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
