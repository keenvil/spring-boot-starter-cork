package com.keenvil.cork.totp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Time Base One-Time-Password Auto Configuration.
 * 
 * <p>Exposes {@link TotpSecretKeyGenerator} to generate secure random secret
 * keys used to generate TOTP, and {@link TotpAuthenticator} to authorize
 * generated TOTP.</p>
 */
@Configuration
@EnableConfigurationProperties({ TotpConfigurationProperties.class})
public class TotpAutoConfiguration {

  @Autowired
  private TotpConfigurationProperties properties;

  @Bean
  public TotpSecretKeyGenerator totpSecretKeyGenerator() {
    return new TotpSecretKeyGenerator(properties);
  }

  @Bean
  public TotpAuthenticator totpAuthenticator() {
    return new TotpAuthenticator(properties);
  }
}
