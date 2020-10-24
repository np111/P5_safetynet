package com.safetynet.alerts.util;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UriUtilTest {
    @Test
    void createUri() throws Exception {
        assertEquals(new URI(""), UriUtil.createUri(""));
        assertEquals(new URI("/test"), UriUtil.createUri("/test"));
        assertEquals(new URI("https://www.google.fr/?q=test"), UriUtil.createUri("https://www.google.fr/?q=test"));
        assertThrows(URISyntaxException.class, () -> UriUtil.createUri(":"));
    }
}