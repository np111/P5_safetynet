package com.safetynet.alerts.controller;

import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.api.validation.constraint.IsName;
import com.safetynet.alerts.api.validation.group.Create;
import com.safetynet.alerts.api.validation.group.Update;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.PersonRepository;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.repository.entity.PersonEntity;
import com.safetynet.alerts.util.ApiErrorCode;
import com.safetynet.alerts.util.ApiException;
import com.safetynet.alerts.util.spring.JsonRequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.Objects;
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

@Tag(name = "person", description = "CRUD operations about persons")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/person")
@Validated
public class PersonController {
    private final PersonRepository personRepository;
    private final AddressRepository addressRepository;

    @Operation(
            summary = "Find person by ID."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.GET, value = "/{id}")
    @Transactional
    public Person getPerson(
            @Parameter(description = "ID of person to return.")
            @PathVariable("id") @NotNull Long id
    ) {
        PersonEntity personEntity = personRepository.findById(id).orElse(null);
        if (personEntity == null) {
            throw errorPersonNotFound();
        }
        return personEntity.toPerson();
    }

    @Operation(
            summary = "Add a new person."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Void> createPerson(
            @Parameter(description = "Allow creation of this new person even if another person has the same first and last name combination.")
            @RequestParam(value = "allowSimilarNames", defaultValue = "false") boolean allowSimilarNames,
            @Parameter(description = "Person object that needs to be added.")
            @RequestBody @Validated({Default.class, Create.class}) Person body
    ) {
        body.setId(null);
        return update(null, body, allowSimilarNames, true);
    }

    @Operation(
            summary = "Update an existing person."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT, value = "/{id}")
    @Transactional
    public ResponseEntity<Void> updatePerson(
            @Parameter(description = "ID of person that needs to be updated.")
            @PathVariable("id") @NotNull Long id,
            @Parameter(description = "Allow update of this new person even if another person has the same first and last name combination.")
            @RequestParam(value = "allowSimilarNames", defaultValue = "false") boolean allowSimilarNames,
            @Parameter(description = "New person object.")
            @RequestBody @Validated({Default.class, Update.class}) Person body
    ) {
        body.setId(id);
        PersonEntity personEntity = personRepository.findById(id).orElse(null);
        if (personEntity == null) {
            throw errorPersonNotFound();
        }
        return update(personEntity, body, allowSimilarNames, true);
    }

    @Operation(
            summary = "Update an existing person by first and last name."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT)
    @Transactional
    public ResponseEntity<Void> updatePersonByNames(
            @Parameter(description = "First name of person that needs to be updated.")
            @RequestParam("firstName") @NotNull @IsName String firstName,
            @Parameter(description = "Last name of person that needs to be updated.")
            @RequestParam("lastName") @NotNull @IsName String lastName,
            @Parameter(description = "New person object.")
            @RequestBody @Validated({Default.class, Update.class}) Person body
    ) {
        ResponseEntity<Void> res = null;
        for (PersonEntity personEntity : personRepository.findAllByFirstNameAndLastName(firstName, lastName)) {
            if (res != null) {
                throw errorInterferingNames();
            }
            res = update(personEntity, body, false, false);
        }
        if (res == null) {
            throw errorPersonNotFound();
        }
        return res;
    }

    @Operation(
            summary = "Deletes a person."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @Transactional
    public ResponseEntity<Void> deletePerson(
            @Parameter(description = "ID of person that needs to be deleted.")
            @PathVariable("id") @NotNull Long id
    ) {
        if (personRepository.removeById(id) == 0) {
            throw errorPersonNotFound();
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Deletes a person by first and last name."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE)
    @Transactional
    public ResponseEntity<Void> deletePersonByNames(
            @Parameter(description = "First name of person that needs to be updated.")
            @RequestParam("firstName") @NotNull @IsName String firstName,
            @Parameter(description = "Last name of person that needs to be updated.")
            @RequestParam("lastName") @NotNull @IsName String lastName
    ) {
        long count = personRepository.removeByFirstNameAndLastName(firstName, lastName);
        if (count == 0) {
            throw errorPersonNotFound();
        }
        if (count > 1) {
            throw errorInterferingNames();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Create or update the person entity from it's model.
     *
     * @param entity            the existing entity; or {@code null} to create one
     * @param body              the person model to apply
     * @param allowSimilarNames whether or not similar names combination are allowed
     * @param allowUpdateNames  whether or not names updates are allowed
     * @return the REST response
     */
    private ResponseEntity<Void> update(PersonEntity entity, Person body, boolean allowSimilarNames,
            boolean allowUpdateNames) {
        // prevent similar names combination if it was not allowed
        boolean create = (entity == null);
        if (create && !allowSimilarNames && personRepository
                .existsByFirstNameAndLastName(body.getFirstName(), body.getLastName())) {
            throw errorPersonExists();
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
            throw errorInterferingAddress();
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
            throw errorImmutableNames();
        }
        entity.setAddress(addressEntity);
        entity.setPhone(body.getPhone());
        entity.setEmail(body.getEmail());
        personRepository.save(entity);

        // returns rest response
        if (create) {
            return ResponseEntity.created(getLocation(entity)).build();
        } else {
            return ResponseEntity.noContent().location(getLocation(entity)).build();
        }
    }

    /**
     * Returns the URL to a person.
     */
    @SneakyThrows
    private URI getLocation(PersonEntity personEntity) {
        // TODO: Returns full URI instead of relative
        return new URI("/person/" + personEntity.getId());
    }

    /**
     * Returns a CLIENT/BAD_REQUEST error when firstName and lastName cannot be updated.
     */
    private ApiException errorImmutableNames() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.CLIENT)
                .status(401)
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
                .status(409)
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
                .status(404)
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
                .status(409)
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
                .status(409)
                .code(ApiErrorCode.INTERFERING_ADDRESS)
                .message("A matching address already exists with a different city/zip combination")
                .build());
    }
}
