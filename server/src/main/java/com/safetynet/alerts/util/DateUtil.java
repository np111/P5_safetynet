package com.safetynet.alerts.util;

import java.time.LocalDate;
import java.time.Period;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtil {
    public static Integer calculateAge(LocalDate birthdate, @NonNull LocalDate now) {
        if (birthdate != null) {
            return Period.between(birthdate, now).getYears();
        }
        return null;
    }
}
