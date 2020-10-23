package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.safetynet.alerts.http.controller.PersonController.errorImmutableNames;
import static com.safetynet.alerts.http.controller.PersonController.errorInterferingAddress;
import static com.safetynet.alerts.http.controller.PersonController.errorInterferingNames;
import static com.safetynet.alerts.http.controller.PersonController.errorPersonExists;
import static com.safetynet.alerts.http.controller.PersonController.errorPersonNotFound;
import static com.safetynet.alerts.http.controller.PersonServiceMock.interferingAddressPerson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.interferingAddressPersonJson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.invalidPersonFirstName;
import static com.safetynet.alerts.http.controller.PersonServiceMock.invalidPersonId;
import static com.safetynet.alerts.http.controller.PersonServiceMock.invalidPersonJson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.invalidPersonLastName;
import static com.safetynet.alerts.http.controller.PersonServiceMock.knownPerson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.knownPersonJson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.manyPerson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.manyPersonJson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.unknownPerson;
import static com.safetynet.alerts.http.controller.PersonServiceMock.unknownPersonJson;
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

@WebMvcTest(PersonController.class)
class PersonControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        PersonServiceMock.init(personService);
    }

    @Test
    void getPersonWithFailedValidation() throws Exception {
        // invalid id
        mockMvc.perform(get("/person/" + invalidPersonId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void getPersonNotExisting() throws Exception {
        // unknown id
        mockMvc.perform(get("/person/" + unknownPerson().getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorPersonNotFound()));
    }

    @Test
    void getPerson() throws Exception {
        // known id
        mockMvc.perform(get("/person/" + knownPerson().getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(knownPersonJson()));
    }

    @Test
    void createPersonWithFailedValidation() throws Exception {
        // missing body
        mockMvc.perform(post("/person"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid body
        mockMvc.perform(post("/person")
                .contentType(APPLICATION_JSON).content(invalidPersonJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void createPerson() throws Exception {
        // create
        mockMvc.perform(post("/person")
                .contentType(APPLICATION_JSON).content(unknownPersonJson()))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/person/" + unknownPerson().getId()));
    }

    @Test
    void createPersonWithSimilarNames() throws Exception {
        // names already exists (allowSimilarNames=false)
        mockMvc.perform(post("/person")
                .contentType(APPLICATION_JSON).content(knownPersonJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorPersonExists()));

        // names already exists (allowSimilarNames=true)
        mockMvc.perform(post("/person").queryParam("allowSimilarNames", "true")
                .contentType(APPLICATION_JSON).content(knownPersonJson()))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl("/person/" + knownPerson().getId()));
    }

    @Test
    void createPersonWithInterferingAddress() throws Exception {
        // interfering address
        mockMvc.perform(post("/person")
                .contentType(APPLICATION_JSON).content(interferingAddressPersonJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorInterferingAddress()));
    }

    @Test
    void updatePersonWithFailedValidation() throws Exception {
        // missing body
        mockMvc.perform(put("/person/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid body
        mockMvc.perform(put("/person/1")
                .contentType(APPLICATION_JSON).content(invalidPersonJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void updatePersonNotExisting() throws Exception {
        // unknown
        mockMvc.perform(put("/person/" + unknownPerson().getId())
                .contentType(APPLICATION_JSON).content(unknownPersonJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorPersonNotFound()));
    }

    @Test
    void updatePerson() throws Exception {
        // known
        mockMvc.perform(put("/person/" + knownPerson().getId())
                .contentType(APPLICATION_JSON).content(knownPersonJson()))
                .andExpect(status().isNoContent())
                .andExpect(redirectedUrl("/person/" + knownPerson().getId()));
    }

    @Test
    void updatePersonWithSimilarNames() throws Exception {
        // update to names that already exists (allowSimilarNames=false)
        mockMvc.perform(put("/person/" + knownPerson().getId())
                .contentType(APPLICATION_JSON).content(manyPersonJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorPersonExists()));

        // update to names that already exists (allowSimilarNames=true)
        mockMvc.perform(put("/person/" + knownPerson().getId()).queryParam("allowSimilarNames", "true")
                .contentType(APPLICATION_JSON).content(manyPersonJson()))
                .andExpect(status().isNoContent())
                .andExpect(redirectedUrl("/person/" + manyPerson().getId()));
    }

    @Test
    void updatePersonWithInterferingAddress() throws Exception {
        // interfering address
        mockMvc.perform(put("/person/" + knownPerson().getId())
                .contentType(APPLICATION_JSON).content(interferingAddressPersonJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorInterferingAddress()));
    }

    @Test
    void updatePersonByNamesWithFailedValidation() throws Exception {
        // missing names
        mockMvc.perform(put("/person")
                .contentType(APPLICATION_JSON).content(unknownPersonJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid names
        mockMvc.perform(put("/person")
                .queryParam("firstName", invalidPersonFirstName()).queryParam("lastName", invalidPersonLastName())
                .contentType(APPLICATION_JSON).content(unknownPersonJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void updatePersonByNamesNotExisting() throws Exception {
        // unknown
        mockMvc.perform(put("/person")
                .queryParam("firstName", unknownPerson().getFirstName()).queryParam("lastName", unknownPerson().getLastName())
                .contentType(APPLICATION_JSON).content(unknownPersonJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorPersonNotFound()));
    }

    @Test
    void updatePersonByNames() throws Exception {
        // known
        mockMvc.perform(put("/person")
                .queryParam("firstName", knownPerson().getFirstName()).queryParam("lastName", knownPerson().getLastName())
                .contentType(APPLICATION_JSON).content(knownPersonJson()))
                .andExpect(status().isNoContent())
                .andExpect(redirectedUrl("/person/" + knownPerson().getId()));
    }

    @Test
    void updatePersonByNamesChangingNames() throws Exception {
        // changing names
        mockMvc.perform(put("/person")
                .queryParam("firstName", knownPerson().getFirstName()).queryParam("lastName", knownPerson().getLastName())
                .contentType(APPLICATION_JSON).content(manyPersonJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(errorImmutableNames()));
    }

    @Test
    void updatePersonByNamesWithSimilarNames() throws Exception {
        // similar names
        mockMvc.perform(put("/person")
                .queryParam("firstName", manyPerson().getFirstName()).queryParam("lastName", manyPerson().getLastName())
                .contentType(APPLICATION_JSON).content(manyPersonJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorInterferingNames()));
    }

    @Test
    void updatePersonByNamesWithInterferingAddress() throws Exception {
        // interfering address
        mockMvc.perform(put("/person")
                .queryParam("firstName", knownPerson().getFirstName()).queryParam("lastName", knownPerson().getLastName())
                .contentType(APPLICATION_JSON).content(interferingAddressPersonJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorInterferingAddress()));
    }

    @Test
    void deletePersonWithFailedValidation() throws Exception {
        // invalid id
        mockMvc.perform(delete("/person/" + invalidPersonId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void deletePersonNotExisting() throws Exception {
        // unknown
        mockMvc.perform(delete("/person/" + unknownPerson().getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorPersonNotFound()));
    }

    @Test
    void deletePerson() throws Exception {
        // known
        mockMvc.perform(delete("/person/" + knownPerson().getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePersonByNamesWithFailedValidation() throws Exception {
        // missing names
        mockMvc.perform(delete("/person")
                .contentType(APPLICATION_JSON).content(unknownPersonJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));

        // invalid names
        mockMvc.perform(delete("/person")
                .queryParam("firstName", invalidPersonFirstName()).queryParam("lastName", invalidPersonLastName()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(VALIDATION_FAILED));
    }

    @Test
    void deletePersonByNamesNotExisting() throws Exception {
        // unknown
        mockMvc.perform(delete("/person")
                .queryParam("firstName", unknownPerson().getFirstName()).queryParam("lastName", unknownPerson().getLastName()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(errorPersonNotFound()));
    }

    @Test
    void deletePersonByNames() throws Exception {
        // known
        mockMvc.perform(delete("/person")
                .queryParam("firstName", knownPerson().getFirstName()).queryParam("lastName", knownPerson().getLastName()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePersonByNamesWithSimilarNames() throws Exception {
        // similar names
        mockMvc.perform(delete("/person")
                .queryParam("firstName", manyPerson().getFirstName()).queryParam("lastName", manyPerson().getLastName()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value(errorInterferingNames()));
    }
}
