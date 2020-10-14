package com.safetynet.alerts.util.exception;

public class FastException extends Exception {
    @Override
    public Throwable initCause(Throwable throwable) {
        return this;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
