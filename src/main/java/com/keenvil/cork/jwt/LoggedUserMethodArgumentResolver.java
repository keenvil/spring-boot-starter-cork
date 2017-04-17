package com.keenvil.cork.jwt;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * This handler inject the current logged User in a REST service call.
 *
 * <p>In order to do so, service signature must annotate a an Individual
 * parameter with {@link LoggedIndividual} annotation.</p>  
 */
public class LoggedUserMethodArgumentResolver
    implements HandlerMethodArgumentResolver {


  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    if (parameter.hasParameterAnnotation(LoggedIndividual.class)) {
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
    JwtUser jwt = (JwtUser) servletRequest.getAttribute("X-Jwt-User");

    String parameterName = parameter.getParameterName();
    WebDataBinder binder =
        binderFactory.createBinder(webRequest, null, parameterName);

    Class<?> parameterType = parameter.getParameterType();
    Long userId = jwt.getUserAccountId();
    return binder.convertIfNecessary(userId, parameterType, parameter);
  }
}
