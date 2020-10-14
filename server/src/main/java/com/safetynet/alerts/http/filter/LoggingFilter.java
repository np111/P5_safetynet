package com.safetynet.alerts.http.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.properties.HttpLoggingProperties;
import com.safetynet.alerts.util.spring.ByteArrayServletRequest;
import com.safetynet.alerts.util.spring.ByteArrayServletResponse;
import com.safetynet.alerts.util.spring.RequestWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Scope("singleton")
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String DEFAULT_ENCODING = "iso-8859-1";

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final boolean includePayload;

    @Autowired
    public LoggingFilter(ObjectMapper objectMapper, HttpLoggingProperties props) {
        this.objectMapper = objectMapper;
        this.enabled = props.isEnabled();
        this.includePayload = props.isIncludePayload();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!shouldLog(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        RequestWrapper requestWrapper = new RequestWrapper(request);
        boolean isFirstRequest = !isAsyncDispatch(request);
        if (isFirstRequest) {
            logBefore(requestWrapper);
        }

        try {
            if (includePayload) {
                response = new ByteArrayServletResponse(response);
            }
            filterChain.doFilter(requestWrapper.getRequest(), response);
        } finally {
            if (!isAsyncStarted(requestWrapper.getRequest())) {
                logAfter(requestWrapper, response);
            }
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    private boolean shouldLog(HttpServletRequest request) {
        return enabled;
    }

    private void logBefore(RequestWrapper requestWrapper) {
        HttpServletRequest request = requestWrapper.getRequest();

        StringBuilder msg = new StringBuilder();
        msg.append("HTTP <");

        msg.append(' ').append(request.getRemoteAddr());

        msg.append(' ').append(request.getMethod());
        msg.append(' ').append(jsonString(getFullUri(request)));

        if (includePayload) {
            String payload = getMessagePayload(requestWrapper);
            if (payload != null) {
                msg.append(' ').append(jsonObject(payload));
            }
        }

        logger.info(msg.toString());
    }

    private void logAfter(RequestWrapper requestWrapper, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        msg.append("HTTP >");

        int status = response.getStatus();
        msg.append(' ').append(status);

        String location = response.getHeader("location");
        msg.append(' ').append(StringUtils.hasLength(location) ? jsonString(location) : "-");

        if (includePayload && response instanceof ByteArrayServletResponse) {
            String contentType = response.getContentType();
            if (contentType != null && contentType.contains("json")) {
                byte[] buf = ((ByteArrayServletResponse) response).toByteArray();
                String encoding = response.getCharacterEncoding();
                String payload;
                try {
                    payload = new String(buf, encoding == null ? DEFAULT_ENCODING : encoding);
                } catch (UnsupportedEncodingException e) {
                    payload = "[unknown]";
                }
                msg.append(' ').append(payload);
            }
        }

        if (status >= 200 && status < 400) {
            logger.info(msg.toString());
        } else {
            logger.error(msg.toString());
        }
    }

    private String getFullUri(HttpServletRequest request) {
        StringBuilder uri = new StringBuilder();
        uri.append(request.getRequestURI());
        String payload = request.getQueryString();
        if (payload != null) {
            uri.append('?').append(payload);
        }
        return uri.toString();
    }

    @SneakyThrows
    private String getMessagePayload(RequestWrapper requestWrapper) {
        HttpServletRequest request = requestWrapper.getRequest();

        switch (request.getMethod()) {
            case "POST":
            case "PUT":
            case "DELETE":
                break;
            default:
                return null;
        }

        InputStream is = request.getInputStream();
        byte[] buf = IOUtils.toByteArray(is);
        requestWrapper.setRequest(new ByteArrayServletRequest(request, buf));

        if (buf.length == 0) {
            return null;
        }
        try {
            String encoding = request.getCharacterEncoding();
            return new String(buf, encoding == null ? DEFAULT_ENCODING : encoding);
        } catch (UnsupportedEncodingException e) {
            return "[unknown]";
        }
    }

    @SneakyThrows
    private String jsonString(String str) {
        return objectMapper.writeValueAsString(str);
    }

    @SneakyThrows
    private String jsonObject(String str) {
        try {
            JsonNode node = objectMapper.readValue(str, JsonNode.class);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return objectMapper.writeValueAsString(str);
        }
    }
}
