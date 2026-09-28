package com.keenvil.cork.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * API error description used to provide useful information about an error to
 * an API consumer.
 */
public class KeenvilApiError {

  // TODO(mario): Externalize this configuration.
  private static final int MAX_STACK_LINES = 10;

  private int httpStatus;

  private String code;

  private String title;

  private String detail;

  private String source;

  private String module;

  private String uri;

  private String httpMethod;

  private String hostName;
 
  private String localHostName;

  /**
   * Id del error: va en la respuesta y en la linea de log "Platform Error", para encontrar
   * el log (con source y localHostName) a partir de lo que reporta el cliente.
   */
  private String errorId = UUID.randomUUID().toString();

  /** HTTP Status code for this error.
   * @return the HTTP status code.
   */
  public int getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(int theHttpStatus) {
    httpStatus = theHttpStatus;
  }

  /** Internal Platform error code.
   * @return error code.
   */
  public String getCode() {
    return code;
  }

  public void setCode(String theCode) {
    code = theCode;
  }

  /** Human readable error title.
   * @return error title.
   */
  public String getTitle() {
    return title;
  }

  public void setTitle(String theTitle) {
    title = theTitle;
  }

  /** Module where the error occur.
   * @return module name.
   */
  public String getModule() {
    return module;
  }

  public void setModule(String theModule) {
    module = theModule;
  }

  /** Brief error description, intended to be human readable. 
   * @return error description.
   */
  public String getDetail() {
    return detail;
  }

  public void setDetail(String theDetail) {
    detail = theDetail;
  }

  /** URI where the error occurs.
   * @return the URI.
   */
  public String getUri() {
    return uri;
  }

  public void setUri(String anUri) {
    uri = anUri;
  }

  /** Call Method. 
   * @return the method call.
   */
  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String theHttpMethod) {
    httpMethod = theHttpMethod;
  }

  /** Host name.
   * @return the host name.
   */
  public String getHostName() {
    return hostName;
  }

  public void setHostName(String theHostName) {
    hostName = theHostName;
  }

  /** Local host name.
   * @return the local host name.
   */
  /** Solo para el log: la IP/nombre interno del pod no se expone en la respuesta (P0-SEC-13). */
  @JsonIgnore
  public String getLocalHostName() {
    return localHostName;
  }

  public void setLocalHostName(String theLocalHostName) {
    localHostName = theLocalHostName;
  }

  /** Stack trace of the exception which originate, if applicable.
   * @return stack trace.
   */
  /** Solo para el log: el stack trace no se expone en la respuesta (P0-SEC-13). */
  @JsonIgnore
  public String getSource() {
    return source;
  }

  public void setSource(String theSource) {
    source = theSource;
  }

  public String getErrorId() {
    return errorId;
  }

  public void setErrorId(String theErrorId) {
    errorId = theErrorId;
  }

  @Override
  public String toString() {
    return String.format("errorId: %s, httpStatus: %s, code: %s, title: %s, detail: %s, "
        + "source: %s, module: %s, uri: %s, httpMethod: %s, hostName: %s, "
        + "localHostName: %s", errorId, String.valueOf(httpStatus), code, title, detail,
        source, module, uri, httpMethod, hostName, localHostName);
  }

  public static class KeenvilApiErrorBuilder {

    private KeenvilApiError error = new KeenvilApiError();

    public KeenvilApiErrorBuilder() { }

    public KeenvilApiErrorBuilder httpStatus(int theHttp) {
      error.setHttpStatus(theHttp);
      return this;
    }
 
    public KeenvilApiErrorBuilder code(String theCode) {
      error.setCode(theCode);
      return this;
    }

    public KeenvilApiErrorBuilder title(String theTitle) {
      error.setTitle(theTitle);
      return this;
    }

    public KeenvilApiErrorBuilder detail(String theDetil) {
      error.setDetail(theDetil);
      return this;
    }

    public KeenvilApiErrorBuilder source(Exception exception) {
      error.setSource(buildStringTrace(exception.getStackTrace()));
      return this;
    }

    public KeenvilApiErrorBuilder module(String theModule) {
      error.setModule(theModule);
      return this;
    }

    public KeenvilApiErrorBuilder request(HttpServletRequest request) {
      error.requestInformation(request);
      return this;
    }

    public KeenvilApiError build() {
      return error;
    }
  }

  private void requestInformation(HttpServletRequest request) {
    if (request != null) {
      setHostName(request.getServerName());
      setLocalHostName(request.getLocalName());
      setHttpMethod(request.getMethod());
      setUri(request.getRequestURI());
    }
  }

  private static String buildStringTrace(StackTraceElement []elements) {
    if (elements != null && elements.length > 0) {
      StringBuilder builder = new StringBuilder();
      int length = elements.length;
      for (int t = 0; t < length && t < MAX_STACK_LINES; t++) {
        builder.append(String.format("%s:%s:%s(%s) ",
            elements[t].getClassName(),
            elements[t].getMethodName(),
            elements[t].getLineNumber(),
            elements[t].getFileName()));
      }
      return builder.toString();
    }
    return "";
  }
}
