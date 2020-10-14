package com.safetynet.alerts.api.model;

import com.safetynet.alerts.api.validation.constraint.IsAddress;
import com.safetynet.alerts.api.validation.constraint.IsStationNumber;
import com.safetynet.alerts.api.validation.group.Update;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * Entry associating a fire station with one of the addresses it covers.
 */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class Firestation implements Cloneable {
    @NotNull(groups = Update.class)
    @IsAddress
    private String address;

    @NotNull(groups = Update.class)
    @IsStationNumber
    private String station;

    @SneakyThrows
    public Firestation clone() {
        return (Firestation) super.clone();
    }
}
