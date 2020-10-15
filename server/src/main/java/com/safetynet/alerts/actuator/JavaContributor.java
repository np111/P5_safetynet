package com.safetynet.alerts.actuator;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class JavaContributor implements InfoContributor {
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> java = new LinkedHashMap<>();
        builder.withDetail("java", java);

        java.put("name", System.getProperty("java.runtime.name"));
        java.put("version", System.getProperty("java.version"));
        java.put("fullVersion", System.getProperty("java.runtime.version"));
    }
}
