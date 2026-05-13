package cn.booslink.llm.common.model.enums;

public enum Channel {

    UNKNOWN("unknown"), PUB("pub"), BJBS("bjbs"), VOICE("voice");

    private final String channel;

    public static Channel fromChannel(String channel) {
        if (channel == null) {
            return null;
        }
        for (Channel strChannel : Channel.values()) {
            if (strChannel.channel.equals(channel)) {
                return strChannel;
            }
        }
        return UNKNOWN;
    }

    Channel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }
}
