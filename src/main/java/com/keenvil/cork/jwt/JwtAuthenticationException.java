package com.keenvil.cork.jwt;

import org.springframework.security.core.AuthenticationException;

/**
 * Jwt Authentication Exception.
 */
public class JwtAuthenticationException extends AuthenticationException {

  private static final long serialVersionUID = 1L;

  public JwtAuthenticationException(final String message) {
    super(message);
  }

  public JwtAuthenticationException(final String message,
      final Throwable throwable) {
    super(message, throwable);
  }
}
