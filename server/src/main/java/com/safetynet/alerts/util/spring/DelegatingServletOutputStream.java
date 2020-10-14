package com.safetynet.alerts.util.spring;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * Delegating implementation of {@link ServletOutputStream}.
 */
public class DelegatingServletOutputStream extends ServletOutputStream {
    private final OutputStream targetStream;

    public DelegatingServletOutputStream(OutputStream targetStream) {
        Objects.requireNonNull(targetStream, "Target OutputStream must not be null");
        this.targetStream = targetStream;
    }

    public final OutputStream getTargetStream() {
        return this.targetStream;
    }

    public void write(int b) throws IOException {
        this.targetStream.write(b);
    }

    public void flush() throws IOException {
        super.flush();
        this.targetStream.flush();
    }

    public void close() throws IOException {
        super.close();
        this.targetStream.close();
    }

    public boolean isReady() {
        return true;
    }

    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }
}
