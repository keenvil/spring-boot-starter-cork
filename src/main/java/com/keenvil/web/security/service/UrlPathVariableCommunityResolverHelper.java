package com.keenvil.web.security.service;

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Encapsulates behavior to obtain the Community Id from the requested Uri.
 */
@Component
public class UrlPathVariableCommunityResolverHelper 
  implements CummunityResolverHelper {

  private static Logger log =
      getLogger(UrlPathVariableCommunityResolverHelper.class);
  
  /** Uri prefix to the community id position. */
  private static final int C_URI_COMPONENT = 1;

  /** Community id position in the uri. */
  private static final int COMMUNITY_URI_COMPONENT = 2;

  /** Minimum uri length components. */
  private static final int MINIMUM_URI_COMPONENTS = 4;

  /** Default tenant. */
  public static final String DEFAULT_TENANT = "default";
  
  public String resolve() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      HttpServletRequest request =
          ((ServletRequestAttributes) attributes).getRequest();
      String requestURI = request.getRequestURI();
      String[] uriComponents = requestURI.split("/");

      if (uriComponents.length >= MINIMUM_URI_COMPONENTS
          && uriComponents[C_URI_COMPONENT] != null
          && uriComponents[C_URI_COMPONENT].equals("c")) {
        String communityId = requestURI.split("/")[COMMUNITY_URI_COMPONENT];
        log.debug("Resolving Cummunity Id: {}", communityId);
        return communityId;        
      }
    }
    log.trace("Leaving resolveCurrentTenantIdentifier with default tenant.");
    return DEFAULT_TENANT;
  }
}
