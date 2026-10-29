package com.keenvil.cork.openapi;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades {@code keenvil.openapi.*} del documento OpenAPI de cada servicio (5.1.0). Todas son opcionales:
 * sin {@code title} se usa {@code spring.application.name}; sin {@code version}, la del build
 * ({@code BuildProperties}) si existe.
 */
@ConfigurationProperties(prefix = "keenvil.openapi")
public class KeenvilOpenApiProperties {

  /** Activa la configuracion OpenAPI de cork. */
  private boolean enabled;

  /** Titulo del documento. */
  private String title;

  /** Descripcion del servicio (markdown); se le agrega la seccion comun de la flota. */
  private String description;

  /** Version del documento; por defecto la del build. */
  private String version;

  /** Agrega a la descripcion la seccion comun (autenticacion, errores, paginacion). */
  private boolean commonSection = true;

  /** Agrega a cada operacion las respuestas de error estandar de cork. */
  private boolean standardErrors = true;

  /** Saca del documento los endpoints {@code /internal/**} y {@code /c/{id}/internal/**} (servicio a servicio). */
  private boolean hideInternal = true;

  private final Contact contact = new Contact();

  /** Servidores del documento; si no hay, springdoc usa la URL del request. */
  private List<Server> servers = new ArrayList<>();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean theEnabled) {
    enabled = theEnabled;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String theTitle) {
    title = theTitle;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String theDescription) {
    description = theDescription;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String theVersion) {
    version = theVersion;
  }

  public boolean isCommonSection() {
    return commonSection;
  }

  public void setCommonSection(boolean theCommonSection) {
    commonSection = theCommonSection;
  }

  public boolean isStandardErrors() {
    return standardErrors;
  }

  public void setStandardErrors(boolean theStandardErrors) {
    standardErrors = theStandardErrors;
  }

  public boolean isHideInternal() {
    return hideInternal;
  }

  public void setHideInternal(boolean theHideInternal) {
    hideInternal = theHideInternal;
  }

  public Contact getContact() {
    return contact;
  }

  public List<Server> getServers() {
    return servers;
  }

  public void setServers(List<Server> theServers) {
    servers = theServers;
  }

  /** Contacto del equipo duenio del servicio. */
  public static class Contact {

    private String name;
    private String url;
    private String email;

    public String getName() {
      return name;
    }

    public void setName(String theName) {
      name = theName;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String theUrl) {
      url = theUrl;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String theEmail) {
      email = theEmail;
    }
  }

  /** Servidor del documento (p. ej. UAT o local). */
  public static class Server {

    private String url;
    private String description;

    public String getUrl() {
      return url;
    }

    public void setUrl(String theUrl) {
      url = theUrl;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String theDescription) {
      description = theDescription;
    }
  }
}
