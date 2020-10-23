package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.service.PersonService;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@UtilityClass
public class PersonServiceMock {
    public static void init(PersonService personService) throws Exception {
        // GET
        // - Get an existing person
        when(personService.getPerson(knownPerson().getId()))
                .thenReturn(knownPerson());

        // CREATE
        // - Create a new person
        when(personService.createPerson(eq(unknownPerson()), anyBoolean()))
                .thenReturn(new PersonService.UpdateResult(true, unknownPerson()));

        // - Create an existing person (allowSimilarNames=false)
        when(personService.createPerson(knownPerson(), false))
                .thenThrow(new PersonService.PersonExistsException());

        // - Create an existing person (allowSimilarNames=true)
        when(personService.createPerson(knownPerson(), true))
                .thenReturn(new PersonService.UpdateResult(true, knownPerson()));

        // - Create a new person with an interfering address
        when(personService.createPerson(eq(interferingAddressPerson()), anyBoolean()))
                .thenThrow(new PersonService.InterferingAddressException());

        // UPDATE
        // - Update an existing person (without changing names)
        when(personService.updatePerson(eq(knownPerson().getId()), eq(knownPerson()), anyBoolean()))
                .thenReturn(new PersonService.UpdateResult(false, knownPerson()));

        // - Update an existing person (changing names to matching names - allowSimilarNames=false)
        when(personService.updatePerson(knownPerson().getId(), manyPerson(), false))
                .thenThrow(new PersonService.PersonExistsException());

        // - Update an existing person (changing names to matching names - allowSimilarNames=true)
        when(personService.updatePerson(knownPerson().getId(), manyPerson(), true))
                .thenReturn(new PersonService.UpdateResult(false, manyPerson()));

        // - Update an existing person to an interfering address
        when(personService.updatePerson(eq(knownPerson().getId()), eq(interferingAddressPerson()), anyBoolean()))
                .thenThrow(new PersonService.InterferingAddressException());

        // UPDATE BY NAMES
        // - Update an existing person (without changing names)
        when(personService.updatePersonByNames(knownPerson().getFirstName(), knownPerson().getLastName(), knownPerson()))
                .thenReturn(new PersonService.UpdateResult(false, knownPerson()));

        // - Update an existing person (changing names)
        when(personService.updatePersonByNames(knownPerson().getFirstName(), knownPerson().getLastName(), manyPerson()))
                .thenThrow(new PersonService.ImmutableNamesException());

        // - Update an existing person with names matching many persons
        when(personService.updatePersonByNames(manyPerson().getFirstName(), manyPerson().getLastName(), manyPerson()))
                .thenThrow(new PersonService.InterferingNamesException());

        // - Update an existing person to an interfering address
        when(personService.updatePersonByNames(knownPerson().getFirstName(), knownPerson().getLastName(), interferingAddressPerson()))
                .thenThrow(new PersonService.InterferingAddressException());

        // DELETE
        // - Delete an existing person
        when(personService.deletePerson(knownPerson().getId()))
                .thenReturn(true);

        // DELETE BY NAMES
        // - Delete an existing person
        when(personService.deletePersonByNames(knownPerson().getFirstName(), knownPerson().getLastName()))
                .thenReturn(true);

        // - Delete an existing person with names matching many persons
        when(personService.deletePersonByNames(manyPerson().getFirstName(), manyPerson().getLastName()))
                .thenThrow(new PersonService.InterferingNamesException());
    }

    public static Person knownPerson() {
        return Person.builder()
                .id(1L)
                .firstName("Known")
                .lastName("Person")
                .address("9-3")
                .city("Paris")
                .zip("93000")
                .phone("123-456-7890")
                .email("mail@domain.tld")
                .build();
    }

    public static String knownPersonJson() {
        return "{\"id\":1"
                + ",\"firstName\":\"Known\""
                + ",\"lastName\":\"Person\""
                + ",\"address\":\"9-3\""
                + ",\"city\":\"Paris\""
                + ",\"zip\":\"93000\""
                + ",\"phone\":\"123-456-7890\""
                + ",\"email\":\"mail@domain.tld\""
                + "}";
    }

    public static Person unknownPerson() {
        return Person.builder()
                .id(2L)
                .firstName("Unknown")
                .lastName("Person")
                .address("9-3")
                .city("Paris")
                .zip("93000")
                .phone("123-456-7890")
                .email("mail@domain.tld")
                .build();
    }

    public static String unknownPersonJson() {
        return "{\"id\":2"
                + ",\"firstName\":\"Unknown\""
                + ",\"lastName\":\"Person\""
                + ",\"address\":\"9-3\""
                + ",\"city\":\"Paris\""
                + ",\"zip\":\"93000\""
                + ",\"phone\":\"123-456-7890\""
                + ",\"email\":\"mail@domain.tld\""
                + "}";
    }

    public static Person manyPerson() {
        return Person.builder()
                .id(3L)
                .firstName("Many")
                .lastName("Person")
                .address("9-3")
                .city("Paris")
                .zip("93000")
                .phone("123-456-7890")
                .email("mail@domain.tld")
                .build();
    }

    public static String manyPersonJson() {
        return "{\"id\":3"
                + ",\"firstName\":\"Many\""
                + ",\"lastName\":\"Person\""
                + ",\"address\":\"9-3\""
                + ",\"city\":\"Paris\""
                + ",\"zip\":\"93000\""
                + ",\"phone\":\"123-456-7890\""
                + ",\"email\":\"mail@domain.tld\""
                + "}";
    }

    public static Person interferingAddressPerson() {
        return Person.builder()
                .id(4L)
                .firstName("Another")
                .lastName("Person")
                .address("9-3")
                .city("Not Paris")
                .zip("10000")
                .phone("123-456-7890")
                .email("mail@domain.tld")
                .build();
    }

    public static String interferingAddressPersonJson() {
        return "{\"id\":4"
                + ",\"firstName\":\"Another\""
                + ",\"lastName\":\"Person\""
                + ",\"address\":\"9-3\""
                + ",\"city\":\"Not Paris\""
                + ",\"zip\":\"10000\""
                + ",\"phone\":\"123-456-7890\""
                + ",\"email\":\"mail@domain.tld\""
                + "}";
    }

    public static String invalidPersonId() {
        return "nan";
    }

    public static String invalidPersonFirstName() {
        return StringUtils.repeat('a', 256);
    }

    public static String invalidPersonLastName() {
        return StringUtils.repeat('b', 256);
    }

    public static String invalidPersonJson() {
        return "{\"id\":\"nan\"}";
    }
}
