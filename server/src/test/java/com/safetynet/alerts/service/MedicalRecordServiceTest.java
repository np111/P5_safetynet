package com.safetynet.alerts.service;

import com.safetynet.alerts.PodamFactoryUtil;
import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.repository.MedicalRecordRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.repository.mapper.MedicalRecordMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MedicalRecordServiceTest {
    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PersonRepository personRepository;

    private final MedicalRecordMapper medicalRecordMapper = new MedicalRecordMapper();

    private MedicalRecordService medicalRecordService;

    private final PodamFactory factory = PodamFactoryUtil.createPodamFactory();

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        medicalRecordService = new MedicalRecordService(medicalRecordRepository, personRepository, medicalRecordMapper);
    }

    @Test
    void getMedicalRecord() {
        MedicalRecordEntity medicalRecord = factory.manufacturePojo(MedicalRecordEntity.class);

        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(medicalRecord));

        assertEquals(medicalRecordMapper.toMedicalRecord(medicalRecord), medicalRecordService.getMedicalRecord(1L));
        assertNull(medicalRecordService.getMedicalRecord(2L));
    }

    @Test
    void createMedicalRecord() {
        createOrUpdateMedicalRecord(true, false);
    }

    @Test
    void createMedicalRecordByNames() {
        createOrUpdateMedicalRecord(true, true);
    }

    @Test
    void updateMedicalRecord() {
        createOrUpdateMedicalRecord(false, false);
    }

    @Test
    void updateMedicalRecordByNames() {
        createOrUpdateMedicalRecord(false, true);
    }

    private void createOrUpdateMedicalRecord(boolean create, boolean byNames) {
        createOrUpdateMedicalRecord(create, byNames, 0, false);
        createOrUpdateMedicalRecord(create, byNames, 1, false);
        createOrUpdateMedicalRecord(create, byNames, 1, true);
        if (byNames) {
            createOrUpdateMedicalRecord(create, byNames, 2, false);
            createOrUpdateMedicalRecord(create, byNames, 2, true);
        }
    }

    private void createOrUpdateMedicalRecord(boolean create, boolean byNames, int matchingPersonsCount, boolean alreadyExists) {
        MedicalRecord medicalRecordResult = MedicalRecord.builder()
                .personId(1L)
                .firstName("Jean")
                .lastName("Sebastien")
                .medication("med1")
                .medication("med2")
                .allergy("allergy1")
                .allergy("allergy2")
                .birthdate(LocalDate.now())
                .build();
        MedicalRecord medicalRecordRequest = medicalRecordResult.clone();
        if (byNames) {
            medicalRecordRequest.setPersonId(null);
        } else {
            medicalRecordRequest.setFirstName(null);
            medicalRecordRequest.setLastName(null);
        }

        // Mock
        // - Persons
        List<PersonEntity> personEntities = new ArrayList<>();
        if (byNames) {
            for (int i = 0; i < matchingPersonsCount; ++i) {
                PersonEntity personEntity = factory.manufacturePojo(PersonEntity.class);
                personEntity.setId(i + 1L);
                personEntity.setFirstName(medicalRecordResult.getFirstName());
                personEntity.setLastName(medicalRecordResult.getLastName());
                personEntities.add(personEntity);
            }
        } else {
            assert matchingPersonsCount <= 1; // ID is unique, so only 0 or 1 person is possible
            if (matchingPersonsCount > 0) {
                PersonEntity personEntity = factory.manufacturePojo(PersonEntity.class);
                personEntity.setId(medicalRecordResult.getPersonId());
                personEntity.setFirstName(medicalRecordResult.getFirstName());
                personEntity.setLastName(medicalRecordResult.getLastName());
                personEntities.add(personEntity);
            }
        }
        when(personRepository.findAllByFirstNameAndLastName(medicalRecordResult.getFirstName(), medicalRecordResult.getLastName()))
                .thenReturn(personEntities);
        for (PersonEntity personEntity : personEntities) {
            when(personRepository.findById(personEntity.getId())).thenReturn(Optional.of(personEntity));
        }
        // - Medical records
        assert !alreadyExists || !personEntities.isEmpty();
        List<MedicalRecordEntity> medicalRecordEntities = new ArrayList<>();
        if (alreadyExists) {
            for (PersonEntity personEntity : personEntities) {
                MedicalRecordEntity medicalRecordEntity = factory.manufacturePojo(MedicalRecordEntity.class);
                medicalRecordEntity.setId(personEntity.getId());
                medicalRecordEntity.setPerson(personEntity);
                medicalRecordEntities.add(medicalRecordEntity);
            }
        }
        when(medicalRecordRepository.findAllByPersonFirstNameAndPersonLastName(medicalRecordResult.getFirstName(), medicalRecordResult.getLastName()))
                .thenReturn(medicalRecordEntities);
        for (MedicalRecordEntity medicalRecordEntity : medicalRecordEntities) {
            when(medicalRecordRepository.existsByPersonId(medicalRecordEntity.getId())).thenReturn(true);
            when(medicalRecordRepository.findById(medicalRecordEntity.getId())).thenReturn(Optional.of(medicalRecordEntity));
        }

        // Execute request
        MedicalRecordService.UpdateResult res = null;
        Exception ex = null;
        try {
            if (create) {
                res = medicalRecordService.createMedicalRecord(medicalRecordRequest);
            } else if (medicalRecordRequest.getPersonId() != null) {
                res = medicalRecordService.updateMedicalRecord(medicalRecordRequest.getPersonId(), medicalRecordRequest);
            } else {
                res = medicalRecordService.updateMedicalRecordByNames(medicalRecordRequest.getFirstName(), medicalRecordRequest.getLastName(), medicalRecordRequest);
            }
        } catch (Exception ex_) {
            ex = ex_;
        }

        if (!create && !alreadyExists) {
            assertNull(ex);
            assertNull(res);
            return;
        }

        if (matchingPersonsCount <= 0) {
            assertTrue(ex instanceof MedicalRecordService.PersonNotFoundException, "throws PersonNotFoundException");
        } else if (matchingPersonsCount <= 1) {
            if (create && alreadyExists) {
                assertTrue(ex instanceof MedicalRecordService.MedicalRecordExistsException, "throws MedicalRecordExistsException");
            } else {
                assertNull(ex);
                assertEquals(create, res.isCreated());
                assertEquals(medicalRecordResult, res.getMedicalRecord());
                verify(medicalRecordRepository, times(1)).save(any());
            }
        } else {
            assertTrue(ex instanceof MedicalRecordService.InterferingNamesException, "throws InterferingNamesException");
        }
    }

    @Test
    void deleteMedicalRecordNotExisting() {
        assertFalse(medicalRecordService.deleteMedicalRecord(1L));
    }

    @Test
    void deleteMedicalRecord() {
        when(medicalRecordRepository.removeById(1L)).thenReturn(1L);

        assertTrue(medicalRecordService.deleteMedicalRecord(1L));
    }

    @Test
    void deleteMedicalRecordByNamesNotExisting() {
        assertFalse(medicalRecordService.deleteMedicalRecordByNames("A", "B"));
    }

    @Test
    void deleteMedicalRecordByNames() {
        when(medicalRecordRepository.removeByPersonFirstNameAndPersonLastName("A", "B")).thenReturn(1L);

        assertTrue(medicalRecordService.deleteMedicalRecordByNames("A", "B"));
    }

    @Test
    void deleteMedicalRecordByNamesInterfering() {
        when(medicalRecordRepository.removeByPersonFirstNameAndPersonLastName("A", "B")).thenReturn(2L);

        assertThrows(MedicalRecordService.InterferingNamesException.class,
                () -> medicalRecordService.deleteMedicalRecordByNames("A", "B"));
    }
}