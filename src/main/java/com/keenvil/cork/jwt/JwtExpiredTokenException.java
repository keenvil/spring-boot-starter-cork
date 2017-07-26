package com.keenvil.cork.jwt;

/**
 * Jwt Expired Token Exception.
 */
public class JwtExpiredTokenException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public JwtExpiredTokenException(final String meesage) {
    super(meesage);
  }

  public JwtExpiredTokenException(final String meesage,
      final Throwable throwable) {
    super(meesage, throwable);
  }
}
