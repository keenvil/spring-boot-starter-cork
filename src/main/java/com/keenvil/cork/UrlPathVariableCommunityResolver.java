package com.keenvil.cork;

import static org.slf4j.LoggerFactory.getLogger;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.keenvil.cork.jwt.JwtTokenHolder;

/**
 * <p>Encapsulates behavior to get current request Community Id from the
 * requested URI.</p>
 * 
 * <p>In order to do that, request URIs must match the following pattern
 * {@code [protocol]:[port]/[context]/c/{id}/[endpoint]}.</p>
 * 
 * <p>If no community was defined, returns a default Tenant identifier.
 * This is mandatory since, Hibernate needs a default tenant to
 * run.</p>
 * TODO (mario): Review how to handle unknown Tenants. 
 */
@Component
public class UrlPathVariableCommunityResolver 
    extends CommunityIdentifierResolver {

  private static Logger log =
      getLogger(UrlPathVariableCommunityResolver.class);

  /** Community Id delimiter. */
  private static final String COMMUNITYID_DELIMITER = "c";

  /** Default tenant. */
  public static final String DEFAULT_TENANT = "default";

  @Override
  public String resolve() {
    log.trace("Resolving Community Id with URL path variable resolver.");

    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes instanceof ServletRequestAttributes servletAttr) {
      HttpServletRequest request = servletAttr.getRequest();
      String[] uriComponents = request.getRequestURI().split("/");
      int delimiter = ArrayUtils.indexOf(uriComponents, COMMUNITYID_DELIMITER);

      if (delimiter > 0 && uriComponents.length > (delimiter + 1)) {
        String communityId = uriComponents[delimiter + 1];
        log.info("Resolved Community id using URL path variable: {}",
            communityId);

        JwtTokenHolder.holdCommunity(communityId);
        return communityId;
      }
      log.trace("Leaving UrlPathVariableCommunityResolverHelper with default tenant.");
      return defaultTenant();
    }

    // Sin RequestAttributes -- p.ej. un hilo de scheduler/ejecutor interno sin
    // ninguna request HTTP asociada (no hay TaskDecorator propagando nada porque no
    // hay ninguna request en curso, a diferencia del caso de una request reciclada).
    // Cae al community que el propio codigo de fondo haya fijado explicitamente con
    // JwtTokenHolder.holdCommunity(...) antes de llamar, usando el tenant que ya
    // tiene a mano. El caso de arriba ("hay request valida pero sin /c/{id}/ en la
    // URL") sigue yendo directo a defaultTenant() sin pasar por el holder, para no
    // arrastrar un valor viejo de un uso anterior del mismo hilo.
    String heldCommunity = JwtTokenHolder.community();
    if (heldCommunity != null) {
      log.trace("Resolved Community id using JwtTokenHolder fallback: {}", heldCommunity);
      return heldCommunity;
    }

    log.trace("Leaving UrlPathVariableCommunityResolverHelper with default tenant.");
    return defaultTenant();
  }

  @Override
  public String defaultTenant() {
    return DEFAULT_TENANT;
  }
}
