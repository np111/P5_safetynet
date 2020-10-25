package com.safetynet.alerts;

import com.safetynet.alerts.api.validation.constraint.IsName;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import lombok.experimental.UtilityClass;
import uk.co.jemos.podam.api.AbstractRandomDataProviderStrategy;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import uk.co.jemos.podam.common.AttributeStrategy;
import uk.co.jemos.podam.common.BeanValidationStrategy;

@UtilityClass
public class PodamFactoryUtil {
    public static PodamFactory createPodamFactory() {
        return new PodamFactoryImpl(new AbstractRandomDataProviderStrategy() {
            {
                setMemoization(false);
            }

            @Override
            public AttributeStrategy<?> getStrategyForAnnotation(Class<? extends Annotation> annotation) {
                if (annotation.getPackage() == IsName.class.getPackage()) {
                    return (type, annotations) -> {
                        return new BeanValidationStrategy(type).getValue(type, Arrays.asList(annotation.getAnnotations()));
                    };
                }
                return super.getStrategyForAnnotation(annotation);
            }
        });
    }
}
