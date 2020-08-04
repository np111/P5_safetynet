package com.safetynet.alerts.controller;

import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.api.response.PersonsCoveredByFirestationResponse;
import com.safetynet.alerts.api.validation.constraint.IsStationNumber;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.util.spring.JsonRequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import java.time.ZonedDateTime;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/")
@Validated
public class AlertsController {
    private final AddressRepository addressRepository;
    private final PersonRepository personRepository;

    @Operation(
            summary = "Returns the list of persons covered by a firestation."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/firestation")
    @Transactional(readOnly = true)
    public PersonsCoveredByFirestationResponse getPersonsCoveredByFirestation(
            @RequestParam("stationNumber") @NotNull @IsStationNumber String stationNumber
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        PersonsCoveredByFirestationResponse.Builder res = PersonsCoveredByFirestationResponse.builder();
        int adultsCount = 0;
        int childrenCount = 0;

        for (PersonEntity personEntity : personRepository.findAllByAddressFirestation(stationNumber)) {
            Person person = personEntity.toCompletePerson(now);
            res.person(person);
            if (isAdult(person)) {
                ++adultsCount;
            } else {
                ++childrenCount;
            }
        }
        return res.adultsCount(adultsCount).childrenCount(childrenCount).build();
    }

    public static boolean isAdult(Person person) {
        Integer age = person.getAge();
        return age == null || age >= 18;
    }
}
