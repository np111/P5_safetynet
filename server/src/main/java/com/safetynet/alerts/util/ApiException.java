package com.safetynet.alerts.util;

import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.http.controller.ExceptionController;
import com.safetynet.alerts.util.exception.FastRuntimeException;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;

/**
 * Wrapper to return an ApiError from a request.
 *
 * @see ExceptionController#handleApiException(ApiException, HttpServletRequest)
 */
@Getter
public class ApiException extends FastRuntimeException {
    private final ApiError error;

    public ApiException(ApiError error) {
        this.error = error;
    }
}
