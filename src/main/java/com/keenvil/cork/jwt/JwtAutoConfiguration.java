package com.keenvil.cork.jwt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.keenvil.cork.CommunityIdentifierResolver;
import com.keenvil.cork.security.KeenvilWebSecurityConfigurerAdapter;
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

  @Bean
  @ConditionalOnBean(KeenvilWebSecurityConfigurerAdapter.class)
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      JwtAuthenticationEntryPoint authenticationEntryPoint,
      JwtService jwtService,
      KeenvilWebSecurityConfigurerAdapter adapter) throws Exception {

    List<String> endpoints = new ArrayList<>();
    endpoints.add("/");
    endpoints.add("/configuration/**");
    endpoints.add("/actuator/**");

    List<String> excluded = adapter.excludeFromAuthentication();
    if (excluded != null && !excluded.isEmpty()) {
      endpoints.addAll(excluded);
    }

    http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(endpoints.toArray(new String[]{})).permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtService),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Feign Request Interceptor in charge of forwarding Jwt Authentication
   * Token to other services calls fired by the main call.
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
