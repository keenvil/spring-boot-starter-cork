package com.keenvil.cork.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.info.BuildProperties;

/**
 * Completa el documento de cada servicio: info (titulo, version, descripcion con la seccion comun, contacto),
 * servidores, esquema y respuestas de error, sin los endpoints internos y con los tags en orden alfabetico.
 * Arma un {@link Info} nuevo en cada llamada (springdoc puede reusar el del bean entre recalculos).
 */
class KeenvilOpenApiCustomizer implements GlobalOpenApiCustomizer {

  /** Titulo y version que pone springdoc cuando nadie los define. */
  static final String SPRINGDOC_DEFAULT_TITLE = "OpenAPI definition";
  static final String SPRINGDOC_DEFAULT_VERSION = "v0";

  static final String COMMON_SECTION_MARK = "## Convenciones de la API Keenvil";

  static final String COMMON_SECTION = COMMON_SECTION_MARK + "\n\n"
      + "**Autenticacion.** Toda operacion, salvo las marcadas sin candado, pide el JWT de la sesion en el header "
      + "`X-Authorization` (el token tal cual, sin `Bearer`). Lo emite security-api en el login y vence; con token "
      + "faltante, invalido o vencido la respuesta es 401. En Swagger UI: boton *Authorize* → `X-Authorization`.\n\n"
      + "**Comunidad.** Las rutas `/c/{id}/...` operan sobre la comunidad (barrio) `{id}`. El rol del usuario se "
      + "evalua en esa comunidad: sin el rol que pide la operacion la respuesta es 403; si el modulo no esta "
      + "habilitado en la comunidad, 404.\n\n"
      + "**Errores.** Todo error responde una lista de `KeenvilApiError` "
      + "(`[{httpStatus, code, title, detail, module, uri, httpMethod, hostName, errorId}]`). El cliente decide por "
      + "`httpStatus` y `code`; `errorId` identifica el error en los logs. Codigos genericos: 400 pedido mal formado, "
      + "401 sin sesion, 403 sin permiso, 404 no existe, 409 conflicto, 412 estado invalido, 422 validacion, "
      + "500 error inesperado.\n\n"
      + "**Paginacion.** Los listados paginados reciben `page` (desde 0), `size` y `sort` (`campo,asc|desc`) y "
      + "devuelven `{content, totalElements, totalPages, number, size, ...}`.\n\n"
      + "**Fechas.** ISO-8601: `2026-10-29` para fechas y `2026-10-29T14:30:00` para fecha y hora.";

  private static final Pattern INTERNAL = Pattern.compile("(^|/)internal(/|$)");

  private final KeenvilOpenApiProperties properties;
  private final String applicationName;
  private final BuildProperties buildProperties;

  KeenvilOpenApiCustomizer(KeenvilOpenApiProperties theProperties, String theApplicationName,
      BuildProperties theBuildProperties) {
    properties = theProperties;
    applicationName = theApplicationName;
    buildProperties = theBuildProperties;
  }

  @Override
  public void customise(OpenAPI openApi) {
    openApi.setInfo(info(openApi.getInfo()));
    if (!properties.getServers().isEmpty()) {
      openApi.setServers(properties.getServers().stream()
          .map(server -> new Server().url(server.getUrl()).description(server.getDescription())).toList());
    }
    if (properties.isHideInternal() && openApi.getPaths() != null) {
      Paths visible = new Paths();
      openApi.getPaths().forEach((path, item) -> {
        if (!INTERNAL.matcher(path).find()) {
          visible.addPathItem(path, item);
        }
      });
      openApi.setPaths(visible);
    }
    if (properties.isStandardErrors()) {
      if (openApi.getComponents() == null) {
        openApi.setComponents(new Components());
      }
      KeenvilApiErrorDocs.register(openApi.getComponents(), applicationName);
    }
    if (openApi.getTags() != null) {
      List<Tag> sorted = openApi.getTags().stream()
          .sorted(Comparator.comparing(Tag::getName, String.CASE_INSENSITIVE_ORDER)).toList();
      openApi.setTags(new ArrayList<>(sorted));
    }
  }

  Info info(Info current) {
    Info info = new Info();
    if (current != null) {
      info.setTermsOfService(current.getTermsOfService());
      info.setLicense(current.getLicense());
      info.setContact(current.getContact());
      info.setExtensions(current.getExtensions());
      info.setSummary(current.getSummary());
    }
    info.setTitle(firstOf(properties.getTitle(),
        current == null || SPRINGDOC_DEFAULT_TITLE.equals(current.getTitle()) ? null : current.getTitle(),
        applicationName));
    info.setVersion(firstOf(properties.getVersion(),
        current == null || SPRINGDOC_DEFAULT_VERSION.equals(current.getVersion()) ? null : current.getVersion(),
        buildProperties == null ? null : buildProperties.getVersion(), SPRINGDOC_DEFAULT_VERSION));
    String description = firstOf(properties.getDescription(), current == null ? null : current.getDescription());
    if (properties.isCommonSection() && (description == null || !description.contains(COMMON_SECTION_MARK))) {
      description = description == null || description.isBlank() ? COMMON_SECTION
          : description.stripTrailing() + "\n\n" + COMMON_SECTION;
    }
    info.setDescription(description);
    KeenvilOpenApiProperties.Contact contact = properties.getContact();
    if (contact.getName() != null || contact.getEmail() != null || contact.getUrl() != null) {
      info.setContact(new Contact().name(contact.getName()).email(contact.getEmail()).url(contact.getUrl()));
    }
    return info;
  }

  private static String firstOf(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}
