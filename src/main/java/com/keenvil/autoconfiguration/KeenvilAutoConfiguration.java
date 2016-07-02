package com.keenvil.autoconfiguration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.keenvil.security.jwt.JwtService;
import com.keenvil.security.service.PlatformSecurityService;

@Configuration
public class KeenvilAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public PlatformSecurityService securityService() {
    return new PlatformSecurityService();
  }

  @Bean
  @ConditionalOnMissingBean
  public JwtService jwtService() {
    return new JwtService();
  }
}
