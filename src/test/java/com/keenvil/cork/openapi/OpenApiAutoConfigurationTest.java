package com.keenvil.cork.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OpenApiAutoConfigurationTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(OpenApiAutoConfiguration.class));

  @Test
  void apagadoPorDefecto() {
    runner.run(ctx -> assertThat(ctx).doesNotHaveBean(OpenAPI.class));
  }

  @Test
  void conElFlagExponeLosEsquemasDeSeguridad() {
    runner.withPropertyValues("keenvil.openapi.enabled=true").run(ctx -> {
      OpenAPI api = ctx.getBean(OpenAPI.class);
      assertThat(api.getComponents().getSecuritySchemes())
          .containsKeys(OpenApiAutoConfiguration.JWT_TOKEN_KEY, OpenApiAutoConfiguration.API_KEY);
    });
  }

  @Test
  void respetaElOpenApiDelServicio() {
    OpenAPI propio = new OpenAPI();
    runner.withPropertyValues("keenvil.openapi.enabled=true").withBean(OpenAPI.class, () -> propio)
        .run(ctx -> assertThat(ctx.getBean(OpenAPI.class)).isSameAs(propio));
  }
}
