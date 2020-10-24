package com.safetynet.alerts.repository.mapper;

import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import java.util.ArrayList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public final class MedicalRecordMapper {
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
