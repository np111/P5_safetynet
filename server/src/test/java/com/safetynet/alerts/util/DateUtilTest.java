package com.safetynet.alerts.util;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateUtilTest {
    private static final LocalDate NOW = LocalDate.of(2020, 10, 24);

    @Test
    void calculateAge() {
        assertThrows(NullPointerException.class, () -> assertNull(DateUtil.calculateAge(null, null)));

        assertNull(DateUtil.calculateAge(null, NOW));

        // 1 year limit (past)
        assertEquals(0, DateUtil.calculateAge(LocalDate.of(2019, 10, 25), NOW));
        assertEquals(1, DateUtil.calculateAge(LocalDate.of(2019, 10, 24), NOW));

        // 1 year limit (future)
        assertEquals(0, DateUtil.calculateAge(LocalDate.of(2021, 10, 23), NOW));
        assertEquals(-1, DateUtil.calculateAge(LocalDate.of(2021, 10, 24), NOW));

        // many dates
        assertEquals(24, DateUtil.calculateAge(LocalDate.of(1996, 5, 20), NOW));
        assertEquals(7, DateUtil.calculateAge(LocalDate.of(2012, 12, 12), NOW));
        assertEquals(2026, DateUtil.calculateAge(LocalDate.of(-7, 12, 25), NOW));
    }
}