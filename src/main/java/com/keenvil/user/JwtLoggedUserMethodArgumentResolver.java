package com.keenvil.user;

import static com.keenvil.web.security.jwt.JwtAuthenticationFilter.X_JWT_USER;

import com.keenvil.core.error.PlatformException;
import com.keenvil.web.security.jwt.JwtService.JwtUser;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * This handler inject the current Jwt logged User in a REST service call.
 *
 * <p>In order to do so, service signature must annotate a an Individual
 * parameter with {@link JwtLoggedUser} annotation.</p>  
 */
public class JwtLoggedUserMethodArgumentResolver
    implements HandlerMethodArgumentResolver {


  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    if (parameter.hasParameterAnnotation(JwtLoggedUser.class)) {
      return true;
    }
    return false;
  }
  
  @Override
  public Object resolveArgument(MethodParameter parameter,
      ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) throws Exception {
    HttpServletRequest servletRequest =
        webRequest.getNativeRequest(HttpServletRequest.class);
    JwtUser jwt =
        (JwtUser) servletRequest.getAttribute(X_JWT_USER);
    if (jwt == null) {
      throw new PlatformException.InvalidJwtToken("Json Web Token not found.");
    }
    return jwt;
  }
}
