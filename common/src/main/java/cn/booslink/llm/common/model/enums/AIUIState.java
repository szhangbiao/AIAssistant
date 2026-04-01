package cn.booslink.llm.common.model.enums;

public enum AIUIState {
    UNKNOWN(0), IDLE(1), READ(2), WORKING(3);
    private final int state;

    AIUIState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
