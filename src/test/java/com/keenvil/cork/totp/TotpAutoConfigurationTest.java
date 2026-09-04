package com.keenvil.cork.totp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Boots a real Spring context with {@link TotpAutoConfiguration} active,
 * with NO {@code keenvil.cork.totp.*} property configured -- matching every
 * real consumer of this module (no repo in the org sets any of them,
 * confirmed by grep).
 *
 * <p>This is deliberately the setup that used to fail: {@link
 * TotpConfigurationProperties} combined {@code @ConfigurationProperties}
 * with a single parameterized constructor whose {@code @Value(...:default)}
 * annotations supplied the actual defaults (secretBits=80, etc). Boot 3's
 * automatic constructor-binding for {@code @EnableConfigurationProperties}
 * beans with a single non-default constructor resolves parameters via its
 * own {@code Binder} and does NOT evaluate {@code @Value} SpEL defaults --
 * any property nobody sets silently binds to Java's default (0 / 0.0)
 * instead. That produced {@code secretBits=0}, so {@link
 * TotpSecretKeyGenerator#generateKey()} built a zero-length secret, which
 * failed downstream with "Secret cannot be empty" the first time a real
 * request exercised it (crowd-api, creating a visitor invitation).</p>
 *
 * <p>Every existing test in this module constructed {@link
 * TotpConfigurationProperties} manually with {@code new}, which always uses
 * plain Java field initializers regardless of Spring's binding behavior --
 * that is exactly why this never caught the regression. Only a real
 * Spring-bound context exercises the bug.</p>
 */
@SpringBootTest(classes = TotpAutoConfigurationTest.TestApp.class)
class TotpAutoConfigurationTest {

  @Autowired
  private TotpSecretKeyGenerator secretKeyGenerator;

  @Test
  void defaultsAreAppliedWhenNothingIsConfigured() {
    String secret = secretKeyGenerator.generateKey();

    assertThat(secret, is(notNullValue()));
    assertThat(secret.isEmpty(), is(false));
    // secretBits default (80) / 8 = 10 bytes -> 16 chars Base32-encoded.
    assertThat(secret.length(), is(16));
  }

  @Configuration
  @Import(TotpAutoConfiguration.class)
  static class TestApp {
  }
}
