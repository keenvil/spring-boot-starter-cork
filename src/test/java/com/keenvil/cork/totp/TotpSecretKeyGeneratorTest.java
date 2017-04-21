package com.keenvil.cork.totp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;

public class TotpSecretKeyGeneratorTest {

  @TestSubject
  private TotpSecretKeyGenerator generator;

  /**
   * Set up.
   */
  @Before
  public void setUp() {
    TotpConfigurationProperties configuration =
        new TotpConfigurationProperties(80, 5, 4, 3, 6.0, 30000L);
    generator = new TotpSecretKeyGenerator(configuration);
  }

  @Test
  public void generate() {
    String key = generator.generateKey();
    assertThat(key, notNullValue());
    assertThat(key.length(), is(16));
  }
}
