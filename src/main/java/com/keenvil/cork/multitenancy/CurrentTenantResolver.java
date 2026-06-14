package com.keenvil.cork.multitenancy;

import static org.slf4j.LoggerFactory.getLogger;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.keenvil.cork.CommunityIdentifierResolver;

/**
 * Resolves the Community Id for this requests.
 * 
 * <p>Used by Hibernate to identify the data source to be used by a request.</p>
 */
@Component
public class CurrentTenantResolver
    implements CurrentTenantIdentifierResolver<String> {

  private static Logger log = getLogger(CurrentTenantResolver.class);
  
  @Autowired
  private CommunityIdentifierResolver communityResolver;
  
  @Override
  public String resolveCurrentTenantIdentifier() {
    log.trace("Entering CurrentTenantResolver.");

    String communityId = communityResolver.resolve();

    log.trace("Leaving CurrentTenantResolver with tenant {}.", communityId);
    return communityId;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
