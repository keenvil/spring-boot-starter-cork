package com.keenvil.autoconfiguration;

import com.keenvil.web.security.jwt.JwtService;
import com.keenvil.web.security.service.PlatformSecurityService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeenvilAutoConfiguration {

  @Bean(name = "securityService")
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
