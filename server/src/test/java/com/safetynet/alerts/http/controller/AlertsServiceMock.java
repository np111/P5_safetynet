package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.api.model.Person;
import com.safetynet.alerts.api.response.ChildAlertResponse;
import com.safetynet.alerts.api.response.CommunityEmailResponse;
import com.safetynet.alerts.api.response.FireResponse;
import com.safetynet.alerts.api.response.FloodStationsResponse;
import com.safetynet.alerts.api.response.PersonInfoResponse;
import com.safetynet.alerts.api.response.PersonsCoveredByFirestationResponse;
import com.safetynet.alerts.api.response.PhoneAlertResponse;
import com.safetynet.alerts.service.AlertsService;
import java.util.Arrays;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import static com.safetynet.alerts.http.controller.PersonServiceMock.knownPerson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@UtilityClass
public class AlertsServiceMock {
    public static void init(AlertsService alertsService) {
        // getPersonsCoveredByFirestation
        // - unknown (empty)
        when(alertsService.getPersonsCoveredByFirestation(any()))
                .thenReturn(emptyPersonsCoveredByFirestationResponse());

        // - known (filled)
        when(alertsService.getPersonsCoveredByFirestation(knownStationNumber()))
                .thenReturn(filledPersonsCoveredByFirestationResponse());

        // getChildAlert
        // - unknown (empty)
        when(alertsService.getChildAlert(any()))
                .thenReturn(emptyChildAlertResponse());

        // - known (filled)
        when(alertsService.getChildAlert(knownAddress()))
                .thenReturn(filledChildAlertResponse());

        // getPhoneAlert
        // - unknown (empty)
        when(alertsService.getPhoneAlert(any()))
                .thenReturn(emptyPhoneAlertResponse());

        // - known (filled)
        when(alertsService.getPhoneAlert(knownStationNumber()))
                .thenReturn(filledPhoneAlertResponse());

        // getFire
        // - unknown (empty)
        when(alertsService.getFire(any()))
                .thenReturn(emptyFireResponse());

        // - known (filled)
        when(alertsService.getFire(knownAddress()))
                .thenReturn(filledFireResponse());

        // getFloodStations
        // - unknown (empty)
        when(alertsService.getFloodStations(any()))
                .thenReturn(emptyFloodStationsResponse());

        // - known (filled)
        when(alertsService.getFloodStations(Arrays.asList(knownStationNumberList().split(","))))
                .thenReturn(filledFloodStationsResponse());

        // getPersonInfo
        // - unknown (empty)
        when(alertsService.getPersonInfo(any(), any()))
                .thenReturn(emptyPersonInfoResponse());

        // - known (filled)
        when(alertsService.getPersonInfo(knownPerson().getFirstName(), knownPerson().getLastName()))
                .thenReturn(filledPersonInfoResponse());

        // getCommunityEmail
        // - unknown (empty)
        when(alertsService.getCommunityEmail(any()))
                .thenReturn(emptyCommunityEmailResponse());

        // - known (filled)
        when(alertsService.getCommunityEmail(knownCity()))
                .thenReturn(filledCommunityEmailResponse());
    }

    public static String knownStationNumber() {
        return "A1";
    }

    public static String knownStationNumberList() {
        return "A1,A2";
    }

    public static String unknownStationNumber() {
        return "B5";
    }

    public static String invalidStationNumber() {
        return StringUtils.repeat('a', 256);
    }

    public static String knownAddress() {
        return "1509 Culver St";
    }

    public static String unknownAddress() {
        return "1509 XXX";
    }

    public static String invalidAddress() {
        return StringUtils.repeat('a', 256);
    }

    public static String knownCity() {
        return "Paris";
    }

    public static String unknownCity() {
        return "Not Paris";
    }

    public static String invalidCity() {
        return StringUtils.repeat('a', 256);
    }

    public static PersonsCoveredByFirestationResponse filledPersonsCoveredByFirestationResponse() {
        return PersonsCoveredByFirestationResponse.builder()
                .childrenCount(1)
                .adultsCount(2)
                .person(Person.builder().id(1L).build())
                .person(Person.builder().id(2L).build())
                .build();
    }

    public static String filledPersonsCoveredByFirestationResponseJson() {
        return "{\"childrenCount\":1,\"adultsCount\":2,\"persons\":[{\"id\":1},{\"id\":2}]}";
    }

    public static PersonsCoveredByFirestationResponse emptyPersonsCoveredByFirestationResponse() {
        return PersonsCoveredByFirestationResponse.builder()
                .childrenCount(0)
                .adultsCount(0)
                .build();
    }

    public static String emptyPersonsCoveredByFirestationResponseJson() {
        return "{\"childrenCount\":0,\"adultsCount\":0,\"persons\":[]}";
    }

    public static ChildAlertResponse filledChildAlertResponse() {
        return ChildAlertResponse.builder()
                .children(Person.builder().id(1L).build())
                .children(Person.builder().id(2L).build())
                .adult(Person.builder().id(3L).build())
                .adult(Person.builder().id(4L).build())
                .build();
    }

    public static String filledChildAlertResponseJson() {
        return "{\"children\":[{\"id\":1},{\"id\":2}],\"adults\":[{\"id\":3},{\"id\":4}]}";
    }

    public static ChildAlertResponse emptyChildAlertResponse() {
        return ChildAlertResponse.builder().build();
    }

    public static String emptyChildAlertResponseJson() {
        return "{\"children\":[],\"adults\":[]}";
    }

    public static PhoneAlertResponse filledPhoneAlertResponse() {
        return PhoneAlertResponse.builder()
                .phone("123-456-7890")
                .phone("098-765-4321")
                .build();
    }

    public static String filledPhoneAlertResponseJson() {
        return "{\"phones\":[\"123-456-7890\",\"098-765-4321\"]}";
    }

    public static PhoneAlertResponse emptyPhoneAlertResponse() {
        return PhoneAlertResponse.builder().build();
    }

    public static String emptyPhoneAlertResponseJson() {
        return "{\"phones\":[]}";
    }

    public static FireResponse filledFireResponse() {
        return FireResponse.builder()
                .person(Person.builder().id(1L).build())
                .person(Person.builder().id(2L).build())
                .build();
    }

    public static String filledFireResponseJson() {
        return "{\"persons\":[{\"id\":1},{\"id\":2}]}";
    }

    public static FireResponse emptyFireResponse() {
        return FireResponse.builder().build();
    }

    public static String emptyFireResponseJson() {
        return "{\"persons\":[]}";
    }

    public static FloodStationsResponse filledFloodStationsResponse() {
        return FloodStationsResponse.builder()
                .station(FloodStationsResponse.Entry.builder().address("A1").build())
                .station(FloodStationsResponse.Entry.builder().address("A2").build())
                .build();
    }

    public static String filledFloodStationsResponseJson() {
        return "{\"stations\":[{\"address\":\"A1\",\"persons\":[]},{\"address\":\"A2\",\"persons\":[]}]}";
    }

    public static FloodStationsResponse emptyFloodStationsResponse() {
        return FloodStationsResponse.builder().build();
    }

    public static String emptyFloodStationsResponseJson() {
        return "{\"stations\":[]}";
    }

    public static PersonInfoResponse filledPersonInfoResponse() {
        return PersonInfoResponse.builder()
                .person(Person.builder().id(1L).build())
                .person(Person.builder().id(2L).build())
                .build();
    }

    public static String filledPersonInfoResponseJson() {
        return "{\"persons\":[{\"id\":1},{\"id\":2}]}";
    }

    public static PersonInfoResponse emptyPersonInfoResponse() {
        return PersonInfoResponse.builder().build();
    }

    public static String emptyPersonInfoResponseJson() {
        return "{\"persons\":[]}";
    }

    public static CommunityEmailResponse filledCommunityEmailResponse() {
        return CommunityEmailResponse.builder()
                .email("123@domain.tld")
                .email("456@mail.com")
                .build();
    }

    public static String filledCommunityEmailResponseJson() {
        return "{\"emails\":[\"123@domain.tld\",\"456@mail.com\"]}";
    }

    public static CommunityEmailResponse emptyCommunityEmailResponse() {
        return CommunityEmailResponse.builder().build();
    }

    public static String emptyCommunityEmailResponseJson() {
        return "{\"emails\":[]}";
    }
}
