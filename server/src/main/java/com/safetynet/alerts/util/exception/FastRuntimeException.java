package com.safetynet.alerts.util.exception;

public class FastRuntimeException extends RuntimeException {
    @Override
    public Throwable initCause(Throwable throwable) {
        return this;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
