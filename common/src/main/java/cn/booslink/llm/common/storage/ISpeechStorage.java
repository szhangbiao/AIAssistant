package cn.booslink.llm.common.storage;

public interface ISpeechStorage {
    boolean shouldShowLeaveConfirm(int type);

    void setShowLeaveConfirm(int type, boolean show);
}
