package com.keenvil.cork.jwt;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

/** 
 * Filter to authenticate JSON Web Tokens.
 * 
 * <p>This filter looks for the JWT as a header parameter, parse it and creates
 * an Authentication object which is placed in the {@link SecurityContextHolder}
 * for future uses.</p>
 * 
 * <p>If the token is present but there is a problem parsing it, no
 * Authentication is set in Spring Security Context Holder and
 * {@link JwtAuthenticationEntryPoint} will be called at the end of the
 * filter chain.</p>
 * 
 * <p>This filter sets the JwtUser and JwtUser Id as http requests
 * headers for internal use.</p>
 * 
 * <p>Filter also sets Jwt token and Community in {@link JwtTokenHolder} to
 * be available in a static way. It is used by Feign clients to forward
 * Jwt token and Community in subsequent API calls.</p>
 * See {@link LoggedIndividual}.
 * See {@link JwtTokenHolder}.
 */
public class JwtAuthenticationFilter
    extends UsernamePasswordAuthenticationFilter {

  private static Logger log =
      getLogger(UsernamePasswordAuthenticationFilter.class);

  public static final String X_JWT_USER = "X-Jwt-User";
  
  public static final String X_USER_ID = "X-User-Id";

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
      String community = httpRequest.getHeader("X-Community-Id");

      if (token != null
          && !token.isEmpty()
          && SecurityContextHolder.getContext().getAuthentication() == null) {

        JwtUser jwtUser = jwtService.parse(token);
        if (log.isDebugEnabled()) {
          log.debug("User {} authenticated.", jwtUser.toString());
        }

        JwtTokenHolder.holdToken(token);
        JwtTokenHolder.holdCommunity(community);

        httpRequest.setAttribute(X_JWT_USER, jwtUser);
        httpRequest.setAttribute(X_USER_ID, jwtUser.getUserAccountId());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(jwtUser, null,
                jwtUser.getAuthorities());

        WebAuthenticationDetails buildDetails =
            new WebAuthenticationDetailsSource().buildDetails(httpRequest);
        authentication.setDetails(buildDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (JwtInvalidTokenException exception) {
      SecurityContextHolder.clearContext();
    }
    log.trace("Leaving JwtAuthenticationFilter.");
    chain.doFilter(request, response);
  }
}
