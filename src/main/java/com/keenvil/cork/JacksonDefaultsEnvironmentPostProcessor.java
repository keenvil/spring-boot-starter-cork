package com.keenvil.cork;

import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.DefaultPropertiesPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Boot 4 / Jackson 3 (ARQUITECTURA §9.20): mientras convivan servicios en Boot 3 y Boot 4, el JSON por
 * HTTP, Feign y Rabbit tiene que salir igual que con Jackson 2. Pone {@code spring.jackson.use-jackson2-defaults=true}
 * con la menor precedencia: un servicio lo apaga en su yml cuando toda la flota este en Jackson 3.
 */
public class JacksonDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

  static final String USE_JACKSON2_DEFAULTS = "spring.jackson.use-jackson2-defaults";

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    DefaultPropertiesPropertySource.addOrMerge(Map.of(USE_JACKSON2_DEFAULTS, "true"),
        environment.getPropertySources());
  }
}
