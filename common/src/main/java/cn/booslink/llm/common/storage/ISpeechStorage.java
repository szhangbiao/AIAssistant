package cn.booslink.llm.common.storage;

public interface ISpeechStorage {
    boolean shouldShowLeaveConfirm();

    void setShowLeaveConfirm(boolean show);

    long getLastUpdateCheckTime();

    void setLastUpdateCheckTime(long time);

    boolean isAuthSuccess();

    void setAuthSuccess(boolean success);

    String getAuthHost();

    void setAuthHost(String host);
}
