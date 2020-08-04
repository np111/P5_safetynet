package com.safetynet.alerts.repository;

import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import org.springframework.data.repository.CrudRepository;

public interface MedicalRecordRepository extends CrudRepository<MedicalRecordEntity, Long> {
    long removeById(Long id);

    boolean existsByPersonId(long personId);

    Iterable<MedicalRecordEntity> findAllByPersonFirstNameAndPersonLastName(String firstName, String lastName);

    long removeByPersonFirstNameAndPersonLastName(String firstName, String lastName);
}
