package cn.booslink.llm.common.model.enums;

public enum QueryState {
    IDLE,
    WAKE_UP,
    QUERYING,
    DOWNLOADING,
    DONE,
    EMPTY,
    FAILED,
    ERROR;
}
