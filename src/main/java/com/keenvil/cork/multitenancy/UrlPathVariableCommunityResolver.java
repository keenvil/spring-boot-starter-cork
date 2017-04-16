package com.keenvil.cork.multitenancy;

import static org.slf4j.LoggerFactory.getLogger;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Resolves the Community Id for this requests.
 * 
 * <p>Resolves the community id which will be used to interact with the
 * platform. Community Id is defined as a URI path variable.</p>
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
    log.trace("Entering UrlPathVariableCommunityResolver.");

    String communityId = helper.resolve();

    log.trace("Leaving UrlPathVariableCommunityResolver with tenant {}.",
        communityId);
    return communityId;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
