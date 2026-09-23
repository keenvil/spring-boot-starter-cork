package com.keenvil.cork.jwt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

/**
 * Cubre el bug real encontrado en producción (guard-api/trebuchet, 2026-09-23): cualquier
 * request sin token devolvía 500 en vez de 401, porque el default de responseStatus solo
 * se corregía dentro del bloque que reintenta parsear el token -- y ese bloque nunca corre
 * si el header directamente no está presente.
 */
public class JwtAuthenticationEntryPointTest {

  private final JwtService jwtService = new JwtService();
  private final JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();

  {
    // No hay setter publico; el campo se inyecta por @Autowired en el bean real,
    // acá lo seteamos a mano vía reflection para no depender de un ApplicationContext.
    try {
      java.lang.reflect.Field field = JwtAuthenticationEntryPoint.class.getDeclaredField("jwtService");
      field.setAccessible(true);
      field.set(entryPoint, jwtService);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void noTokenAtAll_returns401NotServerError() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    entryPoint.commence(request, response,
        new InsufficientAuthenticationException("no auth"));

    assertThat(response.getStatus(), is(401));
  }

  @Test
  public void invalidToken_stillReturns403AsBefore() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(JwtService.X_AUTHORIZATION, "not-a-real-jwt");
    MockHttpServletResponse response = new MockHttpServletResponse();

    entryPoint.commence(request, response,
        new InsufficientAuthenticationException("invalid"));

    assertThat(response.getStatus(), is(403));
  }

  @Test
  public void expiredToken_stillReturns417AsBefore() throws Exception {
    Date past = new Date(System.currentTimeMillis() - 60_000);
    String expired = jwtService.generate("1", "Joe", "Average", "B-52",
        "user@keenvil.com", Collections.emptySet(), past);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(JwtService.X_AUTHORIZATION, expired);
    MockHttpServletResponse response = new MockHttpServletResponse();

    entryPoint.commence(request, response,
        new InsufficientAuthenticationException("expired"));

    assertThat(response.getStatus(), is(417));
  }
}
