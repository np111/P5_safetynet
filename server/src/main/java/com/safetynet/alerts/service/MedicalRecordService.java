package com.safetynet.alerts.service;

import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.repository.MedicalRecordRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.repository.mapper.MedicalRecordMapper;
import com.safetynet.alerts.util.exception.FastRuntimeException;
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
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PersonRepository personRepository;

    /**
     * Returns a {@linkplain MedicalRecord medical record} by it's ID.
     *
     * @param id ID of the medical record to return
     * @return the medical record; or {@code null} if none has the given ID
     */
    @Transactional
    public MedicalRecord getMedicalRecord(long id) {
        return MedicalRecordMapper.getInstance().toMedicalRecord(medicalRecordRepository.findById(id).orElse(null));
    }

    /**
     * Create a new {@linkplain MedicalRecord medical record}.
     *
     * @param body data (personId is ignored)
     * @return the created medical record
     * @throws InterferingNamesException    if more than one person matches this medical record
     * @throws PersonNotFoundException      if no person matches this medical record
     * @throws MedicalRecordExistsException if a medical record already exists for this person
     */
    @Transactional
    public UpdateResult createMedicalRecord(MedicalRecord body) {
        body.setPersonId(null);
        return update(null, body);
    }

    /**
     * Update an existing {@linkplain MedicalRecord medical record}.
     *
     * @param id   ID of the medical record to update
     * @param body data (personId is ignored)
     * @return the updated medical record
     */
    @Transactional
    public UpdateResult updateMedicalRecord(long id, MedicalRecord body) {
        body.setPersonId(id);
        MedicalRecordEntity medicalRecordEntity = medicalRecordRepository.findById(id).orElse(null);
        if (medicalRecordEntity == null) {
            return null;
        }
        return update(medicalRecordEntity, body);
    }

    /**
     * Update an existing {@linkplain MedicalRecord medical record} by it's person names.
     *
     * @param firstName First name of the person to update
     * @param lastName  Last name of the person to update
     * @param body      data (personId is ignored)
     * @return the updated medical record; or {@code null} if none has the given names
     * @throws InterferingNamesException if more than one person matches this medical record
     */
    @Transactional
    public UpdateResult updateMedicalRecordByNames(String firstName, String lastName, MedicalRecord body) {
        UpdateResult res = null;
        for (MedicalRecordEntity medicalRecordEntity : medicalRecordRepository
                .findAllByPersonFirstNameAndPersonLastName(firstName, lastName)) {
            if (res != null) {
                throw new InterferingNamesException();
            }
            res = update(medicalRecordEntity, body);
        }
        return res;
    }

    /**
     * Delete a {@linkplain MedicalRecord medical record} by it's ID.
     *
     * @param id ID of the medical record to delete
     * @return {@code true} if the medical record existed and was deleted; or {@code false} if not
     */
    @Transactional
    public boolean deleteMedicalRecord(long id) {
        return medicalRecordRepository.removeById(id) != 0;
    }

    /**
     * Delete a {@linkplain MedicalRecord medical record} by it's person names.
     *
     * @param firstName First name of the person to delete
     * @param lastName  Last name of the person to delete
     * @return {@code true} if the medical record existed and was deleted; or {@code false} if not
     * @throws InterferingNamesException if more than one person has this names combination
     */
    @Transactional
    public boolean deleteMedicalRecordByNames(String firstName, String lastName) {
        long count = medicalRecordRepository.removeByPersonFirstNameAndPersonLastName(firstName, lastName);
        if (count == 0) {
            return false;
        }
        if (count > 1) {
            throw new InterferingNamesException();
        }
        return true;
    }

    /**
     * Create or update the medical record entity from it's model.
     *
     * @param entity the existing entity; or {@code null} to create one
     * @param body   the medical record model to apply
     * @return the result
     */
    private UpdateResult update(MedicalRecordEntity entity, MedicalRecord body) {
        boolean create = (entity == null);

        // create or update the medical record
        if (create) {
            entity = new MedicalRecordEntity();
            PersonEntity personEntity = findPerson(body);
            entity.setPerson(personEntity);
            if (medicalRecordRepository.existsByPersonId(personEntity.getId())) {
                throw new MedicalRecordExistsException();
            }
        }
        entity.setBirthdate(body.getBirthdate());
        entity.setMedications(body.getMedications());
        entity.setAllergies(body.getAllergies());
        medicalRecordRepository.save(entity);

        // returns response
        return new UpdateResult(create, MedicalRecordMapper.getInstance().toMedicalRecord(entity));
    }

    /**
     * Find the person entity targeted by a medical record.
     */
    private PersonEntity findPerson(MedicalRecord body) throws InterferingNamesException, PersonNotFoundException {
        PersonEntity personEntity = null;
        if (body.getPersonId() != null) {
            // find person by ID
            personEntity = personRepository.findById(body.getPersonId()).orElse(null);
        } else if (body.getFirstName() != null && body.getLastName() != null) {
            // find person by fist and last name
            // but fail if multiple persons share the same fist and last name combination
            for (PersonEntity e : personRepository
                    .findAllByFirstNameAndLastName(body.getFirstName(), body.getLastName())) {
                if (personEntity != null) {
                    throw new InterferingNamesException();
                }
                personEntity = e;
            }
        }

        if (personEntity == null) {
            // fail if nobody was found
            throw new PersonNotFoundException();
        }

        return personEntity;
    }

    @RequiredArgsConstructor
    @Getter
    public static class UpdateResult {
        private final boolean created;
        private final @NonNull MedicalRecord medicalRecord;
    }

    public static class MedicalRecordExistsException extends FastRuntimeException {
    }

    public static class PersonNotFoundException extends FastRuntimeException {
    }

    public static class InterferingNamesException extends FastRuntimeException {
    }
}
