package com.safetynet.alerts.service;

import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.api.response.ChildAlertResponse;
import com.safetynet.alerts.api.response.CommunityEmailResponse;
import com.safetynet.alerts.api.response.FireResponse;
import com.safetynet.alerts.api.response.FloodStationsResponse;
import com.safetynet.alerts.api.response.PersonInfoResponse;
import com.safetynet.alerts.api.response.PersonsCoveredByFirestationResponse;
import com.safetynet.alerts.api.response.PhoneAlertResponse;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.repository.mapper.PersonMapper;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class AlertsService {
    private final AddressRepository addressRepository;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonsCoveredByFirestationResponse getPersonsCoveredByFirestation(String stationNumber) {
        return getPersonsCoveredByFirestation(stationNumber, ZonedDateTime.now());
    }

    @Transactional(readOnly = true)
    public PersonsCoveredByFirestationResponse getPersonsCoveredByFirestation(String stationNumber, ZonedDateTime now) {
        PersonsCoveredByFirestationResponse.Builder res = PersonsCoveredByFirestationResponse.builder();
        int adultsCount = 0;
        int childrenCount = 0;

        for (PersonEntity personEntity : personRepository.findAllByAddressFirestation(stationNumber)) {
            Person person = personMapper.toCompletePerson(personEntity, now);
            res.person(person);
            if (isAdult(person)) {
                ++adultsCount;
            } else {
                ++childrenCount;
            }
        }
        return res.adultsCount(adultsCount).childrenCount(childrenCount).build();
    }

    public ChildAlertResponse getChildAlert(String address) {
        return getChildAlert(address, ZonedDateTime.now());
    }

    @Transactional(readOnly = true)
    public ChildAlertResponse getChildAlert(String address, ZonedDateTime now) {
        ChildAlertResponse.Builder res = ChildAlertResponse.builder();

        for (PersonEntity personEntity : personRepository.findAllByAddressAddress(address)) {
            Person person = personMapper.toCompletePerson(personEntity, now);
            if (isAdult(person)) {
                res.adult(person);
            } else {
                res.children(person);
            }
        }
        return res.build();
    }

    @Transactional(readOnly = true)
    public PhoneAlertResponse getPhoneAlert(String stationNumber) {
        List<String> phones = StreamSupport
                .stream(personRepository.findAllByAddressFirestation(stationNumber).spliterator(), false)
                .map(PersonEntity::getPhone)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return PhoneAlertResponse.builder().phones(phones).build();
    }

    public FireResponse getFire(String address) {
        return getFire(address, ZonedDateTime.now());
    }

    @Transactional(readOnly = true)
    public FireResponse getFire(String address, ZonedDateTime now) {
        FireResponse.Builder res = FireResponse.builder();

        addressRepository.findByAddress(address)
                .ifPresent(addressEntity -> res.stationNumber(addressEntity.getFirestation()));
        for (PersonEntity personEntity : personRepository.findAllByAddressAddress(address)) {
            res.person(personMapper.toCompletePerson(personEntity, now, true));
        }
        return res.build();
    }

    public FloodStationsResponse getFloodStations(List<String> stations) {
        return getFloodStations(stations, ZonedDateTime.now());
    }

    @Transactional(readOnly = true)
    public FloodStationsResponse getFloodStations(List<String> stations, ZonedDateTime now) {
        FloodStationsResponse.Builder res = FloodStationsResponse.builder();

        for (AddressEntity addressEntity : addressRepository.findAllByFirestationIn(stations)) {
            FloodStationsResponse.Entry.Builder entryBuilder = FloodStationsResponse.Entry.builder()
                    .address(addressEntity.getAddress());
            for (PersonEntity personEntity : personRepository.findAllByAddressAddress(addressEntity.getAddress())) {
                entryBuilder.person(personMapper.toCompletePerson(personEntity, now, true));
            }
            FloodStationsResponse.Entry entry = entryBuilder.build();
            if (!entry.getPersons().isEmpty()) {
                res.station(entry);
            }
        }
        return res.build();
    }

    public PersonInfoResponse getPersonInfo(String firstName, String lastName) {
        return getPersonInfo(firstName, lastName, ZonedDateTime.now());
    }

    @Transactional(readOnly = true)
    public PersonInfoResponse getPersonInfo(String firstName, String lastName, ZonedDateTime now) {
        PersonInfoResponse.Builder res = PersonInfoResponse.builder();

        for (PersonEntity personEntity : personRepository.findAllByFirstNameAndLastName(firstName, lastName)) {
            res.person(personMapper.toCompletePerson(personEntity, now, true));
        }
        return res.build();
    }

    @Transactional(readOnly = true)
    public CommunityEmailResponse getCommunityEmail(String city) {
        List<String> emails = StreamSupport
                .stream(personRepository.findAllByAddressCity(city).spliterator(), false)
                .map(PersonEntity::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return CommunityEmailResponse.builder().emails(emails).build();
    }

    public static boolean isAdult(Person person) {
        Integer age = person.getAge();
        return age == null || age >= 18;
    }
}
