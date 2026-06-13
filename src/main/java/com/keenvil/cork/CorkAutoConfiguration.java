package com.keenvil.cork;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.keenvil.cork.CorkConfigurationProperties.CommunityResolverStrategy;
import com.keenvil.cork.internationalization.LocalizableAspect;
import com.keenvil.cork.security.QrService;
import com.keenvil.cork.security.ResourceSecurityService;
import com.keenvil.cork.validation.BindingResultValidationAspect;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableConfigurationProperties(CorkConfigurationProperties.class)
public class CorkAutoConfiguration {

  @Autowired
  private CorkConfigurationProperties properties;

  /**
   * Defines the Community Identifier Resolver strategy.
   * 
   * <p>This resolver will be used by another features inside Cork like
   * {@link ResourceSecurityService} and
   * {@link CurrentTenantIdentifierResolver}</p>
   * 
   * @return resolver.
   */
  @Bean
  public CommunityIdentifierResolver cummunityIdentifierResolver() {
    CommunityResolverStrategy resolver = properties.getCommunityResolver();
    if (CommunityResolverStrategy.URL.equals(resolver)) {
      return new UrlPathVariableCommunityResolver();
    } else {
      return new RequestAttributeCommunityResolver();
    }    
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigIn() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public LocalizableAspect localizableAspect() {
    return new LocalizableAspect();
  }

  @Bean
  public BindingResultValidationAspect bindingResultValidationAspect() {
    return new BindingResultValidationAspect();
  } 

  @Bean
  public QrService qrService() {
    return new QrService();
  }

  @Bean
  public HandlerExceptionResolver sentryExceptionResolver() {
    return new io.sentry.spring.jakarta.SentryExceptionResolver();
  }

  @Bean
  public ServletContextInitializer sentryServletContextInitializer() {
    return new io.sentry.spring.jakarta.SentryServletContextInitializer();
  }
}
