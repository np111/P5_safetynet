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

    /**
     * Returns a {@linkplain Firestation firestation} by it's address.
     *
     * @param address Address of the firestation to return
     * @return the firestation; or {@code null} if none has the given address
     */
    @Transactional
    public Firestation getFirestation(String address) {
        AddressEntity addressEntity = addressRepository.findByAddress(address).orElse(null);
        return addressEntity == null || addressEntity.getFirestation() == null ? null : addressEntity.toFirestation();
    }

    /**
     * Create a new {@linkplain Firestation firestation}.
     *
     * @param body data
     * @return the created firestation
     */
    @Transactional
    public UpdateResult createFirestation(Firestation body) {
        AddressEntity addressEntity = addressRepository.findByAddress(body.getAddress()).orElse(null);
        try {
            return update(addressEntity, body);
        } catch (ImmutableAddressException e) {
            throw new RuntimeException("unreachable", e);
        }
    }

    /**
     * Create or update a {@linkplain Firestation firestation}.
     *
     * @param address Address of the firestation to update
     * @param body    data
     * @return the created or updated firestation
     * @throws ImmutableAddressException if you try to update address
     */
    @Transactional
    public UpdateResult updateFirestation(String address, Firestation body) throws ImmutableAddressException {
        AddressEntity addressEntity = addressRepository.findByAddress(address).orElse(null);
        return update(addressEntity, body);
    }

    /**
     * Delete a {@linkplain Firestation firestation} by it's address.
     *
     * @param address Address of the firestation to delete
     * @return {@code true} if the firestation existed and was deleted; or {@code false} if not
     */
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

        // returns result
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
