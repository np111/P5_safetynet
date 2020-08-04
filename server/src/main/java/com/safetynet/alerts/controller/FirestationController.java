package com.safetynet.alerts.controller;

import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.api.model.Firestation;
import com.safetynet.alerts.api.validation.constraint.IsAddress;
import com.safetynet.alerts.api.validation.group.Create;
import com.safetynet.alerts.api.validation.group.Update;
import com.safetynet.alerts.repository.AddressRepository;
import com.safetynet.alerts.repository.entity.AddressEntity;
import com.safetynet.alerts.util.ApiErrorCode;
import com.safetynet.alerts.util.ApiException;
import com.safetynet.alerts.util.spring.JsonRequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

@Tag(name = "firestation", description = "CRUD operations about firestations")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/firestation")
@Validated
public class FirestationController {
    private final AddressRepository addressRepository;

    @Operation(
            summary = "Find firestation by address."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.GET, value = "/get")
    @Transactional
    public Firestation getFirestation(
            @Parameter(description = "Address of firestation to return.")
            @RequestParam("address") @NotNull @IsAddress String address
    ) {
        AddressEntity addressEntity = addressRepository.findByAddress(address).orElse(null);
        if (addressEntity == null || addressEntity.getFirestation() == null) {
            throw errorFirestationNotFound();
        }
        return addressEntity.toFirestation();
    }

    @Operation(
            summary = "Add a new firestation."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Void> createFirestation(
            @Parameter(description = "Firestation object that needs to be added.")
            @RequestBody @Validated({Default.class, Create.class}) Firestation body
    ) {
        AddressEntity addressEntity = addressRepository.findByAddress(body.getAddress()).orElse(null);
        return update(addressEntity, body);
    }

    @Operation(
            summary = "Add or update a firestation."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT)
    @Transactional
    public ResponseEntity<Void> updateFirestation(
            @Parameter(description = "Address of firestation that needs to be updated.")
            @RequestParam("address") @NotNull @IsAddress String address,
            @Parameter(description = "New firestation object.")
            @RequestBody @Validated({Default.class, Update.class}) Firestation body
    ) {
        AddressEntity addressEntity = addressRepository.findByAddress(address).orElse(null);
        return update(addressEntity, body);
    }

    @Operation(
            summary = "Deletes a firestation."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE)
    @Transactional
    public ResponseEntity<Void> deleteFirestation(
            @Parameter(description = "Address of firestation that needs to be deleted.")
            @RequestParam("address") @NotNull @IsAddress String address
    ) {
        AddressEntity addressEntity = addressRepository.findByAddress(address).orElse(null);
        if (addressEntity == null || addressEntity.getFirestation() == null) {
            throw errorFirestationNotFound();
        }
        addressEntity.setFirestation(null);
        addressRepository.save(addressEntity);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create or update the firestation entity from it's model.
     *
     * @param entity the existing entity; or {@code null} to create one
     * @param body   the firestation model to apply
     * @return the REST response
     */
    private ResponseEntity<Void> update(AddressEntity entity, Firestation body) {
        boolean create = (entity == null);

        // create or update the address record
        if (create) {
            entity = new AddressEntity();
            entity.setAddress(body.getAddress());
        }
        if (!Objects.equals(body.getAddress(), entity.getAddress())) {
            throw errorImmutableAddress();
        }
        entity.setFirestation(body.getStation());
        addressRepository.save(entity);

        // returns rest response
        if (create) {
            return ResponseEntity.created(getLocation(entity)).build();
        } else {
            return ResponseEntity.noContent().location(getLocation(entity)).build();
        }
    }

    /**
     * Returns the URL to a firestation.
     */
    @SneakyThrows
    private URI getLocation(AddressEntity addressEntity) {
        // TODO: Returns full URI instead of relative
        return new URI("/firestation/get?address="
                + UriUtils.encodeQueryParam(addressEntity.getAddress(), StandardCharsets.UTF_8));
    }

    /**
     * Returns a CLIENT/BAD_REQUEST error when an address cannot be updated.
     */
    private ApiException errorImmutableAddress() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.CLIENT)
                .status(401)
                .code(ApiErrorCode.BAD_REQUEST)
                .message("address cannot be updated")
                .build());
    }

    /**
     * Returns a SERVICE/NOT_FOUND error when a firestation does not exists.
     */
    private ApiException errorFirestationNotFound() {
        return new ApiException(ApiError.builder()
                .type(ApiError.ErrorType.SERVICE)
                .status(404)
                .code(ApiErrorCode.NOT_FOUND)
                .message("address not found")
                .build());
    }
}
