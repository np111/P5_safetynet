package com.safetynet.alerts.repository;

import com.safetynet.alerts.repository.entity.AddressEntity;
import org.springframework.data.repository.CrudRepository;

public interface AddressRepository extends CrudRepository<AddressEntity, Long> {
}
