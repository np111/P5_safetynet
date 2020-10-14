package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.api.response.ChildAlertResponse;
import com.safetynet.alerts.api.response.CommunityEmailResponse;
import com.safetynet.alerts.api.response.FireResponse;
import com.safetynet.alerts.api.response.FloodStationsResponse;
import com.safetynet.alerts.api.response.PersonInfoResponse;
import com.safetynet.alerts.api.response.PersonsCoveredByFirestationResponse;
import com.safetynet.alerts.api.response.PhoneAlertResponse;
import com.safetynet.alerts.api.validation.constraint.IsAddress;
import com.safetynet.alerts.api.validation.constraint.IsCity;
import com.safetynet.alerts.api.validation.constraint.IsName;
import com.safetynet.alerts.api.validation.constraint.IsStationNumber;
import com.safetynet.alerts.service.AlertsService;
import com.safetynet.alerts.util.spring.JsonRequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "alerts", description = "Alerts operations")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/")
@Validated
public class AlertsController {
    private final AlertsService alertsService;

    @Operation(
            summary = "Returns the list of persons covered by a firestation."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/firestation")
    public PersonsCoveredByFirestationResponse getPersonsCoveredByFirestation(
            @RequestParam("stationNumber") @NotNull @IsStationNumber String stationNumber
    ) {
        return alertsService.getPersonsCoveredByFirestation(stationNumber);
    }

    @Operation(
            summary = "Returns the list of persons living at an address."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/childAlert")
    public ChildAlertResponse getChildAlert(
            @RequestParam("address") @NotNull @IsAddress String address
    ) {
        return alertsService.getChildAlert(address);
    }

    @Operation(
            summary = "Returns the list of phones of persons covered by a firestation."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/phoneAlert")
    public PhoneAlertResponse getPhoneAlert(
            @RequestParam("firestation") @NotNull @IsStationNumber String stationNumber
    ) {
        return alertsService.getPhoneAlert(stationNumber);
    }

    @Operation(
            summary = "Returns the list of persons living at an address and the firestation covering them."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/fire")
    public FireResponse getFire(
            @RequestParam("address") @NotNull @IsAddress String address
    ) {
        return alertsService.getFire(address);
    }

    @Operation(
            summary = "Returns the list of persons covered by the given firestation (grouped by address)."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/flood/stations")
    public FloodStationsResponse getFloodStations(
            @RequestParam("stations") @NotEmpty List<@NotNull @IsStationNumber String> stations
    ) {
        return alertsService.getFloodStations(stations);
    }

    @Operation(
            summary = "Returns the list of persons with a given first name and last name."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/personInfo")
    public PersonInfoResponse getPersonInfo(
            @RequestParam("firstName") @NotNull @IsName String firstName,
            @RequestParam("lastName") @NotNull @IsName String lastName
    ) {
        return alertsService.getPersonInfo(firstName, lastName);
    }

    @Operation(
            summary = "Returns the list of emails of persons living in a city."
    )
    @JsonRequestMapping(method = RequestMethod.GET, value = "/communityEmail")
    public CommunityEmailResponse getCommunityEmail(
            @RequestParam("city") @NotNull @IsCity String city
    ) {
        return alertsService.getCommunityEmail(city);
    }
}
