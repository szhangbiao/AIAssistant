package cn.booslink.llm.processor.process.control;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;

import javax.inject.Inject;

import cn.booslink.llm.common.speech.ISpeechAgent;
import dagger.Lazy;

public class ControlProcessImpl implements IControlProcess {
    private final Lazy<ISpeechAgent> mSpeechAgentLazy;

    @Inject
    public ControlProcessImpl(Lazy<ISpeechAgent> speechAgentLazy) {
        this.mSpeechAgentLazy = speechAgentLazy;
    }

    @Override
    public void speechSleep() {
        ISpeechAgent speechAgent = mSpeechAgentLazy.get();
        if (speechAgent != null) {
            speechAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_RESET_WAKEUP, 0, 0, null, null));
        }
    }
}
