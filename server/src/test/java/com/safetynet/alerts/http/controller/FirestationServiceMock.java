package com.safetynet.alerts.http.controller;

import com.safetynet.alerts.api.model.Firestation;
import com.safetynet.alerts.service.FirestationService;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import static org.mockito.Mockito.when;

@UtilityClass
public class FirestationServiceMock {
    public static void init(FirestationService firestationService) throws Exception {
        // GET
        // - Get an existing firestation
        when(firestationService.getFirestation(knownFirestation().getAddress()))
                .thenReturn(knownFirestation());

        // CREATE
        // - Create a new firestation
        when(firestationService.createFirestation(unknownFirestation()))
                .thenReturn(new FirestationService.UpdateResult(true, unknownFirestation()));

        // - Create an existing firestation (results in an update)
        when(firestationService.createFirestation(knownFirestation()))
                .thenReturn(new FirestationService.UpdateResult(false, knownFirestation()));

        // UPDATE
        // - Update an unknown firestation (results in a creation)
        when(firestationService.updateFirestation(unknownFirestation().getAddress(), unknownFirestation()))
                .thenReturn(new FirestationService.UpdateResult(true, unknownFirestation()));

        // - Update an existing firestation
        when(firestationService.updateFirestation(knownFirestation().getAddress(), knownFirestation()))
                .thenReturn(new FirestationService.UpdateResult(false, knownFirestation()));

        // - Update an existing firestation + change it's address
        when(firestationService.updateFirestation(knownFirestation().getAddress(), unknownFirestation()))
                .thenThrow(new FirestationService.ImmutableAddressException());

        // DELETE
        // - Delete an existing firestation
        when(firestationService.deleteFirestation(knownFirestation().getAddress()))
                .thenReturn(true);
    }

    public static Firestation knownFirestation() {
        return Firestation.builder()
                .address("known")
                .station("1")
                .build();
    }

    public static String knownFirestationJson() {
        return "{\"address\":\"known\""
                + ",\"station\":\"1\""
                + "}";
    }

    public static Firestation unknownFirestation() {
        return Firestation.builder()
                .address("unknown")
                .station("1")
                .build();
    }

    public static String unknownFirestationJson() {
        return "{\"address\":\"unknown\""
                + ",\"station\":\"1\""
                + "}";
    }

    public static String invalidFirestationAddress() {
        return StringUtils.repeat('a', 256);
    }

    public static String invalidFirestationJson() {
        return "{\"address:\":\"" + StringUtils.repeat('a', 256) + "\",\"station\":\"1\"}";
    }
}
