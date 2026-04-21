package cn.booslink.llm.common.ui;

public interface IToast {
    void showMessage(String message);

    void release();
}
