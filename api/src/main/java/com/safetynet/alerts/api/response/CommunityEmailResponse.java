package com.safetynet.alerts.api.response;

import com.safetynet.alerts.api.model.Person;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;

/**
 * List of {@linkplain Person#getEmail()} email addresses} of {@linkplain Person persons} living in a given {@linkplain
 * Person#getCity()} city}.
 */
@lombok.Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class CommunityEmailResponse implements Cloneable {
    @Singular("email")
    private List<String> emails;

    @SneakyThrows
    public CommunityEmailResponse clone() {
        CommunityEmailResponse o = (CommunityEmailResponse) super.clone();
        if (o.emails != null) {
            o.emails = new ArrayList<>(emails);
        }
        return o;
    }
}
