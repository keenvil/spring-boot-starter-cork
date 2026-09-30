package com.keenvil.cork.error;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Resolution of the last-resort {@code RuntimeException} handler through Spring MVC: unexpected
 * errors are logged and answered with the standard body, while Spring MVC client errors and
 * Spring Security exceptions keep their previous behaviour.
 */
public class KeenvilApiControllerAdviceMvcTest {

  @RestController
  static class TestController {
    @GetMapping("/npe")
    String npe() {
      throw new NullPointerException("Profile cannot be null.");
    }

    @GetMapping("/status")
    String status() {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "nope");
    }

    @GetMapping("/denied")
    String denied() {
      throw new AccessDeniedException("denied");
    }

    @GetMapping("/checked")
    String checked() throws IOException {
      throw new IOException("disk");
    }

    @GetMapping("/param")
    String param(@RequestParam("key") String key) {
      return key;
    }

    @PostMapping("/body")
    String body(@Valid @RequestBody Payload payload) {
      return payload.name;
    }
  }

  static class Payload {
    @NotBlank
    public String name;
  }

  /** Same shape as townhall-api: its own handler for Exception in the subclass. */
  static class SubclassWithGenericHandler extends KeenvilApiControllerAdvice {
    @ExceptionHandler(Exception.class)
    @ResponseBody ResponseEntity<Void> handleUnexpectedException(
        final HttpServletRequest request, final Exception exception) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
  }

  private ListAppender<ILoggingEvent> logs;

  @BeforeEach
  public void setUp() {
    logs = new ListAppender<>();
    logs.start();
    ((Logger) LoggerFactory.getLogger(KeenvilApiControllerAdvice.class)).addAppender(logs);
  }

  @AfterEach
  public void tearDown() {
    ((Logger) LoggerFactory.getLogger(KeenvilApiControllerAdvice.class)).detachAppender(logs);
  }

  private MockMvc mvc(KeenvilApiControllerAdvice advice) {
    ReflectionTestUtils.setField(advice, "name", "keenvil/test");
    return MockMvcBuilders.standaloneSetup(new TestController()).setControllerAdvice(advice).build();
  }

  @Test
  public void anUnexpectedRuntimeExceptionIsLoggedWithStackAndAnsweredAs500() throws Exception {
    mvc(new KeenvilApiControllerAdvice()).perform(get("/npe"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$[0].code").value("internalError"))
        .andExpect(jsonPath("$[0].httpStatus").value(500))
        .andExpect(jsonPath("$[0].errorId").exists())
        .andExpect(jsonPath("$[0].detail").value("Unexpected error"))
        .andExpect(jsonPath("$[0].source").doesNotExist())
        .andExpect(jsonPath("$[0].localHostName").doesNotExist());

    ILoggingEvent event = logs.list.get(0);
    assertThat(event.getLevel(), is(Level.ERROR));
    assertThat(event.getThrowableProxy(), notNullValue());
    assertThat(event.getThrowableProxy().getClassName(), is(NullPointerException.class.getName()));
  }

  @Test
  public void aResponseStatusExceptionKeepsItsStatus() throws Exception {
    mvc(new KeenvilApiControllerAdvice()).perform(get("/status"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$[0].title").value("Not Found"));
    assertThat(logs.list.get(0).getLevel(), is(Level.WARN));
  }

  @Test
  public void malformedJsonIsStillA400() throws Exception {
    mvc(new KeenvilApiControllerAdvice())
        .perform(post("/body").contentType(MediaType.APPLICATION_JSON).content("{not json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$[0].code").value("badRequest"));
  }

  @Test
  public void anInvalidBodyIsStillA400() throws Exception {
    mvc(new KeenvilApiControllerAdvice())
        .perform(post("/body").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void aMissingRequestParameterIsStillA400() throws Exception {
    mvc(new KeenvilApiControllerAdvice()).perform(get("/param"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void anUnsupportedMethodIsStillA405() throws Exception {
    mvc(new KeenvilApiControllerAdvice()).perform(put("/npe"))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  public void springSecurityExceptionsAreNotSwallowed() {
    MockMvc mvc = mvc(new KeenvilApiControllerAdvice());
    ServletException thrown = assertThrows(ServletException.class,
        () -> mvc.perform(get("/denied")));
    assertThat(thrown.getCause() instanceof AccessDeniedException, is(true));
  }

  @Test
  public void aSubclassWithItsOwnExceptionHandlerStillStartsAndKeepsCheckedExceptions()
      throws Exception {
    MockMvc mvc = mvc(new SubclassWithGenericHandler());
    mvc.perform(get("/checked")).andExpect(status().isServiceUnavailable());
    mvc.perform(get("/npe"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$[0].code").value("internalError"));
  }
}
