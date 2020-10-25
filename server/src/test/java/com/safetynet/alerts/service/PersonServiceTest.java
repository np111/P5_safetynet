package com.safetynet.alerts.service;

import com.safetynet.alerts.PodamFactoryUtil;
import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.repository.mapper.PersonMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersonServiceTest {
    @Mock
    private PersonRepository personRepository;

    @Mock
    private AddressRepository addressRepository;

    private final PersonMapper personMapper = new PersonMapper();

    private PersonService personService;

    private final PodamFactory factory = PodamFactoryUtil.createPodamFactory();

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        personService = new PersonService(personRepository, addressRepository, personMapper);
    }

    @Test
    void getPerson() {
        PersonEntity person = factory.manufacturePojo(PersonEntity.class);

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        assertEquals(personMapper.toPerson(person), personService.getPerson(1L));
        assertNull(personService.getPerson(2L));
    }

    @Test
    void createPerson() {
        createPerson(false, false);
    }

    @Test
    void createPersonAllowingSimilarNames() {
        createPerson(true, false);
    }

    @Test
    void createPersonExisting() {
        createPerson(false, true);
    }

    @Test
    void createPersonExistingAllowingSimilarNames() {
        createPerson(true, true);
    }

    private void createPerson(boolean allowSimilarNames, boolean alreadyExists) {
        Person person = Person.builder()
                .firstName("Jean")
                .lastName("Sebastien")
                .address("1509 Culver St")
                .build();

        when(personRepository.existsByFirstNameAndLastName(person.getFirstName(), person.getLastName()))
                .thenReturn(alreadyExists);

        if (alreadyExists && !allowSimilarNames) {
            assertThrows(PersonService.PersonExistsException.class,
                    () -> personService.createPerson(person.clone(), allowSimilarNames));
        } else {
            Person res = personService.createPerson(person.clone(), allowSimilarNames);
            assertEquals(person, res);
            verify(personRepository, times(1)).save(any());
        }
    }

    @Test
    void updatePersonNotExisting() {
        Person res = personService.updatePerson(1L, Person.builder().build(), false);
        assertNull(res);
    }

    @Test
    void updatePerson() {
        updatePerson(false, false);
    }

    @Test
    void updatePersonAllowingSimilarNames() {
        updatePerson(true, false);
    }

    @Test
    void updatePersonWithNewNames() {
        updatePerson(false, true);
    }

    @Test
    void updatePersonWithNewNamesAllowingSimilarNames() {
        updatePerson(true, true);
    }

    private void updatePerson(boolean allowSimilarNames, boolean withNewNames) {
        Person person = Person.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Sebastien")
                .address("1509 Culver St")
                .build();

        PersonEntity existingPerson = factory.manufacturePojo(PersonEntity.class);
        existingPerson.setId(person.getId());
        if (!withNewNames) {
            existingPerson.setFirstName(person.getFirstName());
            existingPerson.setLastName(person.getLastName());
        }
        when(personRepository.findById(person.getId()))
                .thenReturn(Optional.of(existingPerson));
        when(personRepository.existsByFirstNameAndLastName(person.getFirstName(), person.getLastName()))
                .thenReturn(true);

        if (withNewNames && !allowSimilarNames) {
            assertThrows(PersonService.PersonExistsException.class,
                    () -> personService.updatePerson(person.getId(), person.clone(), allowSimilarNames));
        } else {
            Person res = personService.updatePerson(person.getId(), person.clone(), allowSimilarNames);
            assertEquals(person, res);
            verify(personRepository, times(1)).save(any());
        }
    }

    @Test
    void updatePersonByNames() {
        updatePersonByNames(false, false);
    }

    @Test
    void updatePersonByNamesWithNewNames() {
        updatePersonByNames(true, false);
    }

    @Test
    void updatePersonByNamesInterfering() {
        updatePersonByNames(false, true);
    }

    private void updatePersonByNames(boolean withNewNames, boolean interfering) {
        Person person = Person.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Sebastien")
                .address("1509 Culver St")
                .build();

        PersonEntity existingPerson = factory.manufacturePojo(PersonEntity.class);
        existingPerson.setId(person.getId());
        if (!withNewNames) {
            existingPerson.setFirstName(person.getFirstName());
            existingPerson.setLastName(person.getLastName());
        }
        when(personRepository.findAllByFirstNameAndLastName(person.getFirstName(), person.getLastName()))
                .thenReturn(interfering ? Arrays.asList(existingPerson, existingPerson) : Collections.singletonList(existingPerson));
        when(personRepository.existsByFirstNameAndLastName(person.getFirstName(), person.getLastName()))
                .thenReturn(true);

        if (withNewNames) {
            assertThrows(PersonService.ImmutableNamesException.class,
                    () -> personService.updatePersonByNames(person.getFirstName(), person.getLastName(), person.clone()));
        } else if (interfering) {
            assertThrows(PersonService.InterferingNamesException.class,
                    () -> personService.updatePersonByNames(person.getFirstName(), person.getLastName(), person.clone()));
        } else {
            Person res = personService.updatePersonByNames(person.getFirstName(), person.getLastName(), person.clone());
            assertEquals(person, res);
            verify(personRepository, times(1)).save(any());
        }
    }

    @Test
    void deletePersonNotExisting() {
        assertFalse(personService.deletePerson(1L));
    }

    @Test
    void deletePerson() {
        when(personRepository.removeById(1L)).thenReturn(1L);

        assertTrue(personService.deletePerson(1L));
    }

    @Test
    void deletePersonByNamesNotExisting() {
        assertFalse(personService.deletePersonByNames("A", "B"));
    }

    @Test
    void deletePersonByNames() {
        when(personRepository.removeByFirstNameAndLastName("A", "B")).thenReturn(1L);

        assertTrue(personService.deletePersonByNames("A", "B"));
    }

    @Test
    void deletePersonByNamesInterfering() {
        when(personRepository.removeByFirstNameAndLastName("A", "B")).thenReturn(2L);

        assertThrows(PersonService.InterferingNamesException.class,
                () -> personService.deletePersonByNames("A", "B"));
    }
}