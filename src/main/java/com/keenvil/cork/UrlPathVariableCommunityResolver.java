package com.keenvil.cork;

import static org.slf4j.LoggerFactory.getLogger;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.keenvil.cork.jwt.JwtTokenHolder;
import com.keenvil.cork.multitenancy.MultitenancyConfigurationProperties;

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

  /** Literal historico: unico fallback razonable cuando la app ni siquiera tiene
   * multitenancy de cork habilitado (ver defaultTenant()). */
  private static final String DEFAULT_TENANT_FALLBACK = "default";

  // required = false: apps que usan community-resolver: URL solo para resolucion de
  // JWT/seguridad, sin habilitar el multitenancy de cork (excluyen
  // MultitenancyAutoConfiguration explicitamente, ej. community-api), nunca tienen este
  // bean -- @Autowired obligatorio rompia el boot entero con NoSuchBeanDefinitionException.
  @Autowired(required = false)
  private MultitenancyConfigurationProperties multitenancyProperties;

  @Override
  public String resolve() {
    log.trace("Resolving Community Id with URL path variable resolver.");

    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes instanceof ServletRequestAttributes servletAttr) {
      // En un hilo @Async, el TaskDecorator puede propagar estos mismos
      // RequestAttributes, pero para cuando el hilo async corre, el
      // HttpServletRequest real puede ya haber sido reciclado por el
      // contenedor (la respuesta ya se mando). getRequestURI() sobre una
      // request reciclada tira NullPointerException en vez de simplemente
      // no encontrar comunidad -por eso este tramo va en su propio
      // try/catch, distinto del caso "request valida sin /c/" de abajo,
      // que debe seguir devolviendo el tenant por defecto tal cual
      // (confirmado en produccion de crowd-api, ImageService.uploadAvatars).
      try {
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
      } catch (Exception e) {
        log.warn("Could not read tenant from the current request (likely recycled by the "
            + "container in an @Async thread); falling back to JwtTokenHolder.", e);
        String heldCommunity = JwtTokenHolder.community();
        if (heldCommunity != null) {
          return heldCommunity;
        }
        return defaultTenant();
      }
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

  // Antes devolvia el literal "default", que no matchea el name real de ningun tenant
  // configurado (ej. "primary") -- selectDataSource(tenantIdentifier) en
  // DataSourceBasedCommunityConnectionProvider busca por ese name exacto en el mapa
  // poblado desde application.yml, asi que el literal siempre resolvia a null y
  // explotaba con NPE en cualquier codigo que tocara un repositorio sin un /c/{id}/
  // en la URL ni un JwtTokenHolder.holdCommunity(...) previo (ej. @Before de un test,
  // un scheduler sin contexto de request).
  @Override
  public String defaultTenant() {
    if (multitenancyProperties == null) {
      return DEFAULT_TENANT_FALLBACK;
    }
    return multitenancyProperties.getDefaultTenant().getName();
  }
}
