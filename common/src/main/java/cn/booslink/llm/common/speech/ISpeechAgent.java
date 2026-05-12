package cn.booslink.llm.common.speech;

import com.iflytek.aiui.AIUIMessage;

public interface ISpeechAgent {
    void createAgent(Runnable aiuiCallback);

    void sendMessage(AIUIMessage message);

    boolean isAIUIWorking();

    void destroyAgent();
}
