package com.keenvil.cork.openapi;

import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.DefaultPropertiesPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Defaults de Swagger UI para toda la flota (5.1.0): tags y operaciones en orden alfabetico, modelos colapsados,
 * duracion del pedido y filtro. Menor precedencia: el yml del servicio los pisa. Sin springdoc no tienen efecto.
 */
public class OpenApiUiDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

  static final Map<String, Object> DEFAULTS = Map.of(
      "springdoc.swagger-ui.tags-sorter", "alpha",
      "springdoc.swagger-ui.operations-sorter", "alpha",
      "springdoc.swagger-ui.doc-expansion", "none",
      "springdoc.swagger-ui.default-models-expand-depth", "0",
      "springdoc.swagger-ui.display-request-duration", "true",
      "springdoc.swagger-ui.filter", "true",
      "springdoc.swagger-ui.persist-authorization", "true");

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    DefaultPropertiesPropertySource.addOrMerge(DEFAULTS, environment.getPropertySources());
  }
}
