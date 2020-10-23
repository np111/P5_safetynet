package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.service.MedicalRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.safetynet.alerts.http.controller.MedicalRecordController.errorInterferingNames;
import static com.safetynet.alerts.http.controller.MedicalRecordController.errorMedicalRecordExists;
import static com.safetynet.alerts.http.controller.MedicalRecordController.errorMedicalRecordNotFound;
import static com.safetynet.alerts.http.controller.MedicalRecordController.errorPersonNotFound;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.invalidMedicalRecordFirstName;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.invalidMedicalRecordId;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.invalidMedicalRecordJson;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.invalidMedicalRecordLastName;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.knownMedicalRecord;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.knownMedicalRecordJson;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.manyPersonsMedicalRecord;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.manyPersonsMedicalRecordJson;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.unknownMedicalRecord;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.unknownMedicalRecordJson;
import static com.safetynet.alerts.http.controller.MedicalRecordServiceMock.unknownPersonMedicalRecordJson;
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

@WebMvcTest(MedicalRecordController.class)
class MedicalRecordControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicalRecordService medicalRecordService;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        MedicalRecordServiceMock.init(medicalRecordService);
    }

    @Test
    void getMedicalRecordWithFailedValidation() throws Exception {
        // invalid id
        mockMvc.perform(get("/medicalRecord/" + invalidMedicalRecordId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void getMedicalRecordNotExisting() throws Exception {
        // unknown id
        mockMvc.perform(get("/medicalRecord/" + unknownMedicalRecord().getPersonId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorMedicalRecordNotFound()));
    }

    @Test
    void getMedicalRecord() throws Exception {
        // known id
        mockMvc.perform(get("/medicalRecord/" + knownMedicalRecord().getPersonId()))
                .andExpect(status().isOk())
                .andExpect(content().json(knownMedicalRecordJson(), true));
    }

    @Test
    void createMedicalRecordWithFailedValidation() throws Exception {
        // missing body
        mockMvc.perform(post("/medicalRecord"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid body
        mockMvc.perform(post("/medicalRecord")
                .contentType(APPLICATION_JSON).content(invalidMedicalRecordJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void createMedicalRecordAlreadyExists() throws Exception {
        // already exists
        mockMvc.perform(post("/medicalRecord")
                .contentType(APPLICATION_JSON).content(knownMedicalRecordJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorMedicalRecordExists()));
    }

    @Test
    void createMedicalRecordPersonNotExisting() throws Exception {
        // person not found
        mockMvc.perform(post("/medicalRecord")
                .contentType(APPLICATION_JSON).content(unknownPersonMedicalRecordJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorPersonNotFound()));
    }

    @Test
    void createMedicalRecordWithSimilarNames() throws Exception {
        // interfering names
        mockMvc.perform(post("/medicalRecord")
                .contentType(APPLICATION_JSON).content(manyPersonsMedicalRecordJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorInterferingNames()));
    }

    @Test
    void createMedicalRecord() throws Exception {
        // create
        mockMvc.perform(post("/medicalRecord")
                .contentType(APPLICATION_JSON).content(unknownMedicalRecordJson()))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/medicalRecord/" + unknownMedicalRecord().getPersonId()));
    }

    @Test
    void updateMedicalRecordWithFailedValidation() throws Exception {
        // missing body
        mockMvc.perform(put("/medicalRecord/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid body
        mockMvc.perform(put("/medicalRecord/1")
                .contentType(APPLICATION_JSON).content(invalidMedicalRecordJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void updateMedicalRecordNotExisting() throws Exception {
        // unknown
        mockMvc.perform(put("/medicalRecord/" + unknownMedicalRecord().getPersonId())
                .contentType(APPLICATION_JSON).content(unknownMedicalRecordJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorMedicalRecordNotFound()));
    }

    @Test
    void updateMedicalRecord() throws Exception {
        // known
        mockMvc.perform(put("/medicalRecord/" + knownMedicalRecord().getPersonId())
                .contentType(APPLICATION_JSON).content(knownMedicalRecordJson()))
                .andExpect(status().isNoContent())
                .andExpect(redirectedUrl("/medicalRecord/" + knownMedicalRecord().getPersonId()));
    }

    @Test
    void updateMedicalRecordByNamesWithFailedValidation() throws Exception {
        // missing names
        mockMvc.perform(put("/medicalRecord")
                .contentType(APPLICATION_JSON).content(unknownMedicalRecordJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid names
        mockMvc.perform(put("/medicalRecord")
                .queryParam("firstName", invalidMedicalRecordFirstName()).queryParam("lastName", invalidMedicalRecordLastName())
                .contentType(APPLICATION_JSON).content(unknownMedicalRecordJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void updateMedicalRecordByNamesNotExisting() throws Exception {
        // unknown
        mockMvc.perform(put("/medicalRecord")
                .queryParam("firstName", unknownMedicalRecord().getFirstName()).queryParam("lastName", unknownMedicalRecord().getLastName())
                .contentType(APPLICATION_JSON).content(unknownMedicalRecordJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorMedicalRecordNotFound()));
    }

    @Test
    void updateMedicalRecordByNamesWithSimilarNames() throws Exception {
        // interfering
        mockMvc.perform(put("/medicalRecord")
                .queryParam("firstName", manyPersonsMedicalRecord().getFirstName()).queryParam("lastName", manyPersonsMedicalRecord().getLastName())
                .contentType(APPLICATION_JSON).content(manyPersonsMedicalRecordJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorInterferingNames()));
    }

    @Test
    void updateMedicalRecordByNames() throws Exception {
        // update
        mockMvc.perform(put("/medicalRecord")
                .queryParam("firstName", knownMedicalRecord().getFirstName()).queryParam("lastName", knownMedicalRecord().getLastName())
                .contentType(APPLICATION_JSON).content(knownMedicalRecordJson()))
                .andExpect(status().isNoContent())
                .andExpect(redirectedUrl("/medicalRecord/" + knownMedicalRecord().getPersonId()));
    }

    @Test
    void deleteMedicalRecordWithFailedValidation() throws Exception {
        // invalid id
        mockMvc.perform(delete("/medicalRecord/" + invalidMedicalRecordId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void deleteMedicalRecordNotExisting() throws Exception {
        // unknown
        mockMvc.perform(delete("/medicalRecord/" + unknownMedicalRecord().getPersonId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorMedicalRecordNotFound()));
    }

    @Test
    void deleteMedicalRecord() throws Exception {
        // delete
        mockMvc.perform(delete("/medicalRecord/" + knownMedicalRecord().getPersonId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMedicalRecordByNamesWithFailedValidation() throws Exception {
        // missing names
        mockMvc.perform(delete("/medicalRecord")
                .contentType(APPLICATION_JSON).content(unknownMedicalRecordJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid names
        mockMvc.perform(delete("/medicalRecord")
                .queryParam("firstName", invalidMedicalRecordFirstName()).queryParam("lastName", invalidMedicalRecordLastName()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void deleteMedicalRecordByNamesNotExisting() throws Exception {
        // unknown
        mockMvc.perform(delete("/medicalRecord")
                .queryParam("firstName", unknownMedicalRecord().getFirstName()).queryParam("lastName", unknownMedicalRecord().getLastName()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorMedicalRecordNotFound()));
    }

    @Test
    void deleteMedicalRecordByNamesWithSimilarNames() throws Exception {
        // interfering
        mockMvc.perform(delete("/medicalRecord")
                .queryParam("firstName", manyPersonsMedicalRecord().getFirstName()).queryParam("lastName", manyPersonsMedicalRecord().getLastName()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorInterferingNames()));
    }

    @Test
    void deleteMedicalRecordByNames() throws Exception {
        // delete
        mockMvc.perform(delete("/medicalRecord")
                .queryParam("firstName", knownMedicalRecord().getFirstName()).queryParam("lastName", knownMedicalRecord().getLastName()))
                .andExpect(status().isNoContent());
    }
}