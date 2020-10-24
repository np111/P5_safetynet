package com.safetynet.alerts.util.springdoc;

import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.api.model.ApiError.ErrorType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.SpringDocAnnotationsUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.method.HandlerMethod;

import static com.safetynet.alerts.util.ApiErrorCode.VALIDATION_FAILED;

@Component
public class SpringdocCustomizer implements OpenApiCustomiser, OperationCustomizer {
    @Override
    public void customise(OpenAPI openAPI) {
        SpringDocAnnotationsUtils.resolveSchemaFromType(ApiError.class, openAPI.getComponents(), null);
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        addValidationErrorResponses(operation, handlerMethod);
        addApiErrorResponses(operation, handlerMethod);
        setContentType(operation);
        return operation;
    }

    private void addValidationErrorResponses(Operation operation, HandlerMethod handlerMethod) {
        if (handlerMethod.getMethod().getDeclaringClass().isAnnotationPresent(Validated.class)) {
            // TODO: Add details, exclude methods with not parameter or body
            ApiError apiError = ApiError.builder()
                    .type(ErrorType.CLIENT)
                    .status(400)
                    .code(VALIDATION_FAILED)
                    .message("Validation failed")
                    .build();
            addApiErrorResponse(operation, apiError, null);
        }
    }

    private void addApiErrorResponses(Operation operation, HandlerMethod handlerMethod) {
        ApiErrorResponse[] responses = handlerMethod.getMethod().getAnnotationsByType(ApiErrorResponse.class);
        for (ApiErrorResponse response : responses) {
            addApiErrorResponse(operation, handlerMethod, response);
        }
    }

    private void addApiErrorResponse(Operation operation, HandlerMethod handlerMethod, ApiErrorResponse response) {
        ApiError apiError;
        if (!response.method().isEmpty()) {
            apiError = callApiErrorMethod(handlerMethod, response.method());
        } else {
            apiError = new ApiError();
        }
        if (response.type() != ErrorType.UNKNOWN) {
            apiError.setType(response.type());
        }
        if (response.status() != 0) {
            apiError.setStatus(response.status());
        }
        if (!response.code().isEmpty()) {
            apiError.setCode(response.code());
        }
        if (!response.message().isEmpty()) {
            apiError.setMessage(response.message());
        }
        addApiErrorResponse(operation, apiError, response.description());
    }

    private void addApiErrorResponse(Operation operation, ApiError apiError, String description) {
        String name = "" + apiError.getStatus();
        while (operation.getResponses().containsKey(name)) {
            name += "'";
        }

        if (description == null || description.isEmpty()) {
            description = StringUtils.capitalize(StringUtils.defaultString(apiError.getMessage()));
        }

        operation.getResponses().addApiResponse(name, new ApiResponse()
                .description(apiError.getType() + "/" + apiError.getCode() + " - " + description)
                .content(new Content()
                        .addMediaType("*/*", new MediaType()
                                .schema(new Schema<>().$ref("ApiError"))
                                .example(apiError))));
    }

    @SneakyThrows
    private ApiError callApiErrorMethod(HandlerMethod handlerMethod, String methodName) {
        Method method = handlerMethod.getMethod().getDeclaringClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (ApiError) method.invoke(null);
    }

    private void setContentType(Operation operation) {
        operation.getResponses().values().forEach(response -> {
            Content content = response.getContent();
            if (content != null) {
                MediaType mediaType = content.remove("*/*");
                if (mediaType != null) {
                    content.addMediaType("application/json", mediaType);
                }
            }
        });
    }
}
