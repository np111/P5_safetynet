package com.safetynet.alerts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.api.model.Firestation;
import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.properties.JsonSeedProperties;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service that initializes repositories from the data.json file.
 */
@Service
@Scope("singleton")
public class JsonSeedService {
    private static final Logger logger = LoggerFactory.getLogger(JsonSeedService.class);

    private final AddressRepository addressRepository;
    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    @Autowired
    public JsonSeedService(AddressRepository addressRepository, PersonRepository personRepository,
            ObjectMapper objectMapper, JsonSeedProperties props) {
        this.addressRepository = addressRepository;
        this.personRepository = personRepository;
        this.objectMapper = objectMapper;
        this.enabled = props.isEnabled();
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void onContextRefreshed() {
        if (enabled && isDatabaseEmpty()) {
            // Only seed the database at the first usage
            logger.debug("Seeding database with data.json");
            Models models = readSeedDataFromResource("/data.json");
            Entities entities = seedDataToEntities(models);
            personRepository.saveAll(entities.getPersons());
        }
    }

    boolean isDatabaseEmpty() {
        return addressRepository.count() == 0
                && personRepository.count() == 0;
    }

    Models readSeedDataFromResource(String resourcePath) {
        try (InputStream is = JsonSeedService.class.getResourceAsStream(resourcePath)) {
            return readSeedDataFromResource(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Models readSeedDataFromResource(InputStream is) {
        try {
            return objectMapper.readValue(is, Models.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Entities seedDataToEntities(Models models) {
        Map<String, AddressEntity> addresses = new LinkedHashMap<>();
        Map<Person.Key, PersonEntity> persons = new LinkedHashMap<>();

        for (Person person : models.getPersons()) {
            // Extract addresses
            AddressEntity addressEntity = addresses.computeIfAbsent(person.getAddress(), k -> {
                AddressEntity e = new AddressEntity();
                e.setAddress(person.getAddress());
                e.setCity(person.getCity());
                e.setZip(person.getZip());
                return e;
            });

            // Create persons entities and associate them their address
            PersonEntity personEntity = new PersonEntity();
            personEntity.setFirstName(person.getFirstName());
            personEntity.setLastName(person.getLastName());
            personEntity.setAddress(addressEntity);
            personEntity.setPhone(person.getPhone());
            personEntity.setEmail(person.getEmail());
            persons.put(new Person.Key(person.getFirstName(), person.getLastName()), personEntity);
        }

        for (Firestation firestation : models.getFirestations()) {
            // Associate firestation number to addresses
            AddressEntity addressEntity = addresses.get(firestation.getAddress());
            if (addressEntity == null) {
                throw new IllegalArgumentException("Unknown address: " + firestation.getAddress());
            }
            addressEntity.setFirestation(firestation.getStation());
        }

        for (MedicalRecord medicalRecord : models.getMedicalrecords()) {
            // Create medical records and associate them with the persons
            PersonEntity personEntity = persons.get(
                    new Person.Key(medicalRecord.getFirstName(), medicalRecord.getLastName()));
            MedicalRecordEntity medicalRecordEntity = new MedicalRecordEntity();
            medicalRecordEntity.setPerson(personEntity);
            medicalRecordEntity.setBirthdate(medicalRecord.getBirthdate());
            medicalRecordEntity.setMedications(medicalRecord.getMedications());
            medicalRecordEntity.setAllergies(medicalRecord.getAllergies());
            personEntity.setMedicalRecord(medicalRecordEntity);
        }

        return new Entities(
                new ArrayList<>(addresses.values()),
                new ArrayList<>(persons.values()),
                persons.values().stream()
                        .map(PersonEntity::getMedicalRecord)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Data
    public static class Models {
        private List<Person> persons;
        private List<Firestation> firestations;
        private List<MedicalRecord> medicalrecords;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Data
    public static class Entities {
        private List<AddressEntity> addresses;
        private List<PersonEntity> persons;
        private List<MedicalRecordEntity> medicalRecords;
    }
}
