package com.keenvil.cork.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;

class KeenvilOpenApiCustomizerTest {

  private final KeenvilOpenApiProperties properties = new KeenvilOpenApiProperties();

  private KeenvilOpenApiCustomizer customizer(BuildProperties build) {
    return new KeenvilOpenApiCustomizer(properties, "demo-api", build);
  }

  @Test
  void sinPropiedadesUsaElNombreDeLaAppYLaVersionDelBuild() {
    Properties build = new Properties();
    build.setProperty("version", "1.89.3");
    OpenAPI api = new OpenAPI().info(new Info().title(KeenvilOpenApiCustomizer.SPRINGDOC_DEFAULT_TITLE)
        .version(KeenvilOpenApiCustomizer.SPRINGDOC_DEFAULT_VERSION));
    customizer(new BuildProperties(build)).customise(api);
    assertThat(api.getInfo().getTitle()).isEqualTo("demo-api");
    assertThat(api.getInfo().getVersion()).isEqualTo("1.89.3");
    assertThat(api.getInfo().getDescription()).startsWith(KeenvilOpenApiCustomizer.COMMON_SECTION_MARK);
  }

  @Test
  void respetaLaInfoDelServicioYNoDuplicaLaSeccionComun() {
    Info propia = new Info().title("Mi API").version("2").description("Hace cosas.");
    OpenAPI api = new OpenAPI().info(propia);
    customizer(null).customise(api);
    customizer(null).customise(api);
    assertThat(api.getInfo().getTitle()).isEqualTo("Mi API");
    assertThat(api.getInfo().getVersion()).isEqualTo("2");
    assertThat(api.getInfo().getDescription().split(KeenvilOpenApiCustomizer.COMMON_SECTION_MARK, -1)).hasSize(2);
    assertThat(propia.getDescription()).isEqualTo("Hace cosas.");
  }

  @Test
  void ocultaInternosSalvoQueSeApague() {
    OpenAPI api = new OpenAPI().paths(new Paths()
        .addPathItem("/c/{id}/internal/segment-people", new PathItem())
        .addPathItem("/internal/x", new PathItem())
        .addPathItem("/c/{id}/internals-report", new PathItem())
        .addPathItem("/c/{id}/fines", new PathItem()));
    customizer(null).customise(api);
    assertThat(api.getPaths()).containsOnlyKeys("/c/{id}/internals-report", "/c/{id}/fines");

    properties.setHideInternal(false);
    OpenAPI todo = new OpenAPI().paths(new Paths().addPathItem("/internal/x", new PathItem()));
    customizer(null).customise(todo);
    assertThat(todo.getPaths()).containsKey("/internal/x");
  }

  @Test
  void ordenaLosTagsYRegistraLosErrores() {
    OpenAPI api = new OpenAPI().tags(new ArrayList<>(List.of(new Tag().name("Multas"), new Tag().name("actas"))));
    customizer(null).customise(api);
    assertThat(api.getTags()).extracting(Tag::getName).containsExactly("actas", "Multas");
    assertThat(api.getComponents().getSchemas()).containsKeys(KeenvilApiErrorDocs.ERROR_SCHEMA,
        KeenvilApiErrorDocs.ERRORS_SCHEMA);
    assertThat(api.getComponents().getResponses()).hasSize(KeenvilApiErrorDocs.ALL.size());
  }

  @Test
  void sinErroresEstandarNoHayCustomizerDeOperaciones() {
    new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(OpenApiAutoConfiguration.class))
        .withPropertyValues("keenvil.openapi.enabled=true", "keenvil.openapi.standard-errors=false")
        .run(ctx -> {
          assertThat(ctx).hasSingleBean(KeenvilOpenApiCustomizer.class);
          assertThat(ctx).doesNotHaveBean(KeenvilErrorResponsesCustomizer.class);
        });
  }

  @Test
  void defaultsDeSwaggerUiConMenorPrecedencia() {
    StandardEnvironment env = new StandardEnvironment();
    new OpenApiUiDefaultsEnvironmentPostProcessor().postProcessEnvironment(env, null);
    assertThat(env.getProperty("springdoc.swagger-ui.tags-sorter")).isEqualTo("alpha");
    assertThat(env.getProperty("springdoc.swagger-ui.operations-sorter")).isEqualTo("alpha");
  }
}
