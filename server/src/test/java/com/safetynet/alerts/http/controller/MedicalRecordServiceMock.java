package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.service.MedicalRecordService;
import java.time.LocalDate;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import static org.mockito.Mockito.when;

@UtilityClass
public class MedicalRecordServiceMock {
    public static void init(MedicalRecordService medicalRecordService) throws Exception {
        // GET
        // - Get an existing medical record
        when(medicalRecordService.getMedicalRecord(knownMedicalRecord().getPersonId()))
                .thenReturn(knownMedicalRecord());

        // CREATE
        // - Create a new medical record
        when(medicalRecordService.createMedicalRecord(unknownMedicalRecord()))
                .thenReturn(new MedicalRecordService.UpdateResult(true, unknownMedicalRecord()));

        // - Create an existing medical record
        when(medicalRecordService.createMedicalRecord(knownMedicalRecord()))
                .thenThrow(new MedicalRecordService.MedicalRecordExistsException());

        // - Create a new medical record with an unknown person
        when(medicalRecordService.createMedicalRecord(unknownPersonMedicalRecord()))
                .thenThrow(new MedicalRecordService.PersonNotFoundException());

        // - Create a new medical record with names matching many persons
        when(medicalRecordService.createMedicalRecord(manyPersonsMedicalRecord()))
                .thenThrow(new MedicalRecordService.InterferingNamesException());

        // UPDATE
        // - Update an existing medical record
        when(medicalRecordService.updateMedicalRecord(knownMedicalRecord().getPersonId(), knownMedicalRecord()))
                .thenReturn(new MedicalRecordService.UpdateResult(false, knownMedicalRecord()));

        // UPDATE BY NAMES
        // - Update an existing medical record
        when(medicalRecordService.updateMedicalRecordByNames(knownMedicalRecord().getFirstName(), knownMedicalRecord().getLastName(), knownMedicalRecord()))
                .thenReturn(new MedicalRecordService.UpdateResult(false, knownMedicalRecord()));

        // - Update any medical record with names matching many persons
        when(medicalRecordService.updateMedicalRecordByNames(manyPersonsMedicalRecord().getFirstName(), manyPersonsMedicalRecord().getLastName(), manyPersonsMedicalRecord()))
                .thenThrow(new MedicalRecordService.InterferingNamesException());

        // DELETE
        // - Delete an existing medical record
        when(medicalRecordService.deleteMedicalRecord(knownMedicalRecord().getPersonId()))
                .thenReturn(true);

        // DELETE BY NAMES
        // - Delete an existing medical record
        when(medicalRecordService.deleteMedicalRecordByNames(knownMedicalRecord().getFirstName(), knownMedicalRecord().getLastName()))
                .thenReturn(true);

        // - Delete any medical record with names matching many persons
        when(medicalRecordService.deleteMedicalRecordByNames(manyPersonsMedicalRecord().getFirstName(), manyPersonsMedicalRecord().getLastName()))
                .thenThrow(new MedicalRecordService.InterferingNamesException());
    }

    public static MedicalRecord knownMedicalRecord() {
        return MedicalRecord.builder()
                .personId(1L)
                .firstName("Known")
                .lastName("Person")
                .birthdate(LocalDate.of(1998, 7, 12))
                .allergy("peanut").allergy("shellfish")
                .medication("hydrapermazol:300mg").medication("dodoxadin:30mg")
                .build();
    }

    public static String knownMedicalRecordJson() {
        return "{\"personId\":1"
                + ",\"firstName\":\"Known\""
                + ",\"lastName\":\"Person\""
                + ",\"birthdate\":\"07/12/1998\""
                + ",\"medications\":[\"hydrapermazol:300mg\",\"dodoxadin:30mg\"]"
                + ",\"allergies\":[\"peanut\",\"shellfish\"]"
                + "}";
    }

    public static MedicalRecord unknownMedicalRecord() {
        return MedicalRecord.builder()
                .personId(2L)
                .firstName("AnotherKnown")
                .lastName("Person")
                .birthdate(LocalDate.of(2020, 10, 23))
                .allergy("peanut").allergy("shellfish")
                .medication("hydrapermazol:300mg").medication("dodoxadin:30mg")
                .build();
    }

    public static String unknownMedicalRecordJson() {
        return "{\"personId\":2"
                + ",\"firstName\":\"AnotherKnown\""
                + ",\"lastName\":\"Person\""
                + ",\"birthdate\":\"10/23/2020\""
                + ",\"medications\":[\"hydrapermazol:300mg\",\"dodoxadin:30mg\"]"
                + ",\"allergies\":[\"peanut\",\"shellfish\"]"
                + "}";
    }

    public static MedicalRecord unknownPersonMedicalRecord() {
        return MedicalRecord.builder()
                .personId(3L)
                .firstName("Unknown")
                .lastName("Person")
                .birthdate(LocalDate.of(2020, 10, 23))
                .allergy("peanut").allergy("shellfish")
                .medication("hydrapermazol:300mg").medication("dodoxadin:30mg")
                .build();
    }

    public static String unknownPersonMedicalRecordJson() {
        return "{\"personId\":3"
                + ",\"firstName\":\"Unknown\""
                + ",\"lastName\":\"Person\""
                + ",\"birthdate\":\"10/23/2020\""
                + ",\"medications\":[\"hydrapermazol:300mg\",\"dodoxadin:30mg\"]"
                + ",\"allergies\":[\"peanut\",\"shellfish\"]"
                + "}";
    }

    public static MedicalRecord manyPersonsMedicalRecord() {
        return MedicalRecord.builder()
                .firstName("Many")
                .lastName("Person")
                .birthdate(LocalDate.of(2020, 10, 23))
                .allergy("peanut").allergy("shellfish")
                .medication("hydrapermazol:300mg").medication("dodoxadin:30mg")
                .build();
    }

    public static String manyPersonsMedicalRecordJson() {
        return "{\"firstName\":\"Many\""
                + ",\"lastName\":\"Person\""
                + ",\"birthdate\":\"10/23/2020\""
                + ",\"medications\":[\"hydrapermazol:300mg\",\"dodoxadin:30mg\"]"
                + ",\"allergies\":[\"peanut\",\"shellfish\"]"
                + "}";
    }

    public static String invalidMedicalRecordId() {
        return "nan";
    }

    public static String invalidMedicalRecordFirstName() {
        return StringUtils.repeat('a', 256);
    }

    public static String invalidMedicalRecordLastName() {
        return StringUtils.repeat('b', 256);
    }

    public static String invalidMedicalRecordJson() {
        return "{\"personId\":\"nan\"}";
    }
}
