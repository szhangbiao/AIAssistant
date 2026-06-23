package cn.booslink.llm.processor.process.control;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;

import javax.inject.Inject;

import cn.booslink.llm.common.speech.ISpeechAgent;
import dagger.Lazy;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

public class ControlProcessImpl implements IControlProcess {

    private static final String TAG = "ControlProcess";

    private final Context mContext;
    private final Lazy<ISpeechAgent> mSpeechAgentLazy;

    @Inject
    public ControlProcessImpl(@ApplicationContext Context context, Lazy<ISpeechAgent> speechAgentLazy) {
        this.mContext = context;
        this.mSpeechAgentLazy = speechAgentLazy;
    }

    @Override
    public void speechSleep() {
        ISpeechAgent speechAgent = mSpeechAgentLazy.get();
        if (speechAgent != null) {
            speechAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_RESET_WAKEUP, 0, 0, null, null));
        }
    }

    @Override
    public void pageBack() {
        try {
            // 使用异步的 Shell 命令发送返回键，避免目标应用 ANR 时阻塞 Instrumentation
            Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (Exception e) {
            // 记录错误日志
            Timber.tag(TAG).e(e, "Failed to simulate back press");
        }
    }

    @Override
    public void backToDesktop() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Failed to send back home intent");
        }
    }
}
