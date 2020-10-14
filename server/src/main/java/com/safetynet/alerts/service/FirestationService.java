package com.safetynet.alerts.service;

import com.safetynet.alerts.api.model.Firestation;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.util.exception.FastException;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class FirestationService {
    private final AddressRepository addressRepository;

    @Transactional
    public Firestation getFirestation(String address) {
        AddressEntity addressEntity = addressRepository.findByAddress(address).orElse(null);
        if (addressEntity == null || addressEntity.getFirestation() == null) {
            return null;
        }
        return addressEntity.toFirestation();
    }

    @Transactional
    public UpdateResult createFirestation(Firestation body) throws ImmutableAddressException {
        AddressEntity addressEntity = addressRepository.findByAddress(body.getAddress()).orElse(null);
        return update(addressEntity, body);
    }

    @Transactional
    public UpdateResult updateFirestation(String address, Firestation body) throws ImmutableAddressException {
        AddressEntity addressEntity = addressRepository.findByAddress(address).orElse(null);
        return update(addressEntity, body);
    }

    @Transactional
    public boolean deleteFirestation(String address) {
        AddressEntity addressEntity = addressRepository.findByAddress(address).orElse(null);
        if (addressEntity == null || addressEntity.getFirestation() == null) {
            return false;
        }
        addressEntity.setFirestation(null);
        addressRepository.save(addressEntity);
        return true;
    }

    /**
     * Create or update the firestation entity from it's model.
     *
     * @param entity the existing entity; or {@code null} to create one
     * @param body   the firestation model to apply
     * @return the result
     */
    private UpdateResult update(AddressEntity entity, Firestation body) throws ImmutableAddressException {
        boolean create = (entity == null);

        // create or update the address record
        if (create) {
            entity = new AddressEntity();
            entity.setAddress(body.getAddress());
        }
        if (!Objects.equals(body.getAddress(), entity.getAddress())) {
            throw new ImmutableAddressException();
        }
        entity.setFirestation(body.getStation());
        addressRepository.save(entity);

        return new UpdateResult(create, entity);
    }

    @RequiredArgsConstructor
    public static class UpdateResult {
        private final @Getter boolean created;
        private final @NonNull AddressEntity addressEntity;

        public @NonNull Firestation getFirestation() {
            return addressEntity.toFirestation();
        }
    }

    public static class ImmutableAddressException extends FastException {
    }
}
