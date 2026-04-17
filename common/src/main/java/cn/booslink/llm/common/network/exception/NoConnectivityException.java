package cn.booslink.llm.common.network.exception;

import java.io.IOException;

public class NoConnectivityException extends IOException {
    public NoConnectivityException() {
        super("网络已断开");
    }
}
