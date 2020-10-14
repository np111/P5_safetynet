package com.safetynet.alerts.service;

import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.util.exception.FastException;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
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

    @Transactional
    public Person getPerson(long id) {
        PersonEntity personEntity = personRepository.findById(id).orElse(null);
        return personEntity == null ? null : personEntity.toPerson();
    }

    @Transactional
    public UpdateResult createPerson(Person body, boolean allowSimilarNames)
            throws PersonExistsException, InterferingAddressException, ImmutableNamesException {
        body.setId(null);
        return update(null, body, allowSimilarNames, true);
    }

    @Transactional
    public UpdateResult updatePerson(long id, Person body, boolean allowSimilarNames)
            throws PersonExistsException, InterferingAddressException, ImmutableNamesException {
        body.setId(id);
        PersonEntity personEntity = personRepository.findById(id).orElse(null);
        if (personEntity == null) {
            return null;
        }
        return update(personEntity, body, allowSimilarNames, true);
    }

    @Transactional
    public UpdateResult updatePersonByNames(String firstName, String lastName, Person body)
            throws InterferingNamesException, PersonExistsException, InterferingAddressException, ImmutableNamesException {
        UpdateResult res = null;
        for (PersonEntity personEntity : personRepository.findAllByFirstNameAndLastName(firstName, lastName)) {
            if (res != null) {
                throw new InterferingNamesException();
            }
            res = update(personEntity, body, false, false);
        }
        return res;
    }

    @Transactional
    public boolean deletePerson(long id) {
        return personRepository.removeById(id) != 0;
    }

    @Transactional
    public boolean deletePersonByNames(String firstName, String lastName) throws InterferingNamesException {
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
    private UpdateResult update(PersonEntity entity, Person body, boolean allowSimilarNames,
            boolean allowUpdateNames)
            throws ImmutableNamesException, InterferingAddressException, PersonExistsException {
        // prevent similar names combination if it was not allowed
        boolean create = (entity == null);
        if (create && !allowSimilarNames && personRepository
                .existsByFirstNameAndLastName(body.getFirstName(), body.getLastName())) {
            throw new PersonExistsException();
        }

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
        if (create) {
            entity = new PersonEntity();
            entity.setId(body.getId());
        }
        if (create || allowUpdateNames) {
            entity.setFirstName(body.getFirstName());
            entity.setLastName(body.getLastName());
        } else if (!Objects.equals(entity.getFirstName(), body.getFirstName())
                || !Objects.equals(entity.getLastName(), body.getLastName())) {
            throw new ImmutableNamesException();
        }
        entity.setAddress(addressEntity);
        entity.setPhone(body.getPhone());
        entity.setEmail(body.getEmail());
        personRepository.save(entity);

        // returns result
        return new UpdateResult(create, entity);
    }

    @RequiredArgsConstructor
    public static class UpdateResult {
        private final @Getter boolean created;
        private final @NonNull PersonEntity personEntity;

        public @NonNull Person getPerson() {
            return personEntity.toPerson();
        }
    }

    public static class PersonExistsException extends FastException {
    }

    public static class ImmutableNamesException extends FastException {
    }

    public static class InterferingNamesException extends FastException {
    }

    public static class InterferingAddressException extends FastException {
    }
}
