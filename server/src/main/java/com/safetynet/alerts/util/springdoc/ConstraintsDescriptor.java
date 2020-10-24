package com.safetynet.alerts.util.springdoc;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.groups.Default;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.ValueConstants.DEFAULT_NONE;

@UtilityClass
public class ConstraintsDescriptor {
    public static List<Description> describeParameters(Method method) {
        List<Description> descriptions = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            String name;
            boolean required;

            Annotation annotation;
            if ((annotation = AnnotationUtils.getAnnotation(param, RequestBody.class)) != null) {
                RequestBody requestBody = (RequestBody) annotation;
                name = "body";
                required = requestBody.required();
            } else if ((annotation = AnnotationUtils.getAnnotation(param, PathVariable.class)) != null) {
                PathVariable pathVariable = (PathVariable) annotation;
                name = pathVariable.name();
                required = pathVariable.required();
            } else if ((annotation = AnnotationUtils.getAnnotation(param, RequestParam.class)) != null) {
                RequestParam requestParam = (RequestParam) annotation;
                name = requestParam.name();
                required = requestParam.required() && DEFAULT_NONE.equals(requestParam.defaultValue());
            } else {
                continue;
            }

            Description description = new Description(name);
            if (required) {
                description.getConstraints().add("NotNull");
            }
            describe(description, param.getType(), param, new Class[0]);
            if (!description.isEmpty()) {
                descriptions.add(description);
            }
        }
        return descriptions;
    }

    @SneakyThrows
    private static void describe(Description dst, Class<?> type, AnnotatedElement annotations, Class<?>[] validationGroups) {
        for (Annotation annotation : annotations.getAnnotations()) {
            if (AnnotationUtils.getAnnotation(annotation.annotationType(), Constraint.class) != null) {
                Method groupsMethod = MethodUtils.getMatchingMethod(annotation.annotationType(), "groups");
                Class<?>[] constraintGroups = (Class<?>[]) groupsMethod.invoke(annotation);
                if (isActive(constraintGroups, validationGroups)) {
                    dst.getConstraints().add(describe(annotation.annotationType()));
                }
            }
        }

        Class<?>[] fieldsValidationGroups = null;
        Validated validated = AnnotationUtils.getAnnotation(annotations, Validated.class);
        if (validated != null) {
            fieldsValidationGroups = validated.value();
        } else if (AnnotationUtils.getAnnotation(annotations, Valid.class) != null) {
            fieldsValidationGroups = new Class[0];
        }
        if (fieldsValidationGroups != null) {
            for (Field field : FieldUtils.getAllFields(type)) {
                Description description = new Description(field.getName());
                describe(description, field.getType(), field, fieldsValidationGroups);
                if (!description.isEmpty()) {
                    dst.getFields().add(description);
                }
            }
        }
    }

    private static boolean isActive(Class<?>[] constraintGroups, Class<?>[] validationGroups) {
        if (constraintGroups.length == 0) {
            constraintGroups = new Class[]{Default.class};
        }
        if (validationGroups.length == 0) {
            validationGroups = new Class[]{Default.class};
        }
        for (Class<?> validationGroup : validationGroups) {
            for (Class<?> constraintGroup : constraintGroups) {
                if (constraintGroup.isAssignableFrom(validationGroup)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String describe(Class<? extends Annotation> constraint) {
        return constraint.getSimpleName();
    }

    @RequiredArgsConstructor
    @Data
    public static class Description {
        private final String name;
        private final Set<String> constraints = new LinkedHashSet<>();
        private final List<Description> fields = new ArrayList<>();

        public boolean isEmpty() {
            return constraints.isEmpty() && fields.isEmpty();
        }
    }
}