package com.safetynet.alerts.repository;

import com.safetynet.alerts.repository.entity.PersonEntity;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<PersonEntity, Long> {
    long removeById(Long id);

    boolean existsByFirstNameAndLastName(String firstName, String lastName);

    Iterable<PersonEntity> findAllByFirstNameAndLastName(String firstName, String lastName);

    long removeByFirstNameAndLastName(String firstName, String lastName);

    Iterable<PersonEntity> findAllByAddressCity(String city);

    Iterable<PersonEntity> findAllByAddressAddress(String address);

    Iterable<PersonEntity> findAllByAddressFirestation(String firestation);
}
