package com.safetynet.alerts.util.spring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import lombok.Getter;
import org.apache.commons.io.output.TeeOutputStream;

/**
 * An {@link HttpServletResponse} wrapper which copy it's response to {@link #getByteArrayOutputStream()}.
 */
@Getter
public class ByteArrayServletResponse extends HttpServletResponseWrapper {
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public ByteArrayServletResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new DelegatingServletOutputStream(
                new TeeOutputStream(super.getOutputStream(), byteArrayOutputStream));
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new DelegatingServletOutputStream(
                new TeeOutputStream(super.getOutputStream(), byteArrayOutputStream)));
    }

    public byte[] toByteArray() {
        return byteArrayOutputStream.toByteArray();
    }
}
