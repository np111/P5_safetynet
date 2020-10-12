package com.safetynet.alerts.util.spring;

import java.io.ByteArrayInputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * An {@link HttpServletRequest} wrapper with {@link HttpServletRequest#getInputStream()} based on a byte array.
 */
public class ByteArrayServletRequest extends HttpServletRequestWrapper {
    private final ServletInputStream inputStream;

    public ByteArrayServletRequest(HttpServletRequest request, byte[] input) {
        super(request);
        final ByteArrayInputStream stream = new ByteArrayInputStream(input);
        this.inputStream = new ServletInputStream() {
            private boolean finished;

            @Override
            public boolean isFinished() {
                return finished;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException("not supported yet");
            }

            @Override
            public int read() {
                int r = stream.read();
                if (r == -1) {
                    finished = true;
                }
                return r;
            }
        };
    }

    @Override
    public ServletInputStream getInputStream() {
        return inputStream;
    }
}
