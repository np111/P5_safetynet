package com.safetynet.alerts.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiErrorCode {
    public static final String SERVER_EXCEPTION = "SERVER_EXCEPTION";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String ALREADY_EXISTS = "ALREADY_EXISTS";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String INTERFERING_NAMES = "INTERFERING_NAMES";
    public static final String INTERFERING_ADDRESS = "INTERFERING_ADDRESS";
}
