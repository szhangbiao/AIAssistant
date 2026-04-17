package cn.booslink.llm.common.network.exception;

import java.io.IOException;

public class PingPublicNetException extends IOException {
    public PingPublicNetException() {
        super("Ping public network failed");
    }
}
