package com.safetynet.alerts.repository;

import com.safetynet.alerts.repository.entity.PersonEntity;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<PersonEntity, Long> {
    Iterable<PersonEntity> findAllByFirstNameAndLastName(String firstName, String lastName);

    Iterable<PersonEntity> findAllByAddressAddress(String address);

    Iterable<PersonEntity> findAllByAddressFirestation(String firestation);
}
