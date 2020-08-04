package com.safetynet.alerts.util;

import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.controller.ExceptionController;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;

/**
 * Wrapper to return an ApiError from a request.
 *
 * @see ExceptionController#handleApiException(ApiException, HttpServletRequest)
 */
@Getter
public class ApiException extends RuntimeException {
    private final ApiError error;

    public ApiException(ApiError error) {
        this.error = error;
    }

    @Override
    public Throwable initCause(Throwable throwable) {
        return this;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
