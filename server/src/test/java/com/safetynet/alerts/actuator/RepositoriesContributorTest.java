package com.safetynet.alerts.actuator;

import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.MedicalRecordRepository;
import com.safetynet.alerts.repository.PersonRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class RepositoriesContributorTest {
    @Mock
    private PersonRepository personRepository;
    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    @Mock
    private AddressRepository addressRepository;

    private RepositoriesContributor contributor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(personRepository.count()).thenReturn(1L);
        when(medicalRecordRepository.count()).thenReturn(2L);
        when(addressRepository.count()).thenReturn(3L);
        contributor = new RepositoriesContributor(personRepository, medicalRecordRepository, addressRepository);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void contribute() {
        Map repositories = InfoContributorTestUtil.doContribute(contributor).get("repositories", Map.class);
        assertEquals(1L, repositories.get("personsCount"));
        assertEquals(2L, repositories.get("medicalRecordsCount"));
        assertEquals(3L, repositories.get("addressesCount"));
    }
}