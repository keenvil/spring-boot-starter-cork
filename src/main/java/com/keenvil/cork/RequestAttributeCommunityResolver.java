package com.keenvil.cork;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.keenvil.cork.multitenancy.MultitenancyConfigurationProperties;

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
public class RequestAttributeCommunityResolver
    extends CommunityIdentifierResolver {

  private static Logger log =
      getLogger(RequestAttributeCommunityResolver.class);

  /** Attribute name. */
  private static final String COMMUNITY_ID = "community-id";

  @Autowired
  private MultitenancyConfigurationProperties multitenancyProperties;

  @Override
  public String resolve() {
    log.trace("Resolving Community Id with request attribute resolver.");

    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    String communityId = (String) attributes.getAttribute(COMMUNITY_ID,
            RequestAttributes.SCOPE_REQUEST);
    if (communityId == null) {
      log.trace("Leaving RequestAttributeCommunityResolver with"
          + " default tenant.");
      return defaultTenant();
    }
      log.info("Resolved Community id using Request Attribute: [{}]",
          communityId);
    return communityId;
  }

  // Ver UrlPathVariableCommunityResolver.defaultTenant() -- mismo bug, mismo fix: el
  // literal "default" no matcheaba el name real de ningun tenant configurado.
  @Override
  public String defaultTenant() {
    return multitenancyProperties.getDefaultTenant().getName();
  }
}
