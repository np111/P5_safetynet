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
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class AlertsServiceTest {
    private static final ZonedDateTime now = ZonedDateTime.of(2001, 10, 25, 15, 29, 17, 0, ZoneId.of("Europe/Paris"));

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PersonRepository personRepository;

    private final PersonMapper personMapper = new PersonMapper();

    private AlertsService alertsService;

    private final PodamFactory factory = new PodamFactoryImpl();

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        alertsService = new AlertsService(addressRepository, personRepository, personMapper);
    }

    @Test
    void getPersonsCoveredByFirestation() {
        PersonEntity child1 = factory.manufacturePojo(PersonEntity.class);
        child1.getMedicalRecord().setBirthdate(now.minusYears(5).toLocalDate());
        PersonEntity child2 = factory.manufacturePojo(PersonEntity.class);
        child2.getMedicalRecord().setBirthdate(now.minusYears(15).toLocalDate());
        PersonEntity adult1 = factory.manufacturePojo(PersonEntity.class);
        adult1.getMedicalRecord().setBirthdate(now.minusYears(20).toLocalDate());
        PersonEntity adult2 = factory.manufacturePojo(PersonEntity.class);
        adult2.getMedicalRecord().setBirthdate(now.minusYears(50).toLocalDate());

        when(personRepository.findAllByAddressFirestation("A1"))
                .thenReturn(Arrays.asList(child1, child2, adult1, adult2));

        PersonsCoveredByFirestationResponse res = alertsService.getPersonsCoveredByFirestation("A1", now);
        assertEquals(PersonsCoveredByFirestationResponse.builder()
                .childrenCount(2)
                .adultsCount(2)
                .person(personMapper.toCompletePerson(child1, now))
                .person(personMapper.toCompletePerson(child2, now))
                .person(personMapper.toCompletePerson(adult1, now))
                .person(personMapper.toCompletePerson(adult2, now))
                .build(), res);
    }

    @Test
    void getPersonsCoveredByFirestationNow() {
        AlertsService alertsServiceSpy = Mockito.spy(alertsService);
        ZonedDateTime[] overloadedArg = new ZonedDateTime[1];
        doAnswer(ctx -> {
            overloadedArg[0] = ctx.getArgument(1);
            return null;
        }).when(alertsServiceSpy).getPersonsCoveredByFirestation(eq("A"), any(ZonedDateTime.class));
        alertsServiceSpy.getPersonsCoveredByFirestation("A");
        assertThat(Duration.between(ZonedDateTime.now(), overloadedArg[0]).abs()).isLessThan(Duration.ofSeconds(1));
    }

    @Test
    void getChildAlert() {
        PersonEntity child1 = factory.manufacturePojo(PersonEntity.class);
        child1.getMedicalRecord().setBirthdate(now.minusYears(5).toLocalDate());
        PersonEntity child2 = factory.manufacturePojo(PersonEntity.class);
        child2.getMedicalRecord().setBirthdate(now.minusYears(15).toLocalDate());
        PersonEntity adult1 = factory.manufacturePojo(PersonEntity.class);
        adult1.getMedicalRecord().setBirthdate(now.minusYears(20).toLocalDate());
        PersonEntity adult2 = factory.manufacturePojo(PersonEntity.class);
        adult2.getMedicalRecord().setBirthdate(now.minusYears(50).toLocalDate());

        when(personRepository.findAllByAddressAddress("1509 Culver St"))
                .thenReturn(Arrays.asList(child1, child2, adult1, adult2));

        ChildAlertResponse res = alertsService.getChildAlert("1509 Culver St", now);
        assertEquals(ChildAlertResponse.builder()
                .children(personMapper.toCompletePerson(child1, now))
                .children(personMapper.toCompletePerson(child2, now))
                .adult(personMapper.toCompletePerson(adult1, now))
                .adult(personMapper.toCompletePerson(adult2, now))
                .build(), res);
    }

    @Test
    void getChildAlertNow() {
        AlertsService alertsServiceSpy = Mockito.spy(alertsService);
        ZonedDateTime[] overloadedArg = new ZonedDateTime[1];
        doAnswer(ctx -> {
            overloadedArg[0] = ctx.getArgument(1);
            return null;
        }).when(alertsServiceSpy).getChildAlert(eq("A"), any(ZonedDateTime.class));
        alertsServiceSpy.getChildAlert("A");
        assertThat(Duration.between(ZonedDateTime.now(), overloadedArg[0]).abs()).isLessThan(Duration.ofSeconds(1));
    }

    @Test
    void getPhoneAlert() {
        PersonEntity person1 = factory.manufacturePojo(PersonEntity.class);
        person1.setPhone("123-456-7890");
        PersonEntity person2 = factory.manufacturePojo(PersonEntity.class);
        person2.setPhone("012-345-6789");
        PersonEntity person3 = factory.manufacturePojo(PersonEntity.class);
        person3.setPhone("000-000-0000");

        when(personRepository.findAllByAddressFirestation("A1"))
                .thenReturn(Arrays.asList(person1, person2, person3));

        PhoneAlertResponse res = alertsService.getPhoneAlert("A1");
        assertEquals(PhoneAlertResponse.builder()
                .phone("123-456-7890")
                .phone("012-345-6789")
                .phone("000-000-0000")
                .build(), res);
    }

    @Test
    void getFire() {
        getFire(true);
    }

    @Test
    void getFireWithoutStationNumber() {
        getFire(false);
    }

    private void getFire(boolean withStationNumber) {
        AddressEntity addressEntity = withStationNumber ? factory.manufacturePojo(AddressEntity.class) : null;
        PersonEntity person1 = factory.manufacturePojo(PersonEntity.class);
        PersonEntity person2 = factory.manufacturePojo(PersonEntity.class);
        PersonEntity person3 = factory.manufacturePojo(PersonEntity.class);

        when(addressRepository.findByAddress("1509 Culver St"))
                .thenReturn(Optional.ofNullable(addressEntity));
        when(personRepository.findAllByAddressAddress("1509 Culver St"))
                .thenReturn(Arrays.asList(person1, person2, person3));

        FireResponse res = alertsService.getFire("1509 Culver St", now);
        assertEquals(FireResponse.builder()
                .stationNumber(addressEntity != null ? addressEntity.getFirestation() : null)
                .person(personMapper.toCompletePerson(person1, now, true))
                .person(personMapper.toCompletePerson(person2, now, true))
                .person(personMapper.toCompletePerson(person3, now, true))
                .build(), res);
    }

    @Test
    void getFireNow() {
        AlertsService alertsServiceSpy = Mockito.spy(alertsService);
        ZonedDateTime[] overloadedArg = new ZonedDateTime[1];
        doAnswer(ctx -> {
            overloadedArg[0] = ctx.getArgument(1);
            return null;
        }).when(alertsServiceSpy).getFire(eq("A"), any(ZonedDateTime.class));
        alertsServiceSpy.getFire("A");
        assertThat(Duration.between(ZonedDateTime.now(), overloadedArg[0]).abs()).isLessThan(Duration.ofSeconds(1));
    }

    @Test
    void getFloodStations() {
        AddressEntity address1 = factory.manufacturePojo(AddressEntity.class);
        AddressEntity address2 = factory.manufacturePojo(AddressEntity.class);
        PersonEntity person1 = factory.manufacturePojo(PersonEntity.class);
        PersonEntity person2 = factory.manufacturePojo(PersonEntity.class);
        PersonEntity person3 = factory.manufacturePojo(PersonEntity.class);

        when(addressRepository.findAllByFirestationIn(Arrays.asList("A1", "A2")))
                .thenReturn(Arrays.asList(address1, address2));
        when(personRepository.findAllByAddressAddress(address1.getAddress()))
                .thenReturn(Arrays.asList(person1, person2));
        when(personRepository.findAllByAddressAddress(address2.getAddress()))
                .thenReturn(Collections.singletonList(person3));

        FloodStationsResponse res = alertsService.getFloodStations(Arrays.asList("A1", "A2"), now);
        assertEquals(FloodStationsResponse.builder()
                .station(FloodStationsResponse.Entry.builder()
                        .address(address1.getAddress())
                        .person(personMapper.toCompletePerson(person1, now, true))
                        .person(personMapper.toCompletePerson(person2, now, true))
                        .build())
                .station(FloodStationsResponse.Entry.builder()
                        .address(address2.getAddress())
                        .person(personMapper.toCompletePerson(person3, now, true))
                        .build())
                .build(), res);
    }

    @Test
    void getFloodStationsNow() {
        AlertsService alertsServiceSpy = Mockito.spy(alertsService);
        ZonedDateTime[] overloadedArg = new ZonedDateTime[1];
        doAnswer(ctx -> {
            overloadedArg[0] = ctx.getArgument(1);
            return null;
        }).when(alertsServiceSpy).getFloodStations(eq(Collections.singletonList("A")), any(ZonedDateTime.class));
        alertsServiceSpy.getFloodStations(Collections.singletonList("A"));
        assertThat(Duration.between(ZonedDateTime.now(), overloadedArg[0]).abs()).isLessThan(Duration.ofSeconds(1));
    }

    @Test
    void getPersonInfo() {
        PersonEntity person1 = factory.manufacturePojo(PersonEntity.class);
        PersonEntity person2 = factory.manufacturePojo(PersonEntity.class);

        when(personRepository.findAllByFirstNameAndLastName("Jean", "Sebastien"))
                .thenReturn(Arrays.asList(person1, person2));

        PersonInfoResponse res = alertsService.getPersonInfo("Jean", "Sebastien", now);
        assertEquals(PersonInfoResponse.builder()
                .person(personMapper.toCompletePerson(person1, now, true))
                .person(personMapper.toCompletePerson(person2, now, true))
                .build(), res);
    }

    @Test
    void getPersonInfoNow() {
        AlertsService alertsServiceSpy = Mockito.spy(alertsService);
        ZonedDateTime[] overloadedArg = new ZonedDateTime[1];
        doAnswer(ctx -> {
            overloadedArg[0] = ctx.getArgument(2);
            return null;
        }).when(alertsServiceSpy).getPersonInfo(eq("A"), eq("B"), any(ZonedDateTime.class));
        alertsServiceSpy.getPersonInfo("A", "B");
        assertThat(Duration.between(ZonedDateTime.now(), overloadedArg[0]).abs()).isLessThan(Duration.ofSeconds(1));
    }

    @Test
    void getCommunityEmail() {
        PersonEntity person1 = factory.manufacturePojo(PersonEntity.class);
        PersonEntity person2 = factory.manufacturePojo(PersonEntity.class);

        when(personRepository.findAllByAddressCity("Paris"))
                .thenReturn(Arrays.asList(person1, person2));

        CommunityEmailResponse res = alertsService.getCommunityEmail("Paris");
        assertEquals(CommunityEmailResponse.builder()
                .email(person1.getEmail())
                .email(person2.getEmail())
                .build(), res);
    }

    @Test
    void isAdult() {
        assertFalse(AlertsService.isAdult(Person.builder().age(7).build()));
        assertFalse(AlertsService.isAdult(Person.builder().age(17).build()));
        assertTrue(AlertsService.isAdult(Person.builder().age(18).build()));
        assertTrue(AlertsService.isAdult(Person.builder().age(50).build()));
        assertTrue(AlertsService.isAdult(Person.builder().age(null).build()));
    }
}
