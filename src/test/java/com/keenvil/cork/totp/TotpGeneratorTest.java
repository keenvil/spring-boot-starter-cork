package com.keenvil.cork.totp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;

public class TotpGeneratorTest {

  @TestSubject
  private TotpAuthenticator authenticator;

  private TotpSecretKeyGenerator generator;

  /**
   * Set up.
   */
  @Before
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
