package cn.booslink.llm.common.speech;

public interface ISpeechStatus {
    void wakeup();

    void sleep();

    void reset();

    boolean isTimeout();
}
