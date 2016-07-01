package com.keenvil.commons.multitenant;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/** Resolves the Community Id for this requests.
 * 
 * <p>Resolves the community id which will be used to interact with the
 * platform. If no community was defined, returns a default tenant
 * identifier. This is mandatory since, Hibernate needs a default tenant to
 * run.</p>
 */
@Component
public class CurrentCommunityIdentifierResolver 
    implements CurrentTenantIdentifierResolver {


  private static Logger log =
      getLogger(CurrentCommunityIdentifierResolver.class);

  /** Attribute name for tenant/community. */
  private static final String COMMUNITY_ID = "community-id";

  /** Default tenant. */
  public static final String DEFAULT_TENANT = "default";

  @Override
  public String resolveCurrentTenantIdentifier() {
    log.trace("Entering resolveCurrentTenantIdentifier.");

    Optional<String> tenant = null;
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      tenant = Optional.ofNullable((String)
          attributes.getAttribute(COMMUNITY_ID,
          RequestAttributes.SCOPE_REQUEST));

      if (tenant.isPresent() && !tenant.get().isEmpty()) {

        if (log.isDebugEnabled()) {
          log.debug("Resolving Cummunity Id: {}", tenant.get());
        }
        return tenant.get();
      }
    }

    log.trace("Leaving resolveCurrentTenantIdentifier with default tenant.");
    return DEFAULT_TENANT;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
