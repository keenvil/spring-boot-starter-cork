package com.keenvil.cork.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/** Boot 4: @EnableJwtSecurity levanta el contexto y @PreAuthorize se sigue aplicando. */
class EnableJwtSecurityTest {

  @Configuration
  @EnableJwtSecurity
  static class Config {
    @Bean
    Guarded guarded() {
      return new Guarded();
    }
  }

  static class Guarded {
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
      return "ok";
    }
  }

  @Test
  void preAuthorizeSigueActivo() {
    new WebApplicationContextRunner().withUserConfiguration(Config.class).run(ctx -> {
      assertThat(ctx).hasNotFailed();
      Guarded guarded = ctx.getBean(Guarded.class);
      try {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("u", "p", "ROLE_USER"));
        assertThrows(AccessDeniedException.class, guarded::admin);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("u", "p", "ROLE_ADMIN"));
        assertThat(guarded.admin()).isEqualTo("ok");
      } finally {
        SecurityContextHolder.clearContext();
      }
    });
  }
}
