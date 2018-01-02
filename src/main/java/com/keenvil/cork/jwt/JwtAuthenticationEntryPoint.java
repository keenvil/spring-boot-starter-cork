package com.keenvil.cork.jwt;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keenvil.cork.error.KeenvilApiError;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Called when {@link AuthenticationException} is thrown.
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

  private static Logger log = getLogger(JwtAuthenticationEntryPoint.class);

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
    String code = "";
    String title = null;
    int responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    try {
      if (token != null) {
        jwtService.parse(token);
      }
    } catch (JwtInvalidTokenException platformException) {
      log.error("JwtInvalidTokenException authenticationError [{}]", token);
      exception = platformException;
      code = "authenticationError";
      title = "Authentication error";
      responseStatus = HttpServletResponse.SC_FORBIDDEN;
    } catch (JwtExpiredTokenException platformException) {
      log.error("JwtExpiredTokenException tokenExpired [{}]", token);
      exception = platformException;
      code = "tokenExpired";
      title = "Authentication error";
      responseStatus = HttpServletResponse.SC_EXPECTATION_FAILED;
    }

    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder()
        .code(code)
        .httpStatus(responseStatus)
        .title(title)
        .detail(exception.getMessage())
        .request(request)
        .source(exception)
        .build();

    ObjectMapper mapper = new ObjectMapper();
    response.setContentType("application/json");
    response.setStatus(responseStatus);
    response.getOutputStream().println(mapper.writeValueAsString(error));
  }
}