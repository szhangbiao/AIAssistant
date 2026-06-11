package cn.booslink.llm.common.speech;

import com.iflytek.aiui.AIUIEvent;

public interface ITTSSpeech {
    void speak(String text);

    void ttsState(AIUIEvent event);

    boolean isSpeaking();

    void networkError();

    void cancel();
}
