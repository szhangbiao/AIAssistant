package cn.booslink.llm.common.network.exception;

import java.io.IOException;

public class RetryFailException extends IOException {
    private final Throwable exception;

    public RetryFailException(Throwable exception) {
        super("Api retry failed");
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }
}
