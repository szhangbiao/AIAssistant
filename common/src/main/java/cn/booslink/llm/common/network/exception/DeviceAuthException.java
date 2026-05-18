package cn.booslink.llm.common.network.exception;

import java.io.IOException;

public class DeviceAuthException extends IOException {
    public DeviceAuthException() {
        super("Device auth failed!");
    }

    public DeviceAuthException(String message) {
        super(message);
    }
}
