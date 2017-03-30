package com.keenvil.web.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keenvil.core.error.PlatformError;
import com.keenvil.core.error.PlatformError.PlatformErrorBuilder;
import com.keenvil.core.error.PlatformException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Called when {@link AuthenticationException} is thrown.
 * 
 * <p>To get to this point one of two things happened:
 * <ul>
 * <li>An {@link AuthenticationException} was thrown by some other object,</li>
 * <li>There was a problem parsing the JWT and not {@link Authentication}
 * was set in the {@link SecurityContextHolder}. In this case, token
 * will be parsed again since {@link JwtAuthenticationFilter} is executed before 
 *  {@link ExceptionTranslationFilter}</li>.
 * </ul>
 * </p>
 */
@Component
public class JwtAuthenticationEntryPoint
    implements AuthenticationEntryPoint, Serializable {

  private static final long serialVersionUID = 1L;

  @Autowired
  private JwtService jwtService;

  @Override
  public void commence(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authenticationException)
          throws IOException {

    String token = request.getHeader(JwtService.X_AUTHORIZATION);
    Exception exception = authenticationException;

    try {
      if (token != null) {
        jwtService.parse(token);
      }
    } catch (PlatformException.InvalidJwtToken platformException) {
      exception = platformException;
    }

    PlatformError error = new PlatformErrorBuilder()
        .code("authenticationError")
        .httpStatus(HttpServletResponse.SC_UNAUTHORIZED)
        .title("Authentication error")
        .detail(exception.getMessage())
        .request(request)
        .source(exception)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getOutputStream().println(mapper.writeValueAsString(error));
  }
}