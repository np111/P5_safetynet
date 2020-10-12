package com.safetynet.alerts;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.safetynet.alerts.properties")
@OpenAPIDefinition(
        info = @Info(
                title = "SafetyNet Alerts API",
                version = "1.0"
        )
)
public class SafetynetAlertsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SafetynetAlertsApplication.class, args);
    }
}
