package com.safetynet.alerts.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "safetynet.json-seed")
@Data
@Validated
public class JsonSeedProperties {
    private boolean enabled = true;
}
