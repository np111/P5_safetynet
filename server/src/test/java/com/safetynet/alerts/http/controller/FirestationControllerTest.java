package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.service.FirestationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.safetynet.alerts.http.controller.FirestationController.errorFirestationNotFound;
import static com.safetynet.alerts.http.controller.FirestationController.errorImmutableAddress;
import static com.safetynet.alerts.http.controller.FirestationServiceMock.invalidFirestationAddress;
import static com.safetynet.alerts.http.controller.FirestationServiceMock.invalidFirestationJson;
import static com.safetynet.alerts.http.controller.FirestationServiceMock.knownFirestation;
import static com.safetynet.alerts.http.controller.FirestationServiceMock.knownFirestationJson;
import static com.safetynet.alerts.http.controller.FirestationServiceMock.unknownFirestation;
import static com.safetynet.alerts.http.controller.FirestationServiceMock.unknownFirestationJson;
import static com.safetynet.alerts.util.ApiErrorCode.VALIDATION_FAILED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FirestationController.class)
class FirestationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FirestationService firestationService;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        FirestationServiceMock.init(firestationService);
    }

    @Test
    void getFirestationWithFailedValidation() throws Exception {
        // missing address
        mockMvc.perform(get("/firestation/get"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid address
        mockMvc.perform(get("/firestation/get").queryParam("address", invalidFirestationAddress()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void getFirestationNotExisting() throws Exception {
        // unknown address
        mockMvc.perform(get("/firestation/get").queryParam("address", unknownFirestation().getAddress()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorFirestationNotFound()));
    }

    @Test
    void getFirestation() throws Exception {
        // known address
        mockMvc.perform(get("/firestation/get").queryParam("address", knownFirestation().getAddress()))
                .andExpect(status().isOk())
                .andExpect(content().json(knownFirestationJson(), true));
    }

    @Test
    void createFirestationWithFailedValidation() throws Exception {
        // missing body
        mockMvc.perform(post("/firestation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid body
        mockMvc.perform(post("/firestation")
                .contentType(APPLICATION_JSON).content(invalidFirestationJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void createFirestation() throws Exception {
        // create
        mockMvc.perform(post("/firestation")
                .contentType(APPLICATION_JSON).content(unknownFirestationJson()))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/firestation/get?address=" + unknownFirestation().getAddress()));

        // update
        mockMvc.perform(post("/firestation")
                .contentType(APPLICATION_JSON).content(knownFirestationJson()))
                .andExpect(status().isNoContent())
                .andExpect(redirectedUrl("/firestation/get?address=" + knownFirestation().getAddress()));
    }

    @Test
    void updateFirestationWithFailedValidation() throws Exception {
        // missing body
        mockMvc.perform(put("/firestation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid body
        mockMvc.perform(put("/firestation")
                .contentType(APPLICATION_JSON).content(invalidFirestationJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void updateFirestationImmutableAddress() throws Exception {
        // immutable address
        mockMvc.perform(put("/firestation").queryParam("address", knownFirestation().getAddress())
                .contentType(APPLICATION_JSON).content(unknownFirestationJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(errorImmutableAddress()));
    }

    @Test
    void updateFirestation() throws Exception {
        // create
        mockMvc.perform(put("/firestation").queryParam("address", unknownFirestation().getAddress())
                .contentType(APPLICATION_JSON).content(unknownFirestationJson()))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/firestation/get?address=" + unknownFirestation().getAddress()));

        // update
        mockMvc.perform(put("/firestation").queryParam("address", knownFirestation().getAddress())
                .contentType(APPLICATION_JSON).content(knownFirestationJson()))
                .andExpect(status().isNoContent())
                .andExpect(redirectedUrl("/firestation/get?address=" + knownFirestation().getAddress()));
    }

    @Test
    void deleteFirestationWithFailedValidation() throws Exception {
        // missing address
        mockMvc.perform(delete("/firestation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid address
        mockMvc.perform(delete("/firestation").queryParam("address", invalidFirestationAddress()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void deleteFirestationNotExisting() throws Exception {
        // unknown address
        mockMvc.perform(delete("/firestation").queryParam("address", unknownFirestation().getAddress()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorFirestationNotFound()));
    }

    @Test
    void deleteFirestation() throws Exception {
        // known address
        mockMvc.perform(delete("/firestation").queryParam("address", knownFirestation().getAddress()))
                .andExpect(status().isNoContent());
    }
}