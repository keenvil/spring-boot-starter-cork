package com.keenvil.autoconfiguration;

import com.keenvil.internationalization.LocalizableAspect;
import com.keenvil.web.security.jwt.JwtAuthenticationEntryPoint;
import com.keenvil.web.security.jwt.JwtService;
import com.keenvil.web.security.service.PlatformSecurityService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
public class KeenvilAutoConfiguration {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigIn() {
    return new PropertySourcesPlaceholderConfigurer();
  }

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

  @Bean
  @ConditionalOnMissingBean
  public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
    return new JwtAuthenticationEntryPoint();
  }

  @Bean
  public LocalizableAspect localizableAspect() {
    return new LocalizableAspect();
  }
}
