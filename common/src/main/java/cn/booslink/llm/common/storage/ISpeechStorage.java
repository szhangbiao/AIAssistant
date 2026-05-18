package cn.booslink.llm.common.storage;

public interface ISpeechStorage {
    boolean shouldShowLeaveConfirm(int type);

    void setShowLeaveConfirm(int type, boolean show);

    long getLastUpdateCheckTime();

    void setLastUpdateCheckTime(long time);

    boolean isAuthSuccess();

    void setAuthSuccess(boolean success);

    String getAuthHost();

    void setAuthHost(String host);
}
