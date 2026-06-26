package cn.booslink.llm.common.storage;

public interface ISpeechStorage {
    boolean shouldShowLeaveConfirm();

    void setShowLeaveConfirm(boolean show);

    boolean isAuthSuccess();

    void setAuthSuccess(boolean success);

    String getAuthHost();

    void setAuthHost(String host);
}
