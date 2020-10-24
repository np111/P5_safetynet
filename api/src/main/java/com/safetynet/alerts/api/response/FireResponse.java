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
 * List of {@linkplain Person persons} living in a given {@linkplain Person#getAddress()} address} with the number of the firestation covering them.
 */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class FireResponse implements Cloneable {
    private String stationNumber;

    @Singular("person")
    private List<Person> persons;

    @SneakyThrows
    public FireResponse clone() {
        FireResponse o = (FireResponse) super.clone();
        if (o.persons != null) {
            o.persons = o.persons.stream().map(Person::clone).collect(Collectors.toList());
        }
        return o;
    }
}
