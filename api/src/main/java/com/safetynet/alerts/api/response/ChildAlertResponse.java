package com.safetynet.alerts.api.response;

import com.safetynet.alerts.api.model.Person;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;

/**
 * List of {@linkplain Person persons} living at a given {@linkplain Person#getAddress() address}.
 */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class ChildAlertResponse implements Cloneable {
    /**
     * Children list (under the age of 18).
     */
    @Singular("children")
    private List<Person> children;

    /**
     * Adults list (aged 18 and over, or with an unknown age).
     */
    @Singular("adult")
    private List<Person> adults;

    @SneakyThrows
    public ChildAlertResponse clone() {
        ChildAlertResponse o = (ChildAlertResponse) super.clone();
        if (o.children != null) {
            o.children = o.children.stream().map(Person::clone).collect(Collectors.toList());
        }
        if (o.adults != null) {
            o.adults = o.adults.stream().map(Person::clone).collect(Collectors.toList());
        }
        return o;
    }
}
