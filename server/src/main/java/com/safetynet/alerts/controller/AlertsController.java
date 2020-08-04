package com.safetynet.alerts.controller;

import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.api.response.ChildAlertResponse;
import com.safetynet.alerts.api.response.FireResponse;
import com.safetynet.alerts.api.response.PersonsCoveredByFirestationResponse;
import com.safetynet.alerts.api.response.PhoneAlertResponse;
import com.safetynet.alerts.api.validation.constraint.IsAddress;
import com.safetynet.alerts.api.validation.constraint.IsStationNumber;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.util.spring.JsonRequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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

    @Operation(
            summary = "Returns the list of persons living at an address."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/childAlert")
    @Transactional(readOnly = true)
    public ChildAlertResponse getChildAlert(
            @RequestParam("address") @NotNull @IsAddress String address
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        ChildAlertResponse.Builder res = ChildAlertResponse.builder();

        for (PersonEntity personEntity : personRepository.findAllByAddressAddress(address)) {
            Person person = personEntity.toCompletePerson(now);
            if (isAdult(person)) {
                res.adult(person);
            } else {
                res.children(person);
            }
        }
        return res.build();
    }

    @Operation(
            summary = "Returns the list of phones of persons covered by a firestation."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/phoneAlert")
    @Transactional(readOnly = true)
    public PhoneAlertResponse getPhoneAlert(
            @RequestParam("firestation") @NotNull @IsStationNumber String stationNumber
    ) {
        List<String> phones = StreamSupport
                .stream(personRepository.findAllByAddressFirestation(stationNumber).spliterator(), false)
                .map(PersonEntity::getPhone)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return PhoneAlertResponse.builder().phones(phones).build();
    }

    @Operation(
            summary = "Returns the list of persons living at an address and the firestation covering them."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/fire")
    @Transactional(readOnly = true)
    public FireResponse getFire(
            @RequestParam("address") @NotNull @IsAddress String address
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        FireResponse.Builder res = FireResponse.builder();

        addressRepository.findByAddress(address)
                .ifPresent(addressEntity -> res.stationNumber(addressEntity.getFirestation()));
        for (PersonEntity personEntity : personRepository.findAllByAddressAddress(address)) {
            res.person(personEntity.toCompletePerson(now, true));
        }
        return res.build();
    }

    public static boolean isAdult(Person person) {
        Integer age = person.getAge();
        return age == null || age >= 18;
    }
}
