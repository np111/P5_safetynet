package com.safetynet.alerts.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safetynet.alerts.api.validation.constraint.IsAllergy;
import com.safetynet.alerts.api.validation.constraint.IsAllergyCollection;
import com.safetynet.alerts.api.validation.constraint.IsMedication;
import com.safetynet.alerts.api.validation.constraint.IsMedicationCollection;
import com.safetynet.alerts.api.validation.constraint.IsName;
import com.safetynet.alerts.api.validation.group.Update;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * Medical record of a physical person.
 */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MedicalRecord implements Cloneable {
    /**
     * @see Person#getId()
     */
    // @NotNull(groups = Update.class)
    private Long personId;

    // @NotNull(groups = Update.class)
    @IsName
    private String firstName;

    // @NotNull(groups = Update.class)
    @IsName
    private String lastName;

    @NotNull(groups = Update.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private LocalDate birthdate;

    @NotNull(groups = Update.class)
    @IsMedicationCollection
    private List<@NotNull @IsMedication String> medications;

    @NotNull(groups = Update.class)
    @IsAllergyCollection
    private List<@NotNull @IsAllergy String> allergies;

    @SneakyThrows
    public MedicalRecord clone() {
        MedicalRecord o = (MedicalRecord) super.clone();
        if (o.medications != null) {
            o.medications = new ArrayList<>(o.medications);
        }
        if (o.allergies != null) {
            o.allergies = new ArrayList<>(o.allergies);
        }
        return o;
    }
}
