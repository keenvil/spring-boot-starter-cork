package com.keenvil.cork.error;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.keenvil.cork.error.KeenvilApiException.Authorization;
import com.keenvil.cork.error.KeenvilApiException.BadRequest;
import com.keenvil.cork.error.KeenvilApiException.Forbidden;
import com.keenvil.cork.error.KeenvilApiException.InvalidArgument;
import com.keenvil.cork.error.KeenvilApiException.InvalidResourceState;
import com.keenvil.cork.error.KeenvilApiException.ResourceAlreadyExists;
import com.keenvil.cork.error.KeenvilApiException.ResourceNotFound;
import com.keenvil.cork.error.KeenvilBusinessException.ValidationError;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

/** Status, codigo por defecto y codigo propio de cada handler del advice. */
class KeenvilApiControllerAdviceHandlersTest {

  private KeenvilApiControllerAdvice advice;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    advice = new KeenvilApiControllerAdvice();
    ReflectionTestUtils.setField(advice, "name", "keenvil/test");
    request = new MockHttpServletRequest("GET", "/c/keenvil/things/1");
  }

  private static void assertSingle(final ResponseEntity<List<KeenvilApiError>> response,
      final HttpStatus status, final String code, final String detail) {
    assertThat(response.getStatusCode().value(), is(status.value()));
    assertThat(response.getBody().size(), is(1));
    KeenvilApiError error = response.getBody().get(0);
    assertThat(error.getHttpStatus(), is(status.value()));
    assertThat(error.getCode(), is(code));
    assertThat(error.getDetail(), is(detail));
    assertThat(error.getModule(), is("keenvil/test"));
    assertThat(error.getUri(), is("/c/keenvil/things/1"));
    assertThat(error.getHttpMethod(), is("GET"));
  }

  @Test
  void authorizationUsaElCodigoPropioODefault() {
    assertSingle(advice.handleAuthorization(request, new Authorization("no")),
        HttpStatus.UNAUTHORIZED, "unauthorized", "no");
    assertSingle(advice.handleAuthorization(request, new Authorization("no", "expired")),
        HttpStatus.UNAUTHORIZED, "expired", "no");
  }

  @Test
  void resourceNotFound() {
    assertSingle(advice.handleResourceNotFound(request, new ResourceNotFound("x")),
        HttpStatus.NOT_FOUND, "resourceNotFound", "x");
    assertSingle(advice.handleResourceNotFound(request, new ResourceNotFound("x", "noUnit")),
        HttpStatus.NOT_FOUND, "noUnit", "x");
  }

  @Test
  void resourceAlreadyExists() {
    assertSingle(advice.handleResourceAlreadyExist(request, new ResourceAlreadyExists("dup")),
        HttpStatus.CONFLICT, "resourceAlreadyExists", "dup");
    assertSingle(advice.handleResourceAlreadyExist(request, new ResourceAlreadyExists("dup", "dupMail")),
        HttpStatus.CONFLICT, "dupMail", "dup");
  }

  @Test
  void invalidResourceState() {
    assertSingle(advice.handleInvalidState(request, new InvalidResourceState("st")),
        HttpStatus.PRECONDITION_FAILED, "invalidResourceState", "st");
    assertSingle(advice.handleInvalidState(request, new InvalidResourceState("st", "closed")),
        HttpStatus.PRECONDITION_FAILED, "closed", "st");
  }

  @Test
  void invalidArgument() {
    assertSingle(advice.handleInvalidArgument(request, new InvalidArgument("arg")),
        HttpStatus.UNPROCESSABLE_ENTITY, "invalidArgument", "arg");
    assertSingle(advice.handleInvalidArgument(request, new InvalidArgument("arg", "badDni")),
        HttpStatus.UNPROCESSABLE_ENTITY, "badDni", "arg");
  }

  @Test
  void forbidden() {
    assertSingle(advice.handleForbidden(request, new Forbidden("f")),
        HttpStatus.FORBIDDEN, "forbidden", "f");
    assertSingle(advice.handleForbidden(request, new Forbidden("f", "otherCommunity")),
        HttpStatus.FORBIDDEN, "otherCommunity", "f");
  }

  @Test
  void badRequest() {
    assertSingle(advice.handleBadRequest(request, new BadRequest("b")),
        HttpStatus.BAD_REQUEST, "badRequest", "b");
    assertSingle(advice.handleBadRequest(request, new BadRequest("b", "missing")),
        HttpStatus.BAD_REQUEST, "missing", "b");
  }

  @Test
  void apiExceptionGenericaEs500() {
    assertSingle(advice.handleApiException(request, new KeenvilApiException("boom")),
        HttpStatus.INTERNAL_SERVER_ERROR, "internalServerError", "boom");
  }

  @Test
  void validationErrorDevuelveUnErrorPorCampo() {
    ValidationError exception = new ValidationError(List.of(
        new ObjectError("dto", new String[] {"NotNull.dto.name"}, null, "name required"),
        new ObjectError("dto", new String[] {"Size.dto.dni"}, null, "dni too long")));

    ResponseEntity<List<KeenvilApiError>> response = advice.handleValidationErrors(request, exception);

    assertThat(response.getStatusCode().value(), is(422));
    assertThat(response.getBody().size(), is(2));
    assertThat(response.getBody().get(0).getCode(), is("NotNull.dto.name"));
    assertThat(response.getBody().get(1).getDetail(), is("dni too long"));
  }

  @Test
  void runtimeInesperadaEs500SinDetalleInterno() {
    ResponseEntity<List<KeenvilApiError>> response =
        advice.handleUnexpectedException(request, new IllegalStateException("db password wrong"));

    assertThat(response.getStatusCode().value(), is(500));
    assertThat(response.getBody().get(0).getCode(), is("internalError"));
    assertThat(response.getBody().get(0).getDetail(), is("Unexpected error"));
  }

  @Test
  void runtimeConStatusDeClienteLoConserva() {
    ResponseEntity<List<KeenvilApiError>> response = advice.handleUnexpectedException(request,
        new ResponseStatusException(HttpStatus.NOT_FOUND, "nope"));

    assertThat(response.getStatusCode().value(), is(404));
    assertThat(response.getBody().get(0).getCode(), is("clientError"));
    assertThat(response.getBody().get(0).getTitle(), is("Not Found"));
  }

  @ResponseStatus(HttpStatus.GONE)
  static class GoneException extends RuntimeException {
    GoneException() {
      super("gone");
    }
  }

  @Test
  void resolveStatusLeeLaAnotacionResponseStatus() {
    assertThat(KeenvilApiControllerAdvice.resolveStatus(new GoneException()).value(), is(410));
    assertThat(KeenvilApiControllerAdvice.resolveStatus(new RuntimeException()).value(), is(500));
  }

  @Test
  void lasExcepcionesDeSpringSecuritySeRelanzan() {
    AccessDeniedException denied = new AccessDeniedException("denied");
    AccessDeniedException thrown = assertThrows(AccessDeniedException.class,
        () -> advice.handleUnexpectedException(request, denied));
    assertSame(denied, thrown);
  }
}
