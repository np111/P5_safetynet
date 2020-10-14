package com.safetynet.alerts.util.spring;

import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestWrapper {
    private HttpServletRequest request;
}
