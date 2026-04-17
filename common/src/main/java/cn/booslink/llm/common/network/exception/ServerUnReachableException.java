package cn.booslink.llm.common.network.exception;

import java.io.IOException;

public class ServerUnReachableException extends IOException {
    public ServerUnReachableException() {
        super("Current server is unreachable!");
    }
}
