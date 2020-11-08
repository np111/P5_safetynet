package com.safetynet.alerts.service;

import com.safetynet.alerts.api.model.Firestation;
import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import java.time.LocalDate;
import java.util.Arrays;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonSeedServiceTestData {
    public static String seedModelsJson() {
        return "{\n"
                + "  \"persons\": [\n"
                + "    {\"firstName\": \"John\", \"lastName\": \"Boyd\", \"address\": \"1509 Culver St\", \"city\": \"Culver\", \"zip\": \"97451\", \"phone\": \"841-874-6512\", \"email\": \"jaboyd@email.com\"},\n"
                + "    {\"firstName\": \"Jacob\", \"lastName\": \"Boyd\", \"address\": \"1509 Culver St\", \"city\": \"Culver\", \"zip\": \"97451\", \"phone\": \"841-874-6513\", \"email\": \"drk@email.com\"}\n"
                + "  ],\n"
                + "  \"firestations\": [\n"
                + "    {\"address\": \"1509 Culver St\", \"station\": \"3\"}\n"
                + "  ],\n"
                + "  \"medicalrecords\": [\n"
                + "    {\"firstName\": \"John\", \"lastName\": \"Boyd\", \"birthdate\": \"03/06/1984\", \"medications\": [\"aznol:350mg\", \"hydrapermazol:100mg\"], \"allergies\": [\"nillacilan\"]}\n"
                + "  ]\n"
                + "}";
    }

    public static JsonSeedService.Models seedModels() {
        return new JsonSeedService.Models(
                Arrays.asList(
                        Person.builder().firstName("John").lastName("Boyd").address("1509 Culver St").city("Culver").zip("97451").phone("841-874-6512").email("jaboyd@email.com").build(),
                        Person.builder().firstName("Jacob").lastName("Boyd").address("1509 Culver St").city("Culver").zip("97451").phone("841-874-6513").email("drk@email.com").build()),
                Arrays.asList(
                        Firestation.builder().address("1509 Culver St").station("3").build()),
                Arrays.asList(
                        MedicalRecord.builder().firstName("John").lastName("Boyd").birthdate(LocalDate.of(1984, 3, 6)).medication("aznol:350mg").medication("hydrapermazol:100mg").allergy("nillacilan").build()));
    }

    public static JsonSeedService.Entities seedEntities() {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setAddress("1509 Culver St");
        addressEntity.setCity("Culver");
        addressEntity.setZip("97451");
        addressEntity.setFirestation("3");

        PersonEntity person1 = new PersonEntity();
        person1.setFirstName("John");
        person1.setLastName("Boyd");
        person1.setAddress(addressEntity);
        person1.setPhone("841-874-6512");
        person1.setEmail("jaboyd@email.com");

        PersonEntity person2 = new PersonEntity();
        person2.setFirstName("Jacob");
        person2.setLastName("Boyd");
        person2.setAddress(addressEntity);
        person2.setPhone("841-874-6513");
        person2.setEmail("drk@email.com");

        MedicalRecordEntity medicalRecord1 = new MedicalRecordEntity();
        person1.setMedicalRecord(medicalRecord1);
        medicalRecord1.setPerson(person1);
        medicalRecord1.setBirthdate(LocalDate.of(1984, 3, 6));
        medicalRecord1.setMedications(Arrays.asList("aznol:350mg", "hydrapermazol:100mg"));
        medicalRecord1.setAllergies(Arrays.asList("nillacilan"));

        return new JsonSeedService.Entities(
                Arrays.asList(addressEntity),
                Arrays.asList(person1, person2),
                Arrays.asList(medicalRecord1));
    }
}
