package com.keenvil.cork.error;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.keenvil.cork.error.KeenvilApiException.ResourceNotFound;
import com.keenvil.cork.jwt.JwtInvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

public class KeenvilApiControllerAdviceTest {

  private KeenvilApiControllerAdvice advice;
  private HttpServletRequest request;
  private ListAppender<ILoggingEvent> logs;

  @BeforeEach
  public void beforeStart() {
    advice = new KeenvilApiControllerAdvice();
    ReflectionTestUtils.setField(advice, "name", "keenvil/test");
    request = createMock(HttpServletRequest.class);
    logs = new ListAppender<>();
    logs.start();
    ((Logger) LoggerFactory.getLogger(KeenvilApiControllerAdvice.class)).addAppender(logs);
  }

  @AfterEach
  public void afterEach() {
    ((Logger) LoggerFactory.getLogger(KeenvilApiControllerAdvice.class)).detachAppender(logs);
  }

  private HttpServletRequest niceRequest(final String method, final String uri) {
    HttpServletRequest nice = createNiceMock(HttpServletRequest.class);
    expect(nice.getServerName()).andReturn("s.v2.keenvil.com").anyTimes();
    expect(nice.getLocalName()).andReturn("10.0.0.1").anyTimes();
    expect(nice.getMethod()).andReturn(method).anyTimes();
    expect(nice.getRequestURI()).andReturn(uri).anyTimes();
    replay(nice);
    return nice;
  }

  @Test
  public void aClientErrorIsLoggedAsOneWarnLineWithoutTheSource() {
    ResponseEntity<List<KeenvilApiError>> response = advice.handleResourceNotFound(
        niceRequest("GET", "/trebuchet/sm.php"), new ResourceNotFound("URL not Found"));

    KeenvilApiError error = response.getBody().get(0);
    assertThat(logs.list.size(), is(1));
    ILoggingEvent event = logs.list.get(0);
    assertThat(event.getLevel(), is(Level.WARN));
    String line = event.getFormattedMessage();
    assertThat(line, containsString("errorId: " + error.getErrorId()));
    assertThat(line, containsString("httpStatus: 404"));
    assertThat(line, containsString("code: resourceNotFound"));
    assertThat(line, containsString("httpMethod: GET"));
    assertThat(line, containsString("uri: /trebuchet/sm.php"));
    assertThat(line, not(containsString("source:")));
    assertThat(line, not(containsString("URL not Found")));
    assertThat(line, not(containsString("\n")));
  }

  @Test
  public void aServerErrorIsStillLoggedAsErrorWithTheSource() {
    advice.handleApiException(niceRequest("POST", "/c/x/things"),
        new KeenvilApiException("boom"));

    assertThat(logs.list.size(), is(1));
    ILoggingEvent event = logs.list.get(0);
    assertThat(event.getLevel(), is(Level.ERROR));
    assertThat(event.getFormattedMessage(), containsString("source:"));
  }

  @Test
  public void theResponseBodyOfAClientErrorDoesNotChange() {
    ResponseEntity<List<KeenvilApiError>> response = advice.handleResourceNotFound(
        niceRequest("GET", "/trebuchet/a.php"), new ResourceNotFound("URL not Found"));

    assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    KeenvilApiError error = response.getBody().get(0);
    assertThat(error.getCode(), is("resourceNotFound"));
    assertThat(error.getTitle(), is("Resource Not Found"));
    assertThat(error.getDetail(), is("URL not Found"));
    assertThat(error.getUri(), is("/trebuchet/a.php"));
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
