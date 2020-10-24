package com.safetynet.alerts.http.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.properties.HttpLoggingProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoggingFilterTest {
    private static final String URI = "https://user:pass@www.api.tld/endpoint?q1=v1&q2=v2";
    private static final String JSON_BODY = "{\"id\" : 125,\n \"name\":\"Nathan\"  } ";
    private static final String TEXT_BODY = "{'\" Hello World! \"'}";
    private static final byte[] BINARY_BODY = new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, (byte) 0x00};

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void doFilter() throws ServletException, IOException {
        List<Context> contexts = Arrays.asList(
                // Disabled
                Context.builder().enabled(false).uri(URI).build(),

                // Request (no body) + Payload disabled
                Context.builder().enabled(true).uri(URI)
                        .info("HTTP < 127.0.0.1 GET \"" + URI + "\"")
                        .info("HTTP > 200 -")
                        .build(),

                // Request (no body) + Payload enabled
                Context.builder().enabled(true).includePayloads(true).uri(URI)
                        .info("HTTP < 127.0.0.1 GET \"" + URI + "\"")
                        .info("HTTP > 200 -")
                        .build(),

                Context.builder().enabled(true).includePayloads(true).method("POST").uri(URI)
                        .info("HTTP < 127.0.0.1 POST \"" + URI + "\"")
                        .info("HTTP > 200 -")
                        .build(),

                // Request (with body) + Payload disabled
                Context.builder().enabled(true).method("POST").uri(URI).body(JSON_BODY)
                        .info("HTTP < 127.0.0.1 POST \"" + URI + "\"")
                        .info("HTTP > 200 -")
                        .build(),

                // Request (with body) + Payload enabled
                Context.builder().enabled(true).includePayloads(true).method("POST").uri(URI).body(JSON_BODY)
                        .info("HTTP < 127.0.0.1 POST \"" + URI + "\" {\"id\":125,\"name\":\"Nathan\"}")
                        .info("HTTP > 200 -")
                        .build(),

                Context.builder().enabled(true).includePayloads(true).method("POST").uri(URI).body(TEXT_BODY)
                        .info("HTTP < 127.0.0.1 POST \"" + URI + "\" \"{'\\\" Hello World! \\\"'}\"")
                        .info("HTTP > 200 -")
                        .build(),

                Context.builder().enabled(true).includePayloads(true).method("POST").uri(URI).body(BINARY_BODY)
                        .info("HTTP < 127.0.0.1 POST \"" + URI + "\" \"[unknown]\"")
                        .info("HTTP > 200 -")
                        .build(),

                // Response (with body) + Payload disabled
                // TODO

                // Response (with body) + Payload enabled
                Context.builder().enabled(true).includePayloads(true).uri(URI).responseType("application/json").responseBody(JSON_BODY)
                        .info("HTTP < 127.0.0.1 GET \"" + URI + "\"")
                        .info("HTTP > 200 - " + JSON_BODY)
                        .build(),

                // Non-200 responses
                Context.builder().enabled(true).uri(URI).responseStatus(203)
                        .info("HTTP < 127.0.0.1 GET \"" + URI + "\"")
                        .info("HTTP > 203 -")
                        .build(),
                Context.builder().enabled(true).uri(URI).responseStatus(302)
                        .info("HTTP < 127.0.0.1 GET \"" + URI + "\"")
                        .info("HTTP > 302 -")
                        .build(),
                Context.builder().enabled(true).uri(URI).responseStatus(404)
                        .info("HTTP < 127.0.0.1 GET \"" + URI + "\"")
                        .error("HTTP > 404 -")
                        .build(),
                Context.builder().enabled(true).uri(URI).responseStatus(500)
                        .info("HTTP < 127.0.0.1 GET \"" + URI + "\"")
                        .error("HTTP > 500 -")
                        .build()
        );

        for (Context ctx : contexts) {
            MockFilterChain chain = new MockFilterChain() {
                @Override
                public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
                    super.doFilter(req, res);
                    ((HttpServletResponse) res).setStatus(ctx.getResponseStatus());
                    res.setContentType(ctx.getResponseType());
                    String body = ctx.getResponseBody();
                    if (body != null) {
                        res.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                    }
                }
            };
            MockHttpServletRequest req = new MockHttpServletRequest(ctx.getMethod(), ctx.getUri());
            {
                Object body = ctx.getBody();
                if (body != null) {
                    if (body instanceof String) {
                        req.setCharacterEncoding("utf-8");
                        req.setContent(((String) body).getBytes(StandardCharsets.UTF_8));
                    } else {
                        req.setCharacterEncoding("unknown");
                        req.setContent((byte[]) body);
                    }
                }
            }
            MockHttpServletResponse res = new MockHttpServletResponse();

            LoggingFilter filter = new LoggingFilter(objectMapper, ctx.getProps());
            filter.doFilter(req, res, chain);

            assertNotNull(chain.getRequest(), "chain.doFilter must always be called");
            assertNotNull(chain.getResponse(), "chain.doFilter must always be called");

            assertEquals(ctx.getExceptedInfos(), filter.getInfos());
            assertEquals(ctx.getExceptedErrors(), filter.getErrors());
        }
    }

    @Getter
    private static class LoggingFilter extends com.safetynet.alerts.http.filter.LoggingFilter {
        private final List<String> infos = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        public LoggingFilter(ObjectMapper objectMapper, HttpLoggingProperties props) {
            super(objectMapper, props);
        }

        @Override
        protected void logInfo(String message) {
            infos.add(message);
        }

        @Override
        protected void logError(String message) {
            errors.add(message);
        }
    }

    @Getter
    private static class Context {
        private final HttpLoggingProperties props;
        private final String method;
        private final String uri;
        private final Object body;
        private final int responseStatus;
        private final String responseType;
        private final String responseBody;
        private final List<String> exceptedInfos;
        private final List<String> exceptedErrors;

        @Builder
        public Context(boolean enabled, boolean includePayloads, String method, String uri, Object body,
                Integer responseStatus, String responseType, String responseBody,
                @Singular("info") List<String> infos, @Singular("error") List<String> errors) {
            props = new HttpLoggingProperties();
            props.setEnabled(enabled);
            props.setIncludePayload(includePayloads);
            this.method = (method == null ? "GET" : method);
            this.uri = Objects.requireNonNull(uri);
            this.body = body;
            this.responseStatus = (responseStatus == null ? 200 : responseStatus);
            this.responseType = responseType;
            this.responseBody = responseBody;
            exceptedInfos = infos;
            exceptedErrors = errors;
        }
    }
}