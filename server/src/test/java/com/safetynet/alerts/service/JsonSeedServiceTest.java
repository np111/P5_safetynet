package com.safetynet.alerts.service;

import com.safetynet.alerts.properties.JsonSeedProperties;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.PersonRepository;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import static com.safetynet.alerts.service.JsonSeedServiceTestData.seedEntities;
import static com.safetynet.alerts.service.JsonSeedServiceTestData.seedModels;
import static com.safetynet.alerts.service.JsonSeedServiceTestData.seedModelsJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class JsonSeedServiceTest {
    @MockBean
    private AddressRepository addressRepository;

    @MockBean
    private PersonRepository personRepository;

    @Autowired
    private JsonSeedService jsonSeedService;

    @Bean
    public JsonSeedProperties getJsonSeedProperties() {
        JsonSeedProperties props = new JsonSeedProperties();
        props.setEnabled(true);
        return props;
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void isDatabaseEmpty() {
        for (long addressCount = 0; addressCount <= 1; ++addressCount) {
            for (long personCount = 0; personCount <= 1; ++personCount) {
                when(addressRepository.count()).thenReturn(addressCount);
                when(personRepository.count()).thenReturn(personCount);
                assertEquals(addressCount == 0 && personCount == 0, jsonSeedService.isDatabaseEmpty());
            }
        }
    }

    @Test
    void readSeedDataFromResource() {
        assertEquals(seedModels(), jsonSeedService.readSeedDataFromResource(
                new ByteArrayInputStream(seedModelsJson().getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void seedDataToEntities() {
        assertEquals(seedEntities(), JsonSeedService.seedDataToEntities(seedModels()));
    }
}