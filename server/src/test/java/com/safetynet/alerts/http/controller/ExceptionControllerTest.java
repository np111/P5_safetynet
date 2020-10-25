package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.service.AlertsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;

import static com.safetynet.alerts.http.controller.AlertsServiceMock.knownAddress;
import static com.safetynet.alerts.util.ApiErrorCode.BAD_REQUEST;
import static com.safetynet.alerts.util.ApiErrorCode.SERVER_EXCEPTION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertsController.class)
class ExceptionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertsService alertsService;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void unhandledException() throws Exception {
        when(alertsService.getFire(any()))
                .thenThrow(new RuntimeException("test unhandled exception"));

        mockMvc.perform(get("/fire").queryParam("address", knownAddress()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("UNKNOWN"))
                .andExpect(jsonPath("$.code").value(SERVER_EXCEPTION));
    }

    @Test
    void noHandlerFound() throws Exception {
        // invalid number
        mockMvc.perform(get("/an/unknown/handler"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("CLIENT"))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST));
    }

    @Test
    void bodyNotReadable() throws Exception {
        when(alertsService.getFire(any()))
                .thenThrow(new HttpMessageNotReadableException("test", (HttpInputMessage) null));

        mockMvc.perform(get("/fire").queryParam("address", knownAddress()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("CLIENT"))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST));
    }
}