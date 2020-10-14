package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.api.validation.constraint.IsName;
import com.safetynet.alerts.api.validation.group.Create;
import com.safetynet.alerts.api.validation.group.Update;
import com.safetynet.alerts.service.MedicalRecordService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final MedicalRecordService medicalRecordService;

    @Operation(
            summary = "Find medical record by ID."
            // TODO: documentation to explain that same ID is shared by a person and it's medical record
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.GET, value = "/{personId}")
    public MedicalRecord getMedicalRecord(
            @Parameter(description = "ID of medical record to return.")
            @PathVariable("personId") @NotNull Long id
    ) {
        MedicalRecord res = medicalRecordService.getMedicalRecord(id);
        if (res == null) {
            throw errorMedicalRecordNotFound();
        }
        return res;
    }

    @Operation(
            summary = "Add a new medical record."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createMedicalRecord(
            @Parameter(description = "Medical record object that needs to be added.")
            @RequestBody @Validated({Default.class, Create.class}) MedicalRecord body
    ) {
        return toResponse(() -> medicalRecordService.createMedicalRecord(body));
    }

    @Operation(
            summary = "Update an existing medical record."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT, value = "/{personId}")
    public ResponseEntity<Void> updateMedicalRecord(
            @Parameter(description = "ID of medical record that needs to be updated.")
            @PathVariable("personId") @NotNull Long id,
            @Parameter(description = "New medical record object.")
            @RequestBody @Validated({Default.class, Update.class}) MedicalRecord body
    ) {
        return toResponse(() -> medicalRecordService.updateMedicalRecord(id, body));
    }

    @Operation(
            summary = "Update an existing medical record by person first and last name."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Void> updateMedicalRecordByNames(
            @Parameter(description = "First name of person whose medical record needs to be updated.")
            @RequestParam("firstName") @NotNull @IsName String firstName,
            @Parameter(description = "Last name of person whose medical record needs to be updated.")
            @RequestParam("lastName") @NotNull @IsName String lastName,
            @Parameter(description = "New medical record object.")
            @RequestBody @Validated({Default.class, Update.class}) MedicalRecord body
    ) {
        return toResponse(() -> medicalRecordService.updateMedicalRecordByNames(firstName, lastName, body));
    }

    @Operation(
            summary = "Deletes a medical record."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE, value = "/{personId}")
    public ResponseEntity<Void> deleteMedicalRecord(
            @Parameter(description = "ID of medical record that needs to be deleted.")
            @PathVariable("personId") @NotNull Long id
    ) {
        if (!medicalRecordService.deleteMedicalRecord(id)) {
            throw errorMedicalRecordNotFound();
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Deletes a medical record by person first and last name."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMedicalRecordByNames(
            @Parameter(description = "First name of person whose medical record needs to be deleted.")
            @RequestParam("firstName") @NotNull @IsName String firstName,
            @Parameter(description = "Last name of person whose medical record needs to be deleted.")
            @RequestParam("lastName") @NotNull @IsName String lastName
    ) {
        try {
            if (!medicalRecordService.deleteMedicalRecordByNames(firstName, lastName)) {
                throw errorMedicalRecordNotFound();
            }
            return ResponseEntity.noContent().build();
        } catch (MedicalRecordService.InterferingNamesException e) {
            throw errorInterferingNames();
        }
    }

    private ResponseEntity<Void> toResponse(UpdateFunction fct) {
        MedicalRecordService.UpdateResult res;
        try {
            res = fct.call();
        } catch (MedicalRecordService.InterferingNamesException e) {
            throw errorInterferingNames();
        } catch (MedicalRecordService.PersonNotFoundException e) {
            throw errorPersonNotFound();
        } catch (MedicalRecordService.MedicalRecordExistsException e) {
            throw errorMedicalRecordExists();
        }
        if (res == null) {
            throw errorMedicalRecordNotFound();
        }
        if (res.isCreated()) {
            return ResponseEntity.created(getLocation(res.getMedicalRecord())).build();
        } else {
            return ResponseEntity.noContent().location(getLocation(res.getMedicalRecord())).build();
        }
    }

    private interface UpdateFunction {
        MedicalRecordService.UpdateResult call() throws
                MedicalRecordService.InterferingNamesException,
                MedicalRecordService.PersonNotFoundException,
                MedicalRecordService.MedicalRecordExistsException;
    }

    /**
     * Returns the URL to a medical record.
     */
    @SneakyThrows
    private URI getLocation(MedicalRecord medicalRecord) {
        // TODO: Returns full URI instead of relative
        return new URI("/medicalRecord/" + medicalRecord.getPersonId());
    }

    /**
     * Returns a SERVICE/ALREADY_EXISTS error when a medical record already exists for a person.
     */
    private ApiException errorMedicalRecordExists() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.SERVICE)
                .status(HttpStatus.CONFLICT.value())
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
                .status(HttpStatus.NOT_FOUND.value())
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
                .status(HttpStatus.NOT_FOUND.value())
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
                .status(HttpStatus.CONFLICT.value())
                .code(ApiErrorCode.INTERFERING_NAMES)
                .message("Multiple medical records share this names combination, use ID instead")
                .build());
    }
}