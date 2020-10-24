package com.safetynet.alerts.repository.mapper;

import com.safetynet.alerts.api.model.Firestation;
import com.safetynet.alerts.repository.entity.AddressEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public final class AddressMapper {
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
