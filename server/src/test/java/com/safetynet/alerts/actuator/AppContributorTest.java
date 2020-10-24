package com.safetynet.alerts.actuator;

import com.safetynet.alerts.SafetynetAlertsApplication;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppContributorTest {
    private final AppContributor contributor = new AppContributor();

    @Test
    void contribute() {
        Map<String, Object> excepted = new HashMap<>();
        excepted.put("name", SafetynetAlertsApplication.NAME);
        excepted.put("version", SafetynetAlertsApplication.VERSION);
        assertEquals(excepted, InfoContributorTestUtil.doContribute(contributor).get("app"));
    }
}