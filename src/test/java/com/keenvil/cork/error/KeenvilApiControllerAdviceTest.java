package com.keenvil.cork.error;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.keenvil.cork.jwt.JwtInvalidTokenException;

public class KeenvilApiControllerAdviceTest {

  private KeenvilApiControllerAdvice advice;
  private HttpServletRequest request;

  @Before
  public void beforeStart() {
    advice = new KeenvilApiControllerAdvice();
    ReflectionTestUtils.setField(advice, "name", "keenvil/test");

    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setServerName("localhost");
    mockRequest.setMethod("GET");
    mockRequest.setRequestURI("/c/entrenamianto/articles");
    request = mockRequest;
  }

  @Test
  public void aMissingJwtIsReportedAsUnauthorizedNotAsAnUncaughtServerError() {
    JwtInvalidTokenException exception =
        new JwtInvalidTokenException("Json Web Token not found.");

    ResponseEntity<List<KeenvilApiError>> response =
        advice.handleJwtInvalidToken(request, exception);

    assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    assertThat(response.getBody().get(0).getCode(), is("unauthorized"));
  }
}
