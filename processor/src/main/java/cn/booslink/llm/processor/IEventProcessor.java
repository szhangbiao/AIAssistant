package cn.booslink.llm.processor;

import com.iflytek.aiui.AIUIEvent;

public interface IEventProcessor {

    void processEvent(AIUIEvent event);

    void release();
}
