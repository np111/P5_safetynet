package com.safetynet.alerts.repository.mappers;

import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MedicalRecordMapper {
    private static final MedicalRecordMapper INSTANCE = new MedicalRecordMapper();

    public static MedicalRecordMapper getInstance() {
        return INSTANCE;
    }

    public MedicalRecord toMedicalRecord(MedicalRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        PersonEntity personEntity = entity.getPerson(); // note: cannot be null
        return MedicalRecord.builder()
                .personId(personEntity.getId())
                .firstName(personEntity.getFirstName())
                .lastName(personEntity.getLastName())
                .birthdate(entity.getBirthdate())
                .medications(new ArrayList<>(entity.getMedications()))
                .allergies(new ArrayList<>(entity.getAllergies()))
                .build();
    }
}
