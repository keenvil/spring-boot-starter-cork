package io.mycommunity.commons.multitenant;

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import io.mycommunity.commons.error.PlatformException;

/** Resolves the selected community id for this request.
 * 
 * <p>This filters looks for the X-Community-Id http header validates that
 * it is present and it is not empty.</p>
 */
public class CommunityResolverFilter extends HandlerInterceptorAdapter {

  private static Logger log = getLogger(CommunityResolverFilter.class);

  /** The Community Id header's name. */
  public static final String X_COMMUNITY_ID = "X-Community-Id";

  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler) throws Exception {
    log.trace("Entering doFilterInternal.");
    
    String communityId = request.getHeader(X_COMMUNITY_ID);
    if (communityId == null || communityId.isEmpty()) {
      throw new PlatformException
        .InvalidCommunityId("Http Header X-Community-Id was not found.");
    }
    
    if (log.isDebugEnabled()) {
      log.debug("X-Community-Id: {}", communityId);
    }
    
    request.setAttribute("community-id", communityId);
    log.trace("Leaving doFilterInternal.");
    return true;
  }
}
