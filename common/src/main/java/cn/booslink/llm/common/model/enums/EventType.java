package cn.booslink.llm.common.model.enums;

public enum EventType {
    UNKOWN(0),
    RESULT(1),
    ERROR(2),
    STATE(3),
    WAKEUP(4),
    SLEEP(5),
    VAD(6),
    BIND_SUCCESS(7),
    CMD_RETURN(8),
    AUDIO(9),
    PRE_SLEEP(10),
    START_RECORD(11),
    STOP_RECORD(12),
    CONNECTED_TO_SERVER(13),
    SERVER_DISCONNECTED(14),
    TTS(15);

    public static EventType fromType(int type) {
        return EventType.values()[type];
    }

    int type;

    EventType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
