package com.keenvil.cork.multitenant;

import static org.slf4j.LoggerFactory.getLogger;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Resolves the Community Id for this requests.
 * 
 * <p>Resolves the community id which will be used to interact with the
 * platform. Community Id is defined as a uri path variable and accepted uri
 * must have the following pattern:
 * 
 * <p>/c/{community_id}/...</p>
 * 
 * <p>If no community was defined, returns a default tenant
 * identifier. This is mandatory since, Hibernate needs a default tenant to
 * run.</p>
 */
@Component
public class UrlPathVariableCommunityResolver
    implements CurrentTenantIdentifierResolver {

  private static Logger log =
      getLogger(CurrentTenantIdentifierResolver.class);
  
  @Autowired
  private UrlPathVariableCommunityResolverHelper helper;
  
  @Override
  public String resolveCurrentTenantIdentifier() {
    log.trace("Entering resolveCurrentTenantIdentifier.");

    String communityId = helper.resolve();

    log.trace("Leaving resolveCurrentTenantIdentifier with default tenant.");
    return communityId;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
