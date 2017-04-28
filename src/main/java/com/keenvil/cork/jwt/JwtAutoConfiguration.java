package com.keenvil.cork.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import com.keenvil.cork.CommunityIdentifierResolver;
import com.keenvil.cork.security.ResourceSecurityService;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class JwtAutoConfiguration {

  @Autowired
  private CommunityIdentifierResolver communityIdentifierResolver;
  
  @Bean
  @ConditionalOnMissingBean
  public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
    return new JwtAuthenticationEntryPoint();
  }

  /**
   * Security service configuration.
   * 
   * @return security service.
   */
  @Bean(name = "resourceSecurityService")
  public ResourceSecurityService resourceSecurityService() {
    return new ResourceSecurityService(communityIdentifierResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  public JwtService jwtService() {
    return new JwtService();
  }

  /**
   * Feign Request Intercepter in charge of forwarding Jwt Authentication
   * Token to other services calls fired by the main call.
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
      }
    };
  }
}
