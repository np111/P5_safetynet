package com.safetynet.alerts.http.controller;


import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.util.ApiErrorCode;
import com.safetynet.alerts.util.ApiException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Scope("singleton")
public class ExceptionController {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    /**
     * Handles ApiError.
     */
    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleApiException(ApiException e, HttpServletRequest req) {
        return toResponse(e.getError());
    }

    /**
     * Handles non-readable requests (eg. invalid json).
     * <p>
     * Returns a CLIENT/BAD_REQUEST error.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
            HttpServletRequest req) {
        if (e.getMessage() != null && e.getMessage().startsWith("Required request body is missing")) {
            return toResponse(errorValidationFailed("is required", "body", null, null));
        }
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ex = (InvalidFormatException) e.getCause();
            String parameter = ex.getPath().stream().map(Reference::getFieldName).collect(Collectors.joining("."));
            Map<String, Object> attributes = new LinkedHashMap<>();
            attributes.put("parserMessage", ex.getOriginalMessage());
            return toResponse(errorValidationFailed("is badly formatted", parameter, null, attributes));
        }
        return toResponse(errorBadRequest(e.getMessage()));
    }

    /**
     * Handles missing handlers ("404 errors").
     * <p>
     * Returns a CLIENT/BAD_REQUEST error.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleNoHandlerFoundException(NoHandlerFoundException e,
            HttpServletRequest req) {
        return toResponse(errorBadRequest(e.getMessage()));
    }

    /**
     * Handles missing @RequestParam.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleMissingRequestParamException(MissingServletRequestParameterException e,
            HttpServletRequest req) {
        String message = "is required";
        String parameter = e.getParameterName();
        return toResponse(errorValidationFailed(message, parameter, null, null));
    }

    /**
     * Handles bad types @RequestParam.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e,
            HttpServletRequest req) {
        String message = "must be a " + e.getParameter().getParameterType().getSimpleName();
        String parameter = e.getName();
        return toResponse(errorValidationFailed(message, parameter, null, null));
    }

    /**
     * Handles failed validation.
     * <p>
     * Returns a CLIENT/VALIDATION_FAILED error.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException e,
            HttpServletRequest req) {
        ConstraintViolation<?> fieldError = e.getConstraintViolations().stream().findFirst().get();
        String message = fieldError.getMessage();
        String parameter = StreamSupport.stream(fieldError.getPropertyPath().spliterator(), false)
                .skip(1L).map(Path.Node::toString).collect(Collectors.joining("."));
        String constraint = fieldError.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        Map<String, Object> attributes = fieldError.getConstraintDescriptor().getAttributes();
        return toResponse(errorValidationFailed(message, parameter, constraint, attributes));
    }

    /**
     * Handles failed validation.
     * <p>
     * Returns a CLIENT/VALIDATION_FAILED error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
            HttpServletRequest req) {
        // Only handles the first FieldError if there is one
        Optional<FieldError> fieldError = e.getBindingResult()
                .getAllErrors().stream().filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error).findFirst();
        if (fieldError.isPresent()) {
            String message = fieldError.get().getDefaultMessage();
            String parameter = fieldError.get().getField();
            String constraint = fieldError.get().getCode();
            // TODO: retrieves attributes
            return toResponse(errorValidationFailed(message, parameter, constraint, null));
        }

        // Or handles the first misc error
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        String message = objectError.getDefaultMessage();
        String constraint = objectError.getCode();
        // TODO: retrieves attributes
        return toResponse(errorValidationFailed(message, null, constraint, null));
    }

    /**
     * Handles all others exceptions.
     * <p>
     * Returns a UNKNOWN/SERVER_EXCEPTION error.
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleOthersException(Exception e, HttpServletRequest req) {
        logger.error("Unhandled request exception:", e);
        return toResponse(ApiError.builder()
                .type(ApiError.ErrorType.UNKNOWN)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ApiErrorCode.SERVER_EXCEPTION)
                .message("Internal server error (" + e.getClass().getSimpleName() + ")")
                .build());
    }

    private ApiError errorBadRequest(String message) {
        return ApiError.builder()
                .type(ApiError.ErrorType.CLIENT)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ApiErrorCode.BAD_REQUEST)
                .message(message)
                .build();
    }

    private ApiError errorValidationFailed(String message, String parameter, String constraint,
            Map<String, Object> attributes) {
        if (parameter != null) {
            message = parameter + " " + message;
        }
        ApiError.Builder res = ApiError.builder()
                .type(ApiError.ErrorType.CLIENT)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ApiErrorCode.VALIDATION_FAILED)
                .message("Validation failed: " + message);
        if (parameter != null) {
            res.metadata("parameter", parameter);
        }
        if (constraint != null) {
            res.metadata("constraint", constraint);
        }
        if (attributes != null) {
            attributes.forEach((name, value) -> {
                switch (name) {
                    case "groups":
                    case "message":
                    case "payload":
                        break;
                    default:
                        res.metadata(name, value);
                }
            });
        }
        return res.build();
    }

    private ResponseEntity<ApiError> toResponse(ApiError error) {
        return new ResponseEntity<>(error, HttpStatus.valueOf(error.getStatus()));
    }
}
