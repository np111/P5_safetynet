package com.safetynet.alerts.util;

import java.net.URI;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UriUtil {
    @SneakyThrows
    public static URI createUri(String str) {
        return new URI(str);
    }
}
