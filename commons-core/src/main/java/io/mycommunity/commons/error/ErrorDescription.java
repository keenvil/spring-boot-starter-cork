package io.mycommunity.commons.error;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/** Represents a general error description used to provide useful information
 * about an error to the API consumer.
 */
public class ErrorDescription {

  private static final int MAX_STACK_LINES = 5;

  private int code;
  private String module;
  private String description;
  private List<BaseError> validationErrors;
  private String uri;
  private String httpMethod;
  private String hostName;
  private String localHostName;
  private String trace;


  /** Error code that identifies univocally the error across the platform.
   */
  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  /** Module where the error occur.
   * @return module name.
   */
  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  /** Brief error description, intended to be human readable. 
   * @return error description.
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /** List of validation errors.
   * return validation errors.
   */
  public List<BaseError> getValidationErrors() {
    return validationErrors;
  }

  public void setValidationErrors(List<BaseError> someValidationErrors) {
    validationErrors = someValidationErrors;
  }

  /** URI where the error occurs.
   * @return the URI.
   */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  /** Call Method. 
   * @return the method call.
   */
  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  /** Host name.
   * @return the host name.
   */
  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  /** Local host name.
   * @return the local host name.
   */
  public String getLocalHostName() {
    return localHostName;
  }

  public void setLocalHostName(String localHostName) {
    this.localHostName = localHostName;
  }

  /** Stack trace of the exception which originate, if applicable.
   * @return stack trace.
   */
  public String getTrace() {
    return trace;
  }

  public void setTrace(String trace) {
    this.trace = trace;
  }

  public static class ErrorDescriptionBuilder {
    private ErrorDescription error = new ErrorDescription();

    public ErrorDescriptionBuilder() { }

    public ErrorDescriptionBuilder code(int aCode) {
      error.setCode(aCode);
      return this;
    }

    public ErrorDescriptionBuilder module(String aModule) {
      error.setModule(aModule);
      return this;
    }

    public ErrorDescriptionBuilder request(HttpServletRequest request) {
      error.requestInformation(request);
      return this;
    }

    public ErrorDescriptionBuilder exception(Exception exception) {
      error.setDescription(exception.getMessage());
      error.setTrace(buildStringTrace(exception.getStackTrace()));
      return this;
    }

    public ErrorDescription build() {
      return error;
    }

    public ErrorDescriptionBuilder validations(
        final List<BaseError> validationErrors) {
      error.setValidationErrors(validationErrors);
      return this;
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
