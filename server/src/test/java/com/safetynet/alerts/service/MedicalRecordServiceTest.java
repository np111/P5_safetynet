package com.safetynet.alerts.service;

import com.safetynet.alerts.PodamFactoryUtil;
import com.safetynet.alerts.repository.MedicalRecordRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.mapper.MedicalRecordMapper;
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
        // TODO
    }

    @Test
    void updateMedicalRecord() {
        // TODO
    }

    @Test
    void updateMedicalRecordByNames() {
        // TODO
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