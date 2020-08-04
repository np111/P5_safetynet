package com.safetynet.alerts.controller;

import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.api.validation.constraint.IsName;
import com.safetynet.alerts.api.validation.group.Create;
import com.safetynet.alerts.api.validation.group.Update;
import com.safetynet.alerts.repository.MedicalRecordRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.MedicalRecordEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.util.ApiErrorCode;
import com.safetynet.alerts.util.ApiException;
import com.safetynet.alerts.util.spring.JsonRequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "medical record", description = "CRUD operations about medical records")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/medicalRecord")
@Validated
public class MedicalRecordController {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PersonRepository personRepository;

    @Operation(
            summary = "Find medical record by ID."
            // TODO: documentation to explain that same ID is shared by a person and it's medical record
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.GET, value = "/{personId}")
    @Transactional
    public MedicalRecord getMedicalRecord(
            @Parameter(description = "ID of medical record to return.")
            @PathVariable("personId") @NotNull Long id
    ) {
        MedicalRecordEntity medicalRecordEntity = medicalRecordRepository.findById(id).orElse(null);
        if (medicalRecordEntity == null) {
            throw errorMedicalRecordNotFound();
        }
        return medicalRecordEntity.toMedicalRecord();
    }

    @Operation(
            summary = "Add a new medical record."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Void> createMedicalRecord(
            @Parameter(description = "Medical record object that needs to be added.")
            @RequestBody @Validated({Default.class, Create.class}) MedicalRecord body
    ) {
        body.setPersonId(null);
        return update(null, body);
    }

    @Operation(
            summary = "Update an existing medical record."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT, value = "/{personId}")
    @Transactional
    public ResponseEntity<Void> updateMedicalRecord(
            @Parameter(description = "ID of medical record that needs to be updated.")
            @PathVariable("personId") @NotNull Long id,
            @Parameter(description = "New medical record object.")
            @RequestBody @Validated({Default.class, Update.class}) MedicalRecord body
    ) {
        body.setPersonId(id);
        MedicalRecordEntity medicalRecordEntity = medicalRecordRepository.findById(id).orElse(null);
        if (medicalRecordEntity == null) {
            throw errorMedicalRecordNotFound();
        }
        return update(medicalRecordEntity, body);
    }

    @Operation(
            summary = "Update an existing medical record by person first and last name."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT)
    @Transactional
    public ResponseEntity<Void> updateMedicalRecordByNames(
            @Parameter(description = "First name of person whose medical record needs to be updated.")
            @RequestParam("firstName") @NotNull @IsName String firstName,
            @Parameter(description = "Last name of person whose medical record needs to be updated.")
            @RequestParam("lastName") @NotNull @IsName String lastName,
            @Parameter(description = "New medical record object.")
            @RequestBody @Validated({Default.class, Update.class}) MedicalRecord body
    ) {
        ResponseEntity<Void> res = null;
        for (MedicalRecordEntity medicalRecordEntity : medicalRecordRepository
                .findAllByPersonFirstNameAndPersonLastName(firstName, lastName)) {
            if (res != null) {
                throw errorInterferingNames();
            }
            res = update(medicalRecordEntity, body);
        }
        if (res == null) {
            throw errorMedicalRecordNotFound();
        }
        return res;
    }

    @Operation(
            summary = "Deletes a medical record."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE, value = "/{personId}")
    @Transactional
    public ResponseEntity<Void> deleteMedicalRecord(
            @Parameter(description = "ID of medical record that needs to be deleted.")
            @PathVariable("personId") @NotNull Long id
    ) {
        if (medicalRecordRepository.removeById(id) == 0) {
            throw errorMedicalRecordNotFound();
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Deletes a medical record by person first and last name."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE)
    @Transactional
    public ResponseEntity<Void> deleteMedicalRecordByNames(
            @Parameter(description = "First name of person whose medical record needs to be deleted.")
            @RequestParam("firstName") @NotNull @IsName String firstName,
            @Parameter(description = "Last name of person whose medical record needs to be deleted.")
            @RequestParam("lastName") @NotNull @IsName String lastName
    ) {
        long count = medicalRecordRepository.removeByPersonFirstNameAndPersonLastName(firstName, lastName);
        if (count == 0) {
            throw errorMedicalRecordNotFound();
        }
        if (count > 1) {
            throw errorInterferingNames();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Create or update the medical record entity from it's model.
     *
     * @param entity the existing entity; or {@code null} to create one
     * @param body   the medical record model to apply
     * @return the REST response
     */
    private ResponseEntity<Void> update(MedicalRecordEntity entity, MedicalRecord body) {
        boolean create = (entity == null);

        // create or update the medical record
        if (create) {
            entity = new MedicalRecordEntity();
            PersonEntity personEntity = findPerson(body);
            entity.setPerson(personEntity);
            if (medicalRecordRepository.existsByPersonId(personEntity.getId())) {
                throw errorMedicalRecordExists();
            }
        }
        entity.setBirthdate(body.getBirthdate());
        entity.setMedications(body.getMedications());
        entity.setAllergies(body.getAllergies());
        medicalRecordRepository.save(entity);

        // returns rest response
        if (create) {
            return ResponseEntity.created(getLocation(entity)).build();
        } else {
            return ResponseEntity.noContent().location(getLocation(entity)).build();
        }
    }

    /**
     * Find the person entity targeted by a medical record.
     *
     * @throws ApiException {@link #errorInterferingNames()}, {@link #errorPersonNotFound()}
     */
    private PersonEntity findPerson(MedicalRecord body) {
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
                    throw errorInterferingNames();
                }
                personEntity = e;
            }
        }

        if (personEntity == null) {
            // fail if nobody was found
            throw errorPersonNotFound();
        }

        return personEntity;
    }

    /**
     * Returns the URL to a medical record.
     */
    @SneakyThrows
    private URI getLocation(MedicalRecordEntity medicalRecordEntity) {
        // TODO: Returns full URI instead of relative
        return new URI("/medicalRecord/" + medicalRecordEntity.getId());
    }

    /**
     * Returns a SERVICE/ALREADY_EXISTS error when a medical record already exists for a person.
     */
    private ApiException errorMedicalRecordExists() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.SERVICE)
                .status(409)
                .code(ApiErrorCode.ALREADY_EXISTS)
                .message("A medical record already exists for this person")
                .build());
    }

    /**
     * Returns a SERVICE/NOT_FOUND error when a medical record does not exists.
     */
    private ApiException errorMedicalRecordNotFound() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.SERVICE)
                .status(404)
                .code(ApiErrorCode.NOT_FOUND)
                .message("Medical record not found")
                .build());
    }

    /**
     * Returns a SERVICE/NOT_FOUND error when a person does not exists.
     */
    private ApiException errorPersonNotFound() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.SERVICE)
                .status(404)
                .code(ApiErrorCode.NOT_FOUND)
                .message("The person linked to this medical file cannot be found")
                .build());
    }

    /**
     * Returns a SERVICE/INTERFERING_NAMES error when identifying a person by first and last names is impossible because
     * multiples persons share the same names.
     */
    private ApiException errorInterferingNames() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.SERVICE)
                .status(409)
                .code(ApiErrorCode.INTERFERING_NAMES)
                .message("Multiple medical records share this names combination, use ID instead")
                .build());
    }
}
