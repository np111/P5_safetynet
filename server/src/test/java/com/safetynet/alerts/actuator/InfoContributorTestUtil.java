package com.safetynet.alerts.actuator;

import lombok.experimental.UtilityClass;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

@UtilityClass
public class InfoContributorTestUtil {
    public static Info doContribute(InfoContributor contributor) {
        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);
        return builder.build();
    }
}
