package com.keenvil.autoconfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;

import com.keenvil.core.multitenant.CommunityResolverFilter;
import com.keenvil.internationalization.LocalizableAspect;
import com.keenvil.web.security.jwt.JwtAuthenticationEntryPoint;
import com.keenvil.web.security.jwt.JwtService;
import com.keenvil.web.security.jwt.JwtTokenHolder;
import com.keenvil.web.security.service.PlatformSecurityService;
import com.keenvil.web.security.service.RequestAttributeCommunityResolverHelper;
import com.keenvil.web.security.service.UrlPathVariableCommunityResolverHelper;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class KeenvilAutoConfiguration {

  @Value("${keenvil-boot-starter.community-resolver:''}")
  private String communityResolver;
  
  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigIn() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  @ConditionalOnMissingBean
  public UrlPathVariableCommunityResolverHelper
      urlPathVariableCommunityResolverHelper() {
    return new UrlPathVariableCommunityResolverHelper();
  }

  /**
   * Security service configuration.
   * 
   * @return security service.
   */
  @Bean(name = "securityService")
  public PlatformSecurityService securityService() {
    if (communityResolver.equals("urlBased")) {
      UrlPathVariableCommunityResolverHelper helper =
          new UrlPathVariableCommunityResolverHelper();
      return new PlatformSecurityService(helper);
    }
    
    RequestAttributeCommunityResolverHelper helper =
        new RequestAttributeCommunityResolverHelper();
    return new PlatformSecurityService(helper);
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

  /**
   * Feign Request Intercepter in charge of forwarding Jwt Authentication
   * Token and Community to other services calls fired by the main call.
   */
  @Bean
  @ConditionalOnMissingBean
  public RequestInterceptor requestInterceptor() {
    return new RequestInterceptor() {
      
      @Override
      public void apply(RequestTemplate template) {
        SecurityContextHolder.getContext().getAuthentication();
        template.header(JwtService.X_AUTHORIZATION, JwtTokenHolder.token());
        template.header(CommunityResolverFilter.X_COMMUNITY_ID,
            JwtTokenHolder.community());
      }
    };
  }

  @Bean
  public LocalizableAspect localizableAspect() {
    return new LocalizableAspect();
  }
}
