package com.safetynet.alerts.actuator;

import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.MedicalRecordRepository;
import com.safetynet.alerts.repository.PersonRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Scope("singleton")
public class RepositoriesContributor implements InfoContributor {
    private final PersonRepository personRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AddressRepository addressRepository;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> repositories = new LinkedHashMap<>();
        builder.withDetail("repositories", repositories);

        repositories.put("personsCount", personRepository.count());
        repositories.put("medicalRecordsCount", medicalRecordRepository.count());
        repositories.put("addressesCount", addressRepository.count());
    }
}
