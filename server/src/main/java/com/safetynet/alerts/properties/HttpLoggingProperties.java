package com.safetynet.alerts.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "safetynet.http-logging")
@Data
@Validated
public class HttpLoggingProperties {
    private boolean enabled = false;
    private boolean includePayload = true;
}
