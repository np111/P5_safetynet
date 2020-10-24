package com.safetynet.alerts.util.springdoc;

import com.safetynet.alerts.api.model.ApiError.ErrorType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(ApiErrorResponses.class)
public @interface ApiErrorResponse {
    ErrorType type() default ErrorType.UNKNOWN;

    int status() default 0;

    String code() default "";

    String message() default "";

    String description() default "";

    // TODO: metadata?

    String method() default "";
}
