package com.safetynet.alerts.actuator;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaContributorTest {
    private final JavaContributor contributor = new JavaContributor();

    @SuppressWarnings("rawtypes")
    @Test
    void contribute() {
        Map java = InfoContributorTestUtil.doContribute(contributor).get("java", Map.class);
        assertTrue(java.containsKey("name"));
        assertTrue(java.containsKey("version"));
        assertTrue(java.containsKey("fullVersion"));
    }
}