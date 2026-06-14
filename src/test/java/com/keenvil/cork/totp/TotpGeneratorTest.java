package com.keenvil.cork.totp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TotpGeneratorTest {

  private TotpAuthenticator authenticator;
  private TotpSecretKeyGenerator generator;

  @BeforeEach
  public void setUp() {
    TotpConfigurationProperties configuration =
        new TotpConfigurationProperties(80, 5, 4, 3, 6.0, 30000L);
    authenticator = new TotpAuthenticator(configuration);
    generator = new TotpSecretKeyGenerator(configuration);
  }

  @Test
  public void generateAndAuthorize() {
    String secret = generator.generateKey();
    int verificationCode = authenticator.next(secret);
    assertTrue(verificationCode > 0);

    assertTrue(authenticator.authorize(secret, verificationCode));
  }

  @Test
  public void generateAndAuthorizeWithInvalidSecrete() {
    String secret = generator.generateKey();
    int verificationCode = authenticator.next(secret);
    assertTrue(authenticator.authorize(secret, verificationCode));

    String anotherSecret = generator.generateKey();
    assertFalse(authenticator.authorize(anotherSecret, verificationCode));
  }
}
