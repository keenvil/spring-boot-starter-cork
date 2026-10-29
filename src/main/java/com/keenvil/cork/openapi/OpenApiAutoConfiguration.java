package com.keenvil.cork.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean OpenAPI (springdoc) con los esquemas de seguridad comunes de la flota. Reemplaza el SwaggerConfig que cada
 * servicio copiaba. Opt-in con {@code keenvil.openapi.enabled=true}; si el servicio define su propio OpenAPI, se
 * respeta el suyo.
 *
 * <p>5.1.0: con springdoc en el classpath agrega ademas los customizers de {@link KeenvilOpenApiCustomizer} (info,
 * servidores, sin endpoints internos, tags ordenados) y {@link KeenvilErrorResponsesCustomizer} (respuestas de
 * error estandar), que se aplican tambien sobre el OpenAPI propio del servicio. Ver {@link KeenvilOpenApiProperties}.
 * El JWT va en el header {@code X-Authorization} (antes el esquema decia {@code Authorization}, que el filtro no
 * lee).</p>
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnProperty(prefix = "keenvil.openapi", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(KeenvilOpenApiProperties.class)
public class OpenApiAutoConfiguration {

  static final String JWT_TOKEN_KEY = "X-Authorization";
  static final String API_KEY = "X-Api-Key";

  @Bean
  @ConditionalOnMissingBean(OpenAPI.class)
  public OpenAPI keenvilOpenApi() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes(JWT_TOKEN_KEY, new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER).name(JWT_TOKEN_KEY)
                .description("JWT de la sesion (security-api), tal cual, sin `Bearer`."))
            .addSecuritySchemes(API_KEY, new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER).name(API_KEY)
                .description("Clave de integracion. Solo la usan las operaciones que la declaran.")))
        .addSecurityItem(new SecurityRequirement().addList(JWT_TOKEN_KEY));
  }

  /** Customizers de springdoc: solo si springdoc (no solo swagger-models) esta en el classpath. */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(name = "org.springdoc.core.customizers.GlobalOpenApiCustomizer")
  static class SpringdocCustomizers {

    static {
      // El usuario del JWT lo inyecta cork (@JwtLoggedUser): no es un parametro del pedido.
      org.springdoc.core.utils.SpringDocUtils.getConfig()
          .addAnnotationsToIgnore(com.keenvil.cork.jwt.JwtLoggedUser.class)
          .addRequestWrapperToIgnore(com.keenvil.cork.jwt.JwtUser.class);
    }

    @Bean
    KeenvilOpenApiCustomizer keenvilOpenApiCustomizer(KeenvilOpenApiProperties properties,
        @Value("${spring.application.name:api}") String applicationName,
        ObjectProvider<BuildProperties> buildProperties) {
      return new KeenvilOpenApiCustomizer(properties, applicationName, buildProperties.getIfAvailable());
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.security.access.prepost.PreAuthorize")
    KeenvilRolesCustomizer keenvilRolesCustomizer() {
      return new KeenvilRolesCustomizer();
    }

    @Bean
    @ConditionalOnProperty(prefix = "keenvil.openapi", name = "standard-errors", havingValue = "true",
        matchIfMissing = true)
    KeenvilErrorResponsesCustomizer keenvilErrorResponsesCustomizer(
        @Value("${spring.application.name:api}") String applicationName) {
      return new KeenvilErrorResponsesCustomizer(applicationName);
    }
  }
}
