package com.safetynet.alerts.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safetynet.alerts.api.validation.constraint.IsAddress;
import com.safetynet.alerts.api.validation.constraint.IsAllergy;
import com.safetynet.alerts.api.validation.constraint.IsCity;
import com.safetynet.alerts.api.validation.constraint.IsEmail;
import com.safetynet.alerts.api.validation.constraint.IsMedication;
import com.safetynet.alerts.api.validation.constraint.IsName;
import com.safetynet.alerts.api.validation.constraint.IsPhone;
import com.safetynet.alerts.api.validation.constraint.IsZip;
import com.safetynet.alerts.api.validation.group.Update;
import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * A physical person.
 */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Person implements Cloneable {
    /**
     * Unique identifier of this person.
     */
    private Long id;

    @NotNull(groups = Update.class)
    @IsName
    private String firstName;

    @NotNull(groups = Update.class)
    @IsName
    private String lastName;

    @NotNull(groups = Update.class)
    @IsAddress
    private String address;

    @NotNull(groups = Update.class)
    @IsCity
    private String city;

    @NotNull(groups = Update.class)
    @IsZip
    private String zip;

    @NotNull(groups = Update.class)
    @IsPhone
    private String phone;

    @NotNull(groups = Update.class)
    @IsEmail
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private LocalDate birthdate;

    private Integer age;

    private List<@NotNull @IsMedication String> medications;

    private List<@NotNull @IsAllergy String> allergies;

    @SneakyThrows
    public Person clone() {
        return (Person) super.clone();
    }

    @Data
    public static final class Key {
        private final String firstName;
        private final String lastName;
    }
}
