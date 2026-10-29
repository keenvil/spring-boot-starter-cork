package com.keenvil.cork.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

/**
 * Documento que genera springdoc con la configuracion de cork: info con la seccion comun, respuestas de error
 * estandar, sin endpoints internos y Javadoc (therapi) como summary/description.
 */
@SpringBootTest(classes = OpenApiDocumentTest.App.class, properties = {
    "spring.application.name=demo-api",
    "keenvil.openapi.enabled=true",
    "keenvil.openapi.title=Keenvil · Demo",
    "keenvil.openapi.description=Servicio de prueba.",
    "keenvil.openapi.servers[0].url=https://api.example.com/demo",
    "keenvil.openapi.servers[0].description=UAT"
    })
class OpenApiDocumentTest {

  @SpringBootConfiguration
  @EnableAutoConfiguration(excludeName = {
      "com.keenvil.cork.CorkAutoConfiguration",
      "com.keenvil.cork.multitenancy.MultitenancyAutoConfiguration",
      "com.keenvil.cork.jwt.JwtAutoConfiguration",
      "com.keenvil.cork.totp.TotpAutoConfiguration",
      "com.keenvil.cork.mongo.MongoCompatAutoConfiguration",
      "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
      "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
      "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
      "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration",
      "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration",
      "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration",
      "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
      "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
      "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration",
      "org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration"})
  @Import(DemoController.class)
  static class App { }

  /** Datos de una multa de prueba. */
  record Fine(
      /** Patente del vehiculo. */
      String plate) { }

  /** Multas de prueba. */
  @RestController
  static class DemoController {

    /**
     * Devuelve una multa.
     *
     * <p>Busca la multa por id en la comunidad.</p>
     *
     * @param id comunidad
     * @param fineId id de la multa
     * @return la multa
     */
    @GetMapping("/c/{id}/fines/{fineId}")
    @ApiError(status = 409, code = "amount.changed", description = "El importe cambio.")
    @ApiError(status = 409, code = "fine.closed", description = "La multa esta cerrada.")
    @ApiError(status = 400, code = "category.automatic", description = "Categoria automatica.")
    Fine fine(@PathVariable("id") String id, @PathVariable("fineId") Long fineId,
        @com.keenvil.cork.jwt.JwtLoggedUser com.keenvil.cork.jwt.JwtUser user) {
      return new Fine("ABC123");
    }

    /** Crea una multa. */
    @PostMapping("/c/{id}/fines")
    @ApiResponse(responseCode = "409", description = "La patente ya tiene una multa abierta.")
    Fine create(@PathVariable("id") String id, @RequestBody Fine fine) {
      return fine;
    }

    /** Endpoint publico. */
    @GetMapping("/public/ping")
    @SecurityRequirements
    String ping() {
      return "pong";
    }

    @GetMapping("/c/{id}/internal/people")
    String internal(@PathVariable("id") String id) {
      return "x";
    }

    @GetMapping("/internal/health")
    String internalHealth() {
      return "x";
    }
  }

  @Autowired
  private WebApplicationContext context;

  private JsonNode doc;

  @BeforeEach
  void load() throws Exception {
    MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
    String json = mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
    doc = new ObjectMapper().readTree(json);
  }

  @Test
  void infoConTituloDescripcionYSeccionComun() {
    assertThat(doc.at("/info/title").asText()).isEqualTo("Keenvil · Demo");
    assertThat(doc.at("/info/description").asText())
        .startsWith("Servicio de prueba.")
        .contains(KeenvilOpenApiCustomizer.COMMON_SECTION_MARK)
        .contains("X-Authorization");
    assertThat(doc.at("/servers/0/url").asText()).isEqualTo("https://api.example.com/demo");
    assertThat(doc.at("/components/securitySchemes/X-Authorization/name").asText()).isEqualTo("X-Authorization");
  }

  @Test
  void respuestasDeErrorEstandar() {
    JsonNode get = doc.at("/paths/~1c~1{id}~1fines~1{fineId}/get/responses");
    assertThat(get.has("200")).isTrue();
    assertThat(get.at("/401/$ref").asText()).isEqualTo("#/components/responses/Unauthorized");
    assertThat(get.at("/404/$ref").asText()).isEqualTo("#/components/responses/NotFound");
    assertThat(get.has("422")).isFalse();
    assertThat(get.at("/409/description").asText()).contains("`amount.changed`").contains("`fine.closed`");
    assertThat(get.at("/409/content/application~1json/examples/amount.changed/value/0/code").asText())
        .isEqualTo("amount.changed");
    assertThat(get.at("/409/content/application~1json/examples/amount.changed/value/0/module").asText())
        .isEqualTo("demo-api");
    assertThat(get.at("/400/description").asText()).isEqualTo("`category.automatic`: Categoria automatica.");

    JsonNode post = doc.at("/paths/~1c~1{id}~1fines/post/responses");
    assertThat(post.at("/409/description").asText()).isEqualTo("La patente ya tiene una multa abierta.");
    assertThat(post.at("/409/content/application~1json/schema/$ref").asText())
        .isEqualTo("#/components/schemas/KeenvilApiErrors");
    assertThat(post.has("422")).isTrue();

    JsonNode ping = doc.at("/paths/~1public~1ping/get/responses");
    assertThat(ping.has("401")).isFalse();
    assertThat(ping.has("500")).isTrue();

    assertThat(doc.at("/components/schemas/KeenvilApiError/properties/errorId").isMissingNode()).isFalse();
    assertThat(doc.at("/components/responses/InternalServerError/content/application~1json/examples")
        .isMissingNode()).isFalse();
  }

  @Test
  void sinEndpointsInternos() {
    assertThat(doc.at("/paths").fieldNames()).toIterable()
        .noneMatch(path -> path.contains("internal"))
        .contains("/public/ping");
  }

  @Test
  void javadocComoSummaryYDescription() {
    JsonNode get = doc.at("/paths/~1c~1{id}~1fines~1{fineId}/get");
    assertThat(get.at("/summary").asText()).isEqualTo("Devuelve una multa");
    assertThat(get.at("/description").asText()).contains("Busca la multa por id");
    assertThat(get.at("/parameters/1/description").asText()).isEqualTo("id de la multa");
    assertThat(get.at("/parameters/0/description").asText()).isEqualTo("comunidad");
    assertThat(get.at("/parameters").size()).isEqualTo(2);
    assertThat(doc.at("/paths/~1c~1{id}~1fines/post/parameters/0/description").asText())
        .isEqualTo(KeenvilErrorResponsesCustomizer.COMMUNITY_ID_DESCRIPTION);
    assertThat(doc.at("/components/schemas/Fine/description").asText()).isEqualTo("Datos de una multa de prueba.");
  }
}
