package com.keenvil.cork.error;

/**
 * Keenvil base exception.
 * 
 * <p>Any new exception defined by modules must extend this one.</p>
 */
public class KeenvilException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  private String code;
  
  public KeenvilException() {
  }
  
  public KeenvilException(final String message) {
    super(message);
  }
  
  public KeenvilException(final String message, String code) {
    super(message);
    this.code = code;
  }
  
  public KeenvilException(final String message, final Throwable throwable) {
    super(message, throwable);
  }
  
  public String getCode() {
    return code;
  }
}
