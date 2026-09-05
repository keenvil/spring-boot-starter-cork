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
    if (attributes != null) {
      // En un hilo @Async, el TaskDecorator puede propagar estos mismos
      // RequestAttributes, pero para cuando el hilo async corre, el
      // HttpServletRequest real puede ya haber sido reciclado por el
      // contenedor (la respuesta ya se mando). getRequestURI() sobre una
      // request reciclada tira NullPointerException en vez de simplemente
      // no encontrar comunidad -por eso este tramo va en su propio
      // try/catch, distinto del caso "request valida sin /c/" de abajo,
      // que debe seguir devolviendo el tenant por defecto tal cual.
      try {
        HttpServletRequest request =
            ((ServletRequestAttributes) attributes).getRequest();
        String[] uriComponents = request.getRequestURI().split("/");
        int delimiter = ArrayUtils.indexOf(uriComponents, COMMUNITYID_DELIMITER);

        if (delimiter > 0 && uriComponents.length > (delimiter + 1)) {
          String communityId = uriComponents[delimiter + 1];
          log.info("Resolved Community id using URL path variable: {}",
              communityId);

          JwtTokenHolder.holdCommunity(communityId);
          return communityId;
        }
        log.trace("Leaving UrlPathVariableCommunityResolverHelper with"
            + " default tenant.");
        return defaultTenant();
      } catch (Exception e) {
        log.trace("Request attributes present but request is unusable, "
            + "falling back: {}", e.getMessage());
      }
    }

    // Sin RequestAttributes (hilo @Async sin decorator) o con una request
    // ya reciclada: se cae al community que el propio codigo async haya
    // fijado explicitamente con JwtTokenHolder.holdCommunity(...) antes de
    // llamar, usando el dato que ya tiene a mano como parametro. Solo se
    // llega aca cuando no hay forma de leer la URL real, nunca quita
    // prioridad al caso normal de arriba.
    String heldCommunity = JwtTokenHolder.community();
    if (heldCommunity != null) {
      log.trace("Resolved Community id using JwtTokenHolder fallback: {}", heldCommunity);
      return heldCommunity;
    }

    log.trace("Leaving UrlPathVariableCommunityResolverHelper with"
        + " default tenant.");
    return defaultTenant();
  }

  @Override
  public String defaultTenant() {
    return DEFAULT_TENANT;
  }
}
