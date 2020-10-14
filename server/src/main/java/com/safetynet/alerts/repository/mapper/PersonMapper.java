package com.safetynet.alerts.repository.mapper;

import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PersonMapper {
    private static final PersonMapper INSTANCE = new PersonMapper();

    public static PersonMapper getInstance() {
        return INSTANCE;
    }

    public Person toPerson(PersonEntity entity) {
        if (entity == null) {
            return null;
        }
        AddressEntity addressEntity = entity.getAddress(); // note: cannot be null
        return Person.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .address(addressEntity.getAddress())
                .city(addressEntity.getCity())
                .zip(addressEntity.getZip())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .build();
    }

    public Person toCompletePerson(PersonEntity entity, ZonedDateTime now) {
        return toCompletePerson(entity, now, false);
    }

    public Person toCompletePerson(PersonEntity entity, ZonedDateTime now, boolean withMedicalRecords) {
        if (entity == null) {
            return null;
        }
        AddressEntity addressEntity = entity.getAddress(); // note: cannot be null
        MedicalRecordEntity medicalRecord = entity.getMedicalRecord();
        Person.Builder res = Person.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .address(addressEntity.getAddress())
                .city(addressEntity.getCity())
                .zip(addressEntity.getZip())
                .phone(entity.getPhone())
                .email(entity.getEmail());
        if (medicalRecord != null) {
            res.birthdate(medicalRecord.getBirthdate());
            res.age(now == null ? null : medicalRecord.calculateAge(now.toLocalDate()));
            if (withMedicalRecords) {
                res.medications(new ArrayList<>(medicalRecord.getMedications()));
                res.allergies(new ArrayList<>(medicalRecord.getAllergies()));
            }
        }
        return res.build();
    }
}
