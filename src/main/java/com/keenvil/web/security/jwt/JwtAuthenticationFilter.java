package com.keenvil.web.security.jwt;

import static org.slf4j.LoggerFactory.getLogger;

import com.keenvil.web.security.jwt.JwtService.JwtUser;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/** Filter to authenticate JSON Web Tokens.
 *  This filter looks for the JWT as a header parameter, parse it and creates
 *  an Authentication which is placed in the Spring Security Context Holder
 *  for future uses.
 */
public class JwtAuthenticationFilter
    extends UsernamePasswordAuthenticationFilter {

  private static Logger log =
      getLogger(UsernamePasswordAuthenticationFilter.class);

  private JwtService jwtService;

  public JwtAuthenticationFilter(final JwtService theJwtService) {
    Validate.notNull(theJwtService);
    jwtService = theJwtService;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    log.trace("Entering JwtAuthenticationFilter.");

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String token = httpRequest.getHeader(JwtService.X_AUTHORIZATION);

    if (token != null
        && !token.isEmpty()
        && SecurityContextHolder.getContext().getAuthentication() == null) {

      JwtUser jwtUser = jwtService.parse(token);
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(jwtUser, null,
              jwtUser.getAuthorities());

      WebAuthenticationDetails buildDetails =
          new WebAuthenticationDetailsSource().buildDetails(httpRequest);
      authentication.setDetails(buildDetails);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    chain.doFilter(request, response);

    log.trace("Leaving JwtAuthenticationFilter.");
  }  
}
