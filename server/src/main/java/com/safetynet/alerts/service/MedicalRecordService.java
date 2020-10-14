package com.safetynet.alerts.service;

import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.repository.MedicalRecordRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.util.exception.FastException;
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

    @Transactional
    public MedicalRecord getMedicalRecord(long id) {
        MedicalRecordEntity medicalRecordEntity = medicalRecordRepository.findById(id).orElse(null);
        return medicalRecordEntity == null ? null : medicalRecordEntity.toMedicalRecord();
    }

    @Transactional
    public UpdateResult createMedicalRecord(MedicalRecord body)
            throws InterferingNamesException, PersonNotFoundException, MedicalRecordExistsException {
        body.setPersonId(null);
        return update(null, body);
    }

    @Transactional
    public UpdateResult updateMedicalRecord(long id, MedicalRecord body)
            throws InterferingNamesException, PersonNotFoundException, MedicalRecordExistsException {
        body.setPersonId(id);
        MedicalRecordEntity medicalRecordEntity = medicalRecordRepository.findById(id).orElse(null);
        if (medicalRecordEntity == null) {
            return null;
        }
        return update(medicalRecordEntity, body);
    }

    @Transactional
    public UpdateResult updateMedicalRecordByNames(String firstName, String lastName, MedicalRecord body)
            throws InterferingNamesException, MedicalRecordExistsException, PersonNotFoundException {
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

    @Transactional
    public boolean deleteMedicalRecord(long id) {
        return medicalRecordRepository.removeById(id) != 0;
    }

    @Transactional
    public boolean deleteMedicalRecordByNames(String firstName, String lastName) throws InterferingNamesException {
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
    private UpdateResult update(MedicalRecordEntity entity, MedicalRecord body)
            throws InterferingNamesException, PersonNotFoundException, MedicalRecordExistsException {
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
        return new UpdateResult(create, entity);
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
    public static class UpdateResult {
        private final @Getter boolean created;
        private final @NonNull MedicalRecordEntity medicalRecordEntity;

        public @NonNull MedicalRecord getMedicalRecord() {
            return medicalRecordEntity.toMedicalRecord();
        }
    }

    public static class MedicalRecordExistsException extends FastException {
    }

    public static class PersonNotFoundException extends FastException {
    }

    public static class InterferingNamesException extends FastException {
    }
}
