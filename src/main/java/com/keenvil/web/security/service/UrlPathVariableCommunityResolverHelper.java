package com.keenvil.web.security.service;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Arrays;

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

  /** Community Id delimiter. */
  private static final String COMMUNITYID_DELIMITER = "c";

  /** Default tenant. */
  public static final String DEFAULT_TENANT = "default";
  
  public String resolve() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      HttpServletRequest request =
          ((ServletRequestAttributes) attributes).getRequest();
      String[] uriComponents = request.getRequestURI().split("/");
      int delimiter = Arrays.binarySearch(uriComponents, COMMUNITYID_DELIMITER);

      if (delimiter > 0 && uriComponents.length > delimiter) {
        String communityId = uriComponents[delimiter + 1];
        log.debug("Resolving Cummunity Id: {}", communityId);
        return communityId;        
      }
    }
    log.trace("Leaving resolveCurrentTenantIdentifier with default tenant.");
    return DEFAULT_TENANT;
  }
}
