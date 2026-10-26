package com.keenvil.cork;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class JacksonDefaultsEnvironmentPostProcessorTest {

  private final JacksonDefaultsEnvironmentPostProcessor processor = new JacksonDefaultsEnvironmentPostProcessor();

  @Test
  void prendeLosDefaultsDeJackson2() {
    StandardEnvironment env = new StandardEnvironment();
    processor.postProcessEnvironment(env, null);
    assertEquals("true", env.getProperty(JacksonDefaultsEnvironmentPostProcessor.USE_JACKSON2_DEFAULTS));
  }

  @Test
  void elServicioLoPuedeApagar() {
    StandardEnvironment env = new StandardEnvironment();
    env.getPropertySources().addFirst(new MapPropertySource("application",
        Map.of(JacksonDefaultsEnvironmentPostProcessor.USE_JACKSON2_DEFAULTS, "false")));
    processor.postProcessEnvironment(env, null);
    assertEquals("false", env.getProperty(JacksonDefaultsEnvironmentPostProcessor.USE_JACKSON2_DEFAULTS));
  }
}
