package com.keenvil.cork.multitenancy;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * <p>Encapsulates behavior to get current request Community Id from a
 * requested attribute.</p>
 * 
 * <p>In order to be able to do that request must contain an attribute named
 * {@code community-id} with the community id value.</p>
 * 
 * <p>If no community was defined, returns a default Tenant identifier.
 * This is mandatory since, Hibernate needs a default tenant to
 * run.</p>
 */
@Component
public class RequestAttributeCommunityResolverHelper
    extends CummunityResolver {

  private static Logger log =
      getLogger(UrlPathVariableCommunityResolverHelper.class);

  /** Attribute name. */
  private static final String COMMUNITY_ID = "community-id";

  /** Default tenant. */
  public static final String DEFAULT_TENANT = "default";

  @Override
  public String resolve() {
    log.trace("Resolving Community Id with request attribute resolver.");

    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    String communityId = (String) attributes.getAttribute(COMMUNITY_ID,
            RequestAttributes.SCOPE_REQUEST);
    if (communityId == null) {
      log.trace("Leaving RequestAttributeCommunityResolverHelper with"
          + " default tenant.");
      return defaultTenant();
    }

    if (log.isDebugEnabled()) {
      log.debug("Request made over Cummunity Id: {}", communityId);          
    }
    return communityId;
  }

  @Override
  public String defaultTenant() {
    return defaultTenant();
  }
}
