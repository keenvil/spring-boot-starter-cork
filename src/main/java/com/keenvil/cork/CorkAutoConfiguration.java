package com.keenvil.cork;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;

import com.keenvil.cork.internationalization.LocalizableAspect;
import com.keenvil.cork.multitenancy.RequestAttributeCommunityResolverHelper;
import com.keenvil.cork.multitenancy.UrlPathVariableCommunityResolverHelper;
import com.keenvil.cork.security.CommunityResolverFilter;
import com.keenvil.cork.security.ResourceSecurityService;
import com.keenvil.cork.security.jwt.JwtAuthenticationEntryPoint;
import com.keenvil.cork.security.jwt.JwtService;
import com.keenvil.cork.security.jwt.JwtTokenHolder;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class CorkAutoConfiguration {

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
  @Bean(name = "resourceSecurityService")
  public ResourceSecurityService resourceSecurityService() {
    if (communityResolver.equals("urlBased")) {
      UrlPathVariableCommunityResolverHelper helper =
          new UrlPathVariableCommunityResolverHelper();
      return new ResourceSecurityService(helper);
    }
    
    RequestAttributeCommunityResolverHelper helper =
        new RequestAttributeCommunityResolverHelper();
    return new ResourceSecurityService(helper);
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
   * TODO(mario): Review this interceptor since community header attribute
   * is deprecated and there's services which do not need JWT.
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
