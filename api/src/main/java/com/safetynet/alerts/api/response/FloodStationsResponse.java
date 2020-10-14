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
 * List of all homes covered by the given firestation (grouped by address).
 */
@lombok.Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class FloodStationsResponse implements Cloneable {
    @Singular("station")
    private List<Entry> stations;

    @SneakyThrows
    public FloodStationsResponse clone() {
        FloodStationsResponse o = (FloodStationsResponse) super.clone();
        if (o.stations != null) {
            o.stations = o.stations.stream().map(Entry::clone).collect(Collectors.toList());
        }
        return o;
    }

    @lombok.Builder(builderClassName = "Builder")
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    public static class Entry implements Cloneable {
        private String address;

        @Singular("person")
        private List<Person> persons;

        @SneakyThrows
        public Entry clone() {
            Entry o = (Entry) super.clone();
            if (o.persons != null) {
                o.persons = o.persons.stream().map(Person::clone).collect(Collectors.toList());
            }
            return o;
        }
    }
}
