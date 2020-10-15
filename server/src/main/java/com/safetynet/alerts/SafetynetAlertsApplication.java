package com.safetynet.alerts;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import static com.safetynet.alerts.SafetynetAlertsApplication.NAME;
import static com.safetynet.alerts.SafetynetAlertsApplication.VERSION;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.safetynet.alerts.properties")
@OpenAPIDefinition(
        info = @Info(
                title = NAME,
                version = VERSION
        )
)
public class SafetynetAlertsApplication {
    public static final String NAME = "SafetyNet Alerts API";
    public static final String VERSION = "1.0";

    public static void main(String[] args) {
        SpringApplication.run(SafetynetAlertsApplication.class, args);
    }
}
