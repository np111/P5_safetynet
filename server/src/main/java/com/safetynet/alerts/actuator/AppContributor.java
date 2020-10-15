package com.safetynet.alerts.actuator;

import com.safetynet.alerts.SafetynetAlertsApplication;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class AppContributor implements InfoContributor {
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> app = new LinkedHashMap<>();
        builder.withDetail("app", app);

        app.put("name", SafetynetAlertsApplication.NAME);
        app.put("version", SafetynetAlertsApplication.VERSION);
    }
}
