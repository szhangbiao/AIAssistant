package cn.booslink.llm.common.speech;

import com.iflytek.aiui.AIUIMessage;

public interface ISpeechAgent {
    void createAgent();

    void sendMessage(AIUIMessage message);

    void destroyAgent();
}
