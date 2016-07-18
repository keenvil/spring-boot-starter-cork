package com.keenvil.web.security.jwt;

import static org.slf4j.LoggerFactory.getLogger;

import com.keenvil.core.error.PlatformException;
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
 *  an Authentication which is placed in the {@link SecurityContextHolder}
 *  for future uses.
 * 
 * <p>If the token is present but there is a problem parsing it, no
 * Authentication is set in Spring Security Context Holder and
 * {@link JwtAuthenticationEntryPoint} will be called at the end of the
 * filter chain.</p>
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

    try {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String token = httpRequest.getHeader(JwtService.X_AUTHORIZATION);

      if (token != null
          && !token.isEmpty()
          && SecurityContextHolder.getContext().getAuthentication() == null) {

        JwtUser jwtUser = null;
        jwtUser = jwtService.parse(token);
        if (log.isDebugEnabled()) {
          log.debug("User {} authenticated.", jwtUser.toString());
        }

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(jwtUser, null,
                jwtUser.getAuthorities());

        WebAuthenticationDetails buildDetails =
            new WebAuthenticationDetailsSource().buildDetails(httpRequest);
        authentication.setDetails(buildDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (PlatformException.InvalidJwtToken excepetion) {
      SecurityContextHolder.clearContext();
    }
    log.trace("Leaving JwtAuthenticationFilter.");
    chain.doFilter(request, response);
  }
}
