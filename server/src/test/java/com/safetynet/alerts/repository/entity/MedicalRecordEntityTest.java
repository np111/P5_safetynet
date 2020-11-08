package com.safetynet.alerts.repository.entity;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MedicalRecordEntityTest {
    @Test
    void calculateAge() {
        LocalDate today = LocalDate.of(2020, 11, 8);
        LocalDate birthdate = LocalDate.of(2001, 2, 3);
        assertNull(createMedicalRecordEntity(null).calculateAge(today));
        assertEquals(19, createMedicalRecordEntity(birthdate).calculateAge(today));
        assertThrows(NullPointerException.class, () -> createMedicalRecordEntity(birthdate).calculateAge(null));
    }

    private MedicalRecordEntity createMedicalRecordEntity(LocalDate birthdate) {
        MedicalRecordEntity medicalRecordEntity = new MedicalRecordEntity();
        medicalRecordEntity.setBirthdate(birthdate);
        return medicalRecordEntity;
    }
}