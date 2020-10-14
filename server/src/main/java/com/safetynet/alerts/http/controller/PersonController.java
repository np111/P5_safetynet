package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.api.validation.constraint.IsName;
import com.safetynet.alerts.api.validation.group.Create;
import com.safetynet.alerts.api.validation.group.Update;
import com.safetynet.alerts.service.PersonService;
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

@Tag(name = "person", description = "CRUD operations about persons")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/person")
@Validated
public class PersonController {
    private final PersonService personService;

    @Operation(
            summary = "Find person by ID."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.GET, value = "/{id}")
    public Person getPerson(
            @Parameter(description = "ID of person to return.")
            @PathVariable("id") @NotNull Long id
    ) {
        Person res = personService.getPerson(id);
        if (res == null) {
            throw errorPersonNotFound();
        }
        return res;
    }

    @Operation(
            summary = "Add a new person."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createPerson(
            @Parameter(description = "Allow creation of this new person even if another person has the same first and last name combination.")
            @RequestParam(value = "allowSimilarNames", defaultValue = "false") boolean allowSimilarNames,
            @Parameter(description = "Person object that needs to be added.")
            @RequestBody @Validated({Default.class, Create.class}) Person body
    ) {
        return toResponse(() -> personService.createPerson(body, allowSimilarNames));
    }

    @Operation(
            summary = "Update an existing person."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public ResponseEntity<Void> updatePerson(
            @Parameter(description = "ID of person that needs to be updated.")
            @PathVariable("id") @NotNull Long id,
            @Parameter(description = "Allow update of this new person even if another person has the same first and last name combination.")
            @RequestParam(value = "allowSimilarNames", defaultValue = "false") boolean allowSimilarNames,
            @Parameter(description = "New person object.")
            @RequestBody @Validated({Default.class, Update.class}) Person body
    ) {
        return toResponse(() -> personService.updatePerson(id, body, allowSimilarNames));
    }

    @Operation(
            summary = "Update an existing person by first and last name."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Void> updatePersonByNames(
            @Parameter(description = "First name of person that needs to be updated.")
            @RequestParam("firstName") @NotNull @IsName String firstName,
            @Parameter(description = "Last name of person that needs to be updated.")
            @RequestParam("lastName") @NotNull @IsName String lastName,
            @Parameter(description = "New person object.")
            @RequestBody @Validated({Default.class, Update.class}) Person body
    ) {
        return toResponse(() -> personService.updatePersonByNames(firstName, lastName, body));
    }

    @Operation(
            summary = "Deletes a person."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity<Void> deletePerson(
            @Parameter(description = "ID of person that needs to be deleted.")
            @PathVariable("id") @NotNull Long id
    ) {
        if (!personService.deletePerson(id)) {
            throw errorPersonNotFound();
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Deletes a person by first and last name."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deletePersonByNames(
            @Parameter(description = "First name of person that needs to be deleted.")
            @RequestParam("firstName") @NotNull @IsName String firstName,
            @Parameter(description = "Last name of person that needs to be deleted.")
            @RequestParam("lastName") @NotNull @IsName String lastName
    ) {
        try {
            if (!personService.deletePersonByNames(firstName, lastName)) {
                throw errorPersonNotFound();
            }
            return ResponseEntity.noContent().build();
        } catch (PersonService.InterferingNamesException e) {
            throw errorInterferingNames();
        }
    }

    private ResponseEntity<Void> toResponse(UpdateFunction fct) {
        PersonService.UpdateResult res;
        try {
            res = fct.call();
        } catch (PersonService.InterferingNamesException e) {
            throw errorInterferingNames();
        } catch (PersonService.PersonExistsException e) {
            throw errorPersonExists();
        } catch (PersonService.InterferingAddressException e) {
            throw errorInterferingAddress();
        } catch (PersonService.ImmutableNamesException e) {
            throw errorImmutableNames();
        }
        if (res == null) {
            throw errorPersonNotFound();
        }
        if (res.isCreated()) {
            return ResponseEntity.created(getLocation(res.getPerson())).build();
        } else {
            return ResponseEntity.noContent().location(getLocation(res.getPerson())).build();
        }
    }

    private interface UpdateFunction {
        PersonService.UpdateResult call() throws
                PersonService.InterferingNamesException,
                PersonService.PersonExistsException,
                PersonService.InterferingAddressException,
                PersonService.ImmutableNamesException;
    }

    /**
     * Returns the URL to a person.
     */
    @SneakyThrows
    private URI getLocation(Person person) {
        // TODO: Returns full URI instead of relative
        return new URI("/person/" + person.getId());
    }

    /**
     * Returns a CLIENT/BAD_REQUEST error when firstName and lastName cannot be updated.
     */
    private ApiException errorImmutableNames() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.CLIENT)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ApiErrorCode.BAD_REQUEST)
                .message("firstName and lastName cannot be updated in this context, use ID instead")
                .build());
    }

    /**
     * Returns a SERVICE/ALREADY_EXISTS error when similar names combination are not allowed.
     */
    private ApiException errorPersonExists() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.SERVICE)
                .status(HttpStatus.CONFLICT.value())
                .code(ApiErrorCode.ALREADY_EXISTS)
                .message("A person with a similar names combination already exists")
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
                .message("Person not found")
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
                .message("Multiple persons share this names combination, use ID instead")
                .build());
    }

    /**
     * Returns a SERVICE/INTERFERING_ADDRESS error for attempts to create an invalid address (with bad city/zip
     * combination).
     */
    private ApiException errorInterferingAddress() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.SERVICE)
                .status(HttpStatus.CONFLICT.value())
                .code(ApiErrorCode.INTERFERING_ADDRESS)
                .message("A matching address already exists with a different city/zip combination")
                .build());
    }
}