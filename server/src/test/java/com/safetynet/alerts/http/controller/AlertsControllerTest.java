package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.service.AlertsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.safetynet.alerts.http.controller.AlertsServiceMock.emptyChildAlertResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.emptyCommunityEmailResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.emptyFireResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.emptyFloodStationsResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.emptyPersonInfoResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.emptyPersonsCoveredByFirestationResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.emptyPhoneAlertResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.filledChildAlertResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.filledCommunityEmailResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.filledFireResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.filledFloodStationsResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.filledPersonInfoResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.filledPersonsCoveredByFirestationResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.filledPhoneAlertResponseJson;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.invalidAddress;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.invalidCity;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.invalidStationNumber;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.knownAddress;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.knownCity;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.knownStationNumber;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.knownStationNumberList;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.unknownAddress;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.unknownCity;
import static com.safetynet.alerts.http.controller.AlertsServiceMock.unknownStationNumber;
import static com.safetynet.alerts.http.controller.PersonServiceMock.invalidPersonFirstName;
import static com.safetynet.alerts.http.controller.PersonServiceMock.invalidPersonLastName;
import static com.safetynet.alerts.http.controller.PersonServiceMock.knownPerson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.knownPersonJson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.unknownPerson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.unknownPersonJson;
import static com.safetynet.alerts.util.ApiErrorCode.VALIDATION_FAILED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertsController.class)
class AlertsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertsService alertsService;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        AlertsServiceMock.init(alertsService);
    }

    @Test
    void getPersonsCoveredByFirestation() throws Exception {
        // invalid number
        mockMvc.perform(get("/firestation").queryParam("stationNumber", invalidStationNumber()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // unknown number
        mockMvc.perform(get("/firestation").queryParam("stationNumber", unknownStationNumber()))
                .andExpect(status().isOk())
                .andExpect(content().json(emptyPersonsCoveredByFirestationResponseJson()));

        // known number
        mockMvc.perform(get("/firestation").queryParam("stationNumber", knownStationNumber()))
                .andExpect(status().isOk())
                .andExpect(content().json(filledPersonsCoveredByFirestationResponseJson()));
    }

    @Test
    void getChildAlert() throws Exception {
        // invalid address
        mockMvc.perform(get("/childAlert").queryParam("address", invalidAddress()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // unknown address
        mockMvc.perform(get("/childAlert").queryParam("address", unknownAddress()))
                .andExpect(status().isOk())
                .andExpect(content().json(emptyChildAlertResponseJson()));

        // known address
        mockMvc.perform(get("/childAlert").queryParam("address", knownAddress()))
                .andExpect(status().isOk())
                .andExpect(content().json(filledChildAlertResponseJson()));
    }

    @Test
    void getPhoneAlert() throws Exception {
        // invalid number
        mockMvc.perform(get("/phoneAlert").queryParam("firestation", invalidStationNumber()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // unknown number
        mockMvc.perform(get("/phoneAlert").queryParam("firestation", unknownStationNumber()))
                .andExpect(status().isOk())
                .andExpect(content().json(emptyPhoneAlertResponseJson()));

        // known number
        mockMvc.perform(get("/phoneAlert").queryParam("firestation", knownStationNumber()))
                .andExpect(status().isOk())
                .andExpect(content().json(filledPhoneAlertResponseJson()));
    }

    @Test
    void getFire() throws Exception {
        // invalid address
        mockMvc.perform(get("/fire").queryParam("address", invalidAddress()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // unknown address
        mockMvc.perform(get("/fire").queryParam("address", unknownAddress()))
                .andExpect(status().isOk())
                .andExpect(content().json(emptyFireResponseJson()));

        // known address
        mockMvc.perform(get("/fire").queryParam("address", knownAddress()))
                .andExpect(status().isOk())
                .andExpect(content().json(filledFireResponseJson()));
    }

    @Test
    void getFloodStations() throws Exception {
        // invalid number
        mockMvc.perform(get("/flood/stations").queryParam("stations", invalidStationNumber()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // unknown number
        mockMvc.perform(get("/flood/stations").queryParam("stations", unknownStationNumber()))
                .andExpect(status().isOk())
                .andExpect(content().json(emptyFloodStationsResponseJson()));

        // known number
        mockMvc.perform(get("/flood/stations").queryParam("stations", knownStationNumberList()))
                .andExpect(status().isOk())
                .andExpect(content().json(filledFloodStationsResponseJson()));
    }

    @Test
    void getPersonInfo() throws Exception {
        // invalid names
        mockMvc.perform(get("/personInfo").queryParam("firstName", invalidPersonFirstName()).queryParam("lastName", invalidPersonLastName()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // unknown names
        mockMvc.perform(get("/personInfo").queryParam("firstName", unknownPerson().getFirstName()).queryParam("lastName", unknownPerson().getLastName()))
                .andExpect(status().isOk())
                .andExpect(content().json(emptyPersonInfoResponseJson()));

        // known names
        mockMvc.perform(get("/personInfo").queryParam("firstName", knownPerson().getFirstName()).queryParam("lastName", knownPerson().getLastName()))
                .andExpect(status().isOk())
                .andExpect(content().json(filledPersonInfoResponseJson()));
    }

    @Test
    void getCommunityEmail() throws Exception {
        // invalid city
        mockMvc.perform(get("/communityEmail").queryParam("city", invalidCity()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // unknown city
        mockMvc.perform(get("/communityEmail").queryParam("city", unknownCity()))
                .andExpect(status().isOk())
                .andExpect(content().json(emptyCommunityEmailResponseJson()));

        // known city
        mockMvc.perform(get("/communityEmail").queryParam("city", knownCity()))
                .andExpect(status().isOk())
                .andExpect(content().json(filledCommunityEmailResponseJson()));
    }
}