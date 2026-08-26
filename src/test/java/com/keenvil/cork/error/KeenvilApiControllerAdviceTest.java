package com.keenvil.cork.error;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.keenvil.cork.jwt.JwtInvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

public class KeenvilApiControllerAdviceTest {

  private KeenvilApiControllerAdvice advice;
  private HttpServletRequest request;

  @BeforeEach
  public void beforeStart() {
    advice = new KeenvilApiControllerAdvice();
    ReflectionTestUtils.setField(advice, "name", "keenvil/test");
    request = createMock(HttpServletRequest.class);
  }

  @Test
  public void aMissingJwtIsReportedAsUnauthorizedNotAsAnUncaughtServerError() {
    expect(request.getServerName()).andReturn("localhost");
    expect(request.getLocalName()).andReturn("localhost");
    expect(request.getMethod()).andReturn("GET");
    expect(request.getRequestURI()).andReturn("/c/entrenamianto/articles");
    replay(request);

    JwtInvalidTokenException exception =
        new JwtInvalidTokenException("Json Web Token not found.");

    ResponseEntity<List<KeenvilApiError>> response =
        advice.handleJwtInvalidToken(request, exception);

    assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    assertThat(response.getBody().get(0).getCode(), is("unauthorized"));
  }
}
