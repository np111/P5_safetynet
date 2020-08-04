package com.safetynet.alerts.repository;

import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import org.springframework.data.repository.CrudRepository;

public interface MedicalRecordRepository extends CrudRepository<MedicalRecordEntity, Long> {
}
