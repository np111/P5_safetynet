package com.safetynet.alerts.repository.mapper;

import com.safetynet.alerts.api.model.Firestation;
import com.safetynet.alerts.repository.entity.AddressEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddressMapper {
    private static final AddressMapper INSTANCE = new AddressMapper();

    public static AddressMapper getInstance() {
        return INSTANCE;
    }

    public Firestation toFirestation(AddressEntity entity) {
        if (entity == null || entity.getFirestation() == null) {
            return null;
        }
        return Firestation.builder()
                .address(entity.getAddress())
                .station(entity.getFirestation())
                .build();
    }
}
