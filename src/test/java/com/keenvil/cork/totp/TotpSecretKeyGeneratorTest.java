package com.keenvil.cork.totp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TotpSecretKeyGeneratorTest {

  private TotpSecretKeyGenerator generator;

  @BeforeEach
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
