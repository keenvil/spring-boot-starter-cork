package com.keenvil.web.security.jwt;

import org.springframework.security.core.AuthenticationException;

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
