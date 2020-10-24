package com.safetynet.alerts.repository;

import com.safetynet.alerts.repository.entity.AddressEntity;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface AddressRepository extends CrudRepository<AddressEntity, Long> {
    Optional<AddressEntity> findByAddress(String address);

    Iterable<AddressEntity> findAllByFirestationIn(Iterable<String> firestations);
}
