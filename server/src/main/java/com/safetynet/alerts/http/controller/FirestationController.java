package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.api.model.ApiError;
import com.safetynet.alerts.api.model.Firestation;
import com.safetynet.alerts.api.validation.constraint.IsAddress;
import com.safetynet.alerts.api.validation.group.Create;
import com.safetynet.alerts.api.validation.group.Update;
import com.safetynet.alerts.service.FirestationService;
import com.safetynet.alerts.util.ApiErrorCode;
import com.safetynet.alerts.util.ApiException;
import com.safetynet.alerts.util.UriUtil;
import com.safetynet.alerts.util.spring.JsonRequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import static com.safetynet.alerts.http.controller.ExceptionController.errorToResponse;

@Tag(name = "firestation", description = "CRUD operations about firestations")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/firestation")
@Validated
public class FirestationController {
    private final FirestationService firestationService;

    @Operation(
            summary = "Find firestation by address."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.GET, value = "/get")
    public Firestation getFirestation(
            @Parameter(description = "Address of firestation to return.")
            @RequestParam("address") @NotNull @IsAddress String address
    ) {
        Firestation res = firestationService.getFirestation(address);
        if (res == null) {
            throw new ApiException(errorFirestationNotFound());
        }
        return res;
    }

    @Operation(
            summary = "Add a new firestation."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createFirestation(
            @Parameter(description = "Firestation object that needs to be added.")
            @RequestBody @Validated({Default.class, Create.class}) Firestation body
    ) {
        return toResponse(firestationService.createFirestation(body));
    }

    @Operation(
            summary = "Add or update a firestation."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Void> updateFirestation(
            @Parameter(description = "Address of firestation that needs to be updated.")
            @RequestParam("address") @NotNull @IsAddress String address,
            @Parameter(description = "New firestation object.")
            @RequestBody @Validated({Default.class, Update.class}) Firestation body
    ) {
        return toResponse(firestationService.updateFirestation(address, body));
    }

    @Operation(
            summary = "Deletes a firestation."
    )
    // TODO: Add errors documentation
    @JsonRequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteFirestation(
            @Parameter(description = "Address of firestation that needs to be deleted.")
            @RequestParam("address") @NotNull @IsAddress String address
    ) {
        if (!firestationService.deleteFirestation(address)) {
            throw new ApiException(errorFirestationNotFound());
        }
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(FirestationService.ImmutableAddressException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleImmutableAddressException() {
        return errorToResponse(errorImmutableAddress());
    }

    private ResponseEntity<Void> toResponse(FirestationService.UpdateResult res) {
        if (res.isCreated()) {
            return ResponseEntity.created(getLocation(res.getFirestation())).build();
        } else {
            return ResponseEntity.noContent().location(getLocation(res.getFirestation())).build();
        }
    }

    /**
     * Returns the URL to a firestation.
     */
    private URI getLocation(Firestation firestation) {
        return UriUtil.createUri("/firestation/get?address="
                + UriUtils.encodeQueryParam(firestation.getAddress(), StandardCharsets.UTF_8));
    }

    /**
     * Returns a SERVICE/NOT_FOUND error when a firestation does not exists.
     */
    static ApiError errorFirestationNotFound() {
        return ApiError.builder()
                .type(ApiError.ErrorType.SERVICE)
                .status(HttpStatus.NOT_FOUND.value())
                .code(ApiErrorCode.NOT_FOUND)
                .message("address not found")
                .build();
    }

    /**
     * Returns a CLIENT/BAD_REQUEST error when an address cannot be updated.
     */
    static ApiError errorImmutableAddress() {
        return ApiError.builder()
                .type(ApiError.ErrorType.CLIENT)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ApiErrorCode.BAD_REQUEST)
                .message("address cannot be updated")
                .build();
    }
}
