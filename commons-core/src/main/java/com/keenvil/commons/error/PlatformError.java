package com.keenvil.commons.error;

import javax.servlet.http.HttpServletRequest;

/** Represents a general error description used to provide useful information
 * about an error to the API consumer.
 */
public class PlatformError {

  private static final int MAX_STACK_LINES = 5;

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

  /** HTTP Status code for this error.
   * @return the HTTP status code.
   */
  public int getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(int aHttpStatus) {
    httpStatus = aHttpStatus;
  }

  /** Internal Platform error code.
   * @return error code.
   */
  public String getCode() {
    return code;
  }

  public void setCode(String aCode) {
    code = aCode;
  }

  /** Human readable error title.
   * @return error title.
   */
  public String getTitle() {
    return title;
  }

  public void setTitle(String aTitle) {
    title = aTitle;
  }

  /** Module where the error occur.
   * @return module name.
   */
  public String getModule() {
    return module;
  }

  public void setModule(String aModule) {
    module = aModule;
  }

  /** Brief error description, intended to be human readable. 
   * @return error description.
   */
  public String getDetail() {
    return detail;
  }

  public void setDetail(String aDetail) {
    detail = aDetail;
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

  public void setHttpMethod(String aHttpMethod) {
    httpMethod = aHttpMethod;
  }

  /** Host name.
   * @return the host name.
   */
  public String getHostName() {
    return hostName;
  }

  public void setHostName(String aHostName) {
    hostName = aHostName;
  }

  /** Local host name.
   * @return the local host name.
   */
  public String getLocalHostName() {
    return localHostName;
  }

  public void setLocalHostName(String aLocalHostName) {
    localHostName = aLocalHostName;
  }

  /** Stack trace of the exception which originate, if applicable.
   * @return stack trace.
   */
  public String getSource() {
    return source;
  }

  public void setSource(String aSource) {
    source = aSource;
  }

  @Override
  public String toString() {
    return String.format("httpStatus: %s, code: %s, title: %s, detail: %s, "
        + "source: %s, module: %s, uri: %s, httpMethod: %s, hostName: %s, "
        + "localHostName: %s", String.valueOf(httpStatus), code, title, detail,
        source, module, uri, httpMethod, hostName, localHostName);
  }

  public static class PlatformErrorBuilder {

    private PlatformError error = new PlatformError();

    public PlatformErrorBuilder() { }

    public PlatformErrorBuilder httpStatus(int aHttpStatus) {
      error.setHttpStatus(aHttpStatus);
      return this;
    }
 
    public PlatformErrorBuilder code(String aCode) {
      error.setCode(aCode);
      return this;
    }

    public PlatformErrorBuilder title(String aTitle) {
      error.setTitle(aTitle);
      return this;
    }

    public PlatformErrorBuilder detail(String aDetail) {
      error.setDetail(aDetail);
      return this;
    }

    public PlatformErrorBuilder source(Exception exception) {
      error.setSource(buildStringTrace(exception.getStackTrace()));
      return this;
    }

    public PlatformErrorBuilder module(String aModule) {
      error.setModule(aModule);
      return this;
    }

    public PlatformErrorBuilder request(HttpServletRequest request) {
      error.requestInformation(request);
      return this;
    }

    public PlatformError build() {
      return error;
    }
  }

  private void requestInformation(HttpServletRequest request) {
    if(request != null) {
      setHostName(request.getServerName());
      setLocalHostName(request.getLocalName());
      setHttpMethod(request.getMethod());
      setUri(request.getRequestURI());
    }
  }

  private static String buildStringTrace(StackTraceElement []elements) {
    if(elements != null && elements.length > 0) {
      StringBuilder builder = new StringBuilder();
      int length = elements.length;
      for(int t=0; t < length && t < MAX_STACK_LINES; t++) {
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
