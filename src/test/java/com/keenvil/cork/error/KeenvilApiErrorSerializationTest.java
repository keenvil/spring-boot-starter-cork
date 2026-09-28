package com.keenvil.cork.error;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** P0-SEC-13: la respuesta de error no expone stack trace ni la IP interna del pod. */
public class KeenvilApiErrorSerializationTest {

  @Test
  public void theResponseBodyHidesSourceAndLocalHostNameButTheLogKeepsThem() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/c/x/things");
    request.setLocalName("172.31.29.107");
    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder()
        .httpStatus(412).code("invalidResourceState").title("Invalid Resource State")
        .detail("boom").module("keenvil/test").request(request)
        .source(new IllegalStateException("boom"))
        .build();

    String json = new ObjectMapper().writeValueAsString(error);

    assertThat(json, not(containsString("\"source\"")));
    assertThat(json, not(containsString("localHostName")));
    assertThat(json, not(containsString("172.31.29.107")));
    assertThat(json, containsString("\"errorId\":\"" + error.getErrorId() + "\""));
    assertThat(json, containsString("\"uri\":\"/c/x/things\""));

    String logLine = error.toString();
    assertThat(error.getErrorId(), notNullValue());
    assertThat(logLine, containsString("errorId: " + error.getErrorId()));
    assertThat(logLine, containsString("localHostName: 172.31.29.107"));
    assertThat(logLine, containsString("KeenvilApiErrorSerializationTest"));
  }
}
