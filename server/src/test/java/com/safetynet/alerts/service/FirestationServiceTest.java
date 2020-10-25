package com.safetynet.alerts.service;

import com.safetynet.alerts.PodamFactoryUtil;
import com.safetynet.alerts.api.model.Firestation;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.repository.mapper.AddressMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirestationServiceTest {
    @Mock
    private AddressRepository addressRepository;

    private final AddressMapper addressMapper = new AddressMapper();

    private FirestationService firestationService;

    private final PodamFactory factory = PodamFactoryUtil.createPodamFactory();

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        firestationService = new FirestationService(addressRepository, addressMapper);
    }

    @Test
    void getFirestation() {
        getFirestation(true, true);
        getFirestation(true, false);
        getFirestation(false, false);
    }

    private void getFirestation(boolean exists, boolean withFirestation) {
        AddressEntity address = exists ? factory.manufacturePojo(AddressEntity.class) : null;
        if (exists && !withFirestation) {
            address.setFirestation(null);
        }

        when(addressRepository.findByAddress("1509 Culver St"))
                .thenReturn(Optional.ofNullable(address));

        Firestation res = firestationService.getFirestation("1509 Culver St");
        assertEquals(addressMapper.toFirestation(address), res);
    }

    @Test
    void createFirestation() {
        createOrUpdateFirestation(false, true);
        createOrUpdateFirestation(false, false);
    }

    @Test
    void updateFirestation() {
        createOrUpdateFirestation(true, true);
        createOrUpdateFirestation(true, false);
        updateFirestationAddress();
    }

    private void createOrUpdateFirestation(boolean update, boolean alreadyExists) {
        Firestation firestation = Firestation.builder().address("1509 Culver St").station("A1").build();
        AddressEntity address = null;
        if (alreadyExists) {
            address = factory.manufacturePojo(AddressEntity.class);
            address.setAddress(firestation.getAddress());
        }

        when(addressRepository.findByAddress(firestation.getAddress()))
                .thenReturn(Optional.ofNullable(address));

        FirestationService.UpdateResult res;
        if (update) {
            res = firestationService.updateFirestation(firestation.getAddress(), firestation.clone());
        } else {
            res = firestationService.createFirestation(firestation.clone());
        }
        assertEquals(!alreadyExists, res.isCreated());
        assertEquals(firestation, res.getFirestation());
    }

    private void updateFirestationAddress() {
        Firestation firestation = Firestation.builder().address("1509 Culver St").station("A1").build();
        AddressEntity address = factory.manufacturePojo(AddressEntity.class);

        when(addressRepository.findByAddress(address.getAddress()))
                .thenReturn(Optional.ofNullable(address));

        assertThrows(FirestationService.ImmutableAddressException.class,
                () -> firestationService.updateFirestation(address.getAddress(), firestation.clone()));
    }

    @Test
    void deleteFirestation() {
        deleteFirestation(true, true);
        deleteFirestation(true, false);
        deleteFirestation(false, false);
    }

    private void deleteFirestation(boolean exists, boolean withFirestation) {
        AddressEntity address = exists ? factory.manufacturePojo(AddressEntity.class) : null;
        if (exists && !withFirestation) {
            address.setFirestation(null);
        }

        when(addressRepository.findByAddress("1509 Culver St"))
                .thenReturn(Optional.ofNullable(address));

        boolean res = firestationService.deleteFirestation("1509 Culver St");
        assertEquals(withFirestation, res);
        if (res) {
            assertNull(address.getFirestation());
            verify(addressRepository, times(1)).save(address);
        }
    }
}