package com.safetynet.alerts.repository.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressEntityTest {
    @Test
    void isComplete() {
        assertFalse(createAddressEntity(null, null).isComplete());
        assertFalse(createAddressEntity(null, "zip").isComplete());
        assertFalse(createAddressEntity("city", null).isComplete());
        assertTrue(createAddressEntity("city", "zip").isComplete());
    }

    private AddressEntity createAddressEntity(String city, String zip) {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCity(city);
        addressEntity.setZip(zip);
        return addressEntity;
    }
}