package com.keenvil.cork.security.jwt;

public class JwtInvalidTokenException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public JwtInvalidTokenException(final String meesage) {
    super(meesage);
  }

  public JwtInvalidTokenException(final String meesage,
      final Throwable throwable) {
    super(meesage, throwable);
  }

}
