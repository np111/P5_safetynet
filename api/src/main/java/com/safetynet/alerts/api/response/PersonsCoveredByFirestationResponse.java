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
 * List of {@linkplain Person persons} covered by a given firestation.
 */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class PersonsCoveredByFirestationResponse implements Cloneable {
    /**
     * Children count (under the age of 18).
     */
    private Integer childrenCount;

    /**
     * Adults count (aged 18 and over, or with an unknown age).
     */
    private Integer adultsCount;

    @Singular("person")
    private List<Person> persons;

    @SneakyThrows
    public PersonsCoveredByFirestationResponse clone() {
        PersonsCoveredByFirestationResponse o = (PersonsCoveredByFirestationResponse) super.clone();
        if (o.persons != null) {
            o.persons = o.persons.stream().map(Person::clone).collect(Collectors.toList());
        }
        return o;
    }
}
