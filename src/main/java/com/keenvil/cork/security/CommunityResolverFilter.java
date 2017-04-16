package com.keenvil.cork.security;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Resolves the selected community id for this request.
 * 
 * <p>This filters looks for the X-Community-Id http header and sets that value
 * under a request attribute.</p>
 * 
 * <p>Tenant value is mandatory since Hibernate needs at least a default
 * value to run.</p>
 */
public class CommunityResolverFilter extends HandlerInterceptorAdapter {

  private static Logger log = getLogger(CommunityResolverFilter.class);

  /** The Community Id header's name. */
  public static final String X_COMMUNITY_ID = "X-Community-Id";

  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler) throws Exception {
    log.trace("Entering preHandle.");
    
    String communityId = request.getHeader(X_COMMUNITY_ID);
    if (communityId != null && !communityId.isEmpty()) {

      if (log.isDebugEnabled()) {
        log.debug("X-Community-Id header found with value: {}", communityId);
      }
      request.setAttribute("community-id", communityId);
    }
    
    log.trace("Leaving preHandle.");
    return true;
  }
}
