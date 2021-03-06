package com.safetynet.alerts.service;

import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.repository.mapper.PersonMapper;
import com.safetynet.alerts.util.exception.FastRuntimeException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class PersonService {
    private final PersonRepository personRepository;
    private final AddressRepository addressRepository;
    private final PersonMapper personMapper;

    /**
     * Returns a {@linkplain Person person} by it's ID.
     *
     * @param id ID of the person to return
     * @return the person; or {@code null} if none has the given ID
     */
    @Transactional
    public Person getPerson(long id) {
        return personMapper.toPerson(personRepository.findById(id).orElse(null));
    }

    /**
     * Create a new {@linkplain Person person}.
     *
     * @param body              data (id is ignored)
     * @param allowSimilarNames whether or not similar names combination are allowed
     * @return the created person
     * @throws PersonExistsException       if {@code allowSimilarNames == false} and an existing person has a similar names combination
     * @throws InterferingAddressException if a matching address already exists with a different city/zip combination
     */
    @Transactional
    public Person createPerson(Person body, boolean allowSimilarNames) {
        body.setId(null);
        return update(null, body, allowSimilarNames, true);
    }

    /**
     * Update an existing {@linkplain Person person}.
     *
     * @param id                ID of the person to update
     * @param body              data (id is ignored)
     * @param allowSimilarNames whether or not similar names combination are allowed
     * @return the updated person; or {@code null} if none has the given ID
     * @throws PersonExistsException       if {@code allowSimilarNames == false} and an existing person has a similar names combination
     * @throws InterferingAddressException if a matching address already exists with a different city/zip combination
     */
    @Transactional
    public Person updatePerson(long id, Person body, boolean allowSimilarNames) {
        body.setId(id);
        PersonEntity personEntity = personRepository.findById(id).orElse(null);
        if (personEntity == null) {
            return null;
        }
        return update(personEntity, body, allowSimilarNames, true);
    }

    /**
     * Update an existing {@linkplain Person person} by it's names.
     *
     * @param firstName First name of the person to update
     * @param lastName  Last name of the person to update
     * @param body      data (id is ignored)
     * @return the updated person; or {@code null} if none has the given names
     * @throws InterferingNamesException   if more than one person has this names combination
     * @throws PersonExistsException       if an existing person has a similar names combination
     * @throws InterferingAddressException if a matching address already exists with a different city/zip combination
     * @throws ImmutableNamesException     if you try to update firstName or lastName
     */
    @Transactional
    public Person updatePersonByNames(String firstName, String lastName, Person body) {
        Person res = null;
        for (PersonEntity personEntity : personRepository.findAllByFirstNameAndLastName(firstName, lastName)) {
            if (res != null) {
                throw new InterferingNamesException();
            }
            res = update(personEntity, body, false, false);
        }
        return res;
    }

    /**
     * Delete a {@linkplain Person person} by it's ID.
     *
     * @param id ID of the person to delete
     * @return {@code true} if the person existed and was deleted; or {@code false} if not
     */
    @Transactional
    public boolean deletePerson(long id) {
        return personRepository.removeById(id) != 0;
    }

    /**
     * Delete a {@linkplain Person person} by it's names.
     *
     * @param firstName First name of the person to delete
     * @param lastName  Last name of the person to delete
     * @return {@code true} if the person existed and was deleted; or {@code false} if not
     * @throws InterferingNamesException if more than one person has this names combination
     */
    @Transactional
    public boolean deletePersonByNames(String firstName, String lastName) {
        long count = personRepository.removeByFirstNameAndLastName(firstName, lastName);
        if (count == 0) {
            return false;
        }
        if (count > 1) {
            throw new InterferingNamesException();
        }
        return true;
    }

    /**
     * Create or update the person entity from it's model.
     *
     * @param entity            the existing entity; or {@code null} to create one
     * @param body              the person model to apply
     * @param allowSimilarNames whether or not similar names combination are allowed
     * @param allowUpdateNames  whether or not names updates are allowed
     * @return the result
     */
    private Person update(PersonEntity entity, Person body, boolean allowSimilarNames, boolean allowUpdateNames) {
        boolean create = (entity == null);

        // retrieve or create the address
        AddressEntity addressEntity = addressRepository.findByAddress(body.getAddress()).orElse(null);
        if (addressEntity == null) {
            addressEntity = new AddressEntity();
            addressEntity.setAddress(body.getAddress());
            addressEntity.setCity(body.getCity());
            addressEntity.setZip(body.getZip());
            addressRepository.save(addressEntity);
        } else if (!addressEntity.isComplete()) {
            addressEntity.setCity(body.getCity());
            addressEntity.setZip(body.getZip());
            addressRepository.save(addressEntity);
        } else if (!body.getCity().equals(addressEntity.getCity())
                || !body.getZip().equals(addressEntity.getZip())) {
            throw new InterferingAddressException();
        }

        // create or update the person
        boolean namesUpdated;
        if (create) {
            entity = new PersonEntity();
            entity.setId(body.getId());
            entity.setFirstName(body.getFirstName());
            entity.setLastName(body.getLastName());
            namesUpdated = true;
        } else if (!Objects.equals(entity.getFirstName(), body.getFirstName())
                || !Objects.equals(entity.getLastName(), body.getLastName())) {
            if (!allowUpdateNames) {
                throw new ImmutableNamesException();
            }
            entity.setFirstName(body.getFirstName());
            entity.setLastName(body.getLastName());
            namesUpdated = true;
        } else {
            namesUpdated = false;
        }
        if (namesUpdated && !allowSimilarNames && personRepository
                .existsByFirstNameAndLastName(body.getFirstName(), body.getLastName())) {
            throw new PersonExistsException();
        }
        entity.setAddress(addressEntity);
        entity.setPhone(body.getPhone());
        entity.setEmail(body.getEmail());
        personRepository.save(entity);

        // returns result
        return personMapper.toPerson(entity);
    }

    public static class PersonExistsException extends FastRuntimeException {
    }

    public static class ImmutableNamesException extends FastRuntimeException {
    }

    public static class InterferingNamesException extends FastRuntimeException {
    }

    public static class InterferingAddressException extends FastRuntimeException {
    }
}
