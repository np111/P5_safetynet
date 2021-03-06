package com.safetynet.alerts.api.response;

import com.safetynet.alerts.api.model.Person;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;

/**
 * List of {@linkplain Person#getPhone()} phones} of {@linkplain Person persons} covered by a given firestation.
 */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class PhoneAlertResponse implements Cloneable {
    @Singular("phone")
    private List<String> phones;

    @SneakyThrows
    public PhoneAlertResponse clone() {
        PhoneAlertResponse o = (PhoneAlertResponse) super.clone();
        if (o.phones != null) {
            o.phones = new ArrayList<>(o.phones);
        }
        return o;
    }
}
