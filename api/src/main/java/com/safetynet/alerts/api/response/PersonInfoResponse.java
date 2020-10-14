package com.safetynet.alerts.api.response;

import com.safetynet.alerts.api.model.Person;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;

/**
 * List of {@linkplain Person persons} with a given first and last name.
 */
@lombok.Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class PersonInfoResponse {
    @Singular("person")
    private List<Person> persons;

    @SneakyThrows
    public PersonInfoResponse clone() {
        PersonInfoResponse o = (PersonInfoResponse) super.clone();
        if (o.persons != null) {
            o.persons = o.persons.stream().map(Person::clone).collect(Collectors.toList());
        }
        return o;
    }
}
