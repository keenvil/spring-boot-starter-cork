package com.keenvil.cork.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Bean OpenAPI (springdoc) con los esquemas de seguridad comunes de la flota (X-Authorization y X-Api-Key en el
 * header Authorization). Reemplaza el SwaggerConfig que cada servicio copiaba. Opt-in con
 * {@code keenvil.openapi.enabled=true}; si el servicio define su propio OpenAPI, se respeta el suyo.
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnProperty(prefix = "keenvil.openapi", name = "enabled", havingValue = "true")
public class OpenApiAutoConfiguration {

  static final String JWT_TOKEN_KEY = "X-Authorization";
  static final String API_KEY = "X-Api-Key";

  @Bean
  @ConditionalOnMissingBean(OpenAPI.class)
  public OpenAPI keenvilOpenApi() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes(JWT_TOKEN_KEY, apiKeyScheme())
            .addSecuritySchemes(API_KEY, apiKeyScheme()))
        .addSecurityItem(new SecurityRequirement().addList(JWT_TOKEN_KEY).addList(API_KEY));
  }

  private SecurityScheme apiKeyScheme() {
    return new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("Authorization");
  }
}
