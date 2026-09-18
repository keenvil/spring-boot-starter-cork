package com.keenvil.cork.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.keenvil.cork.jwt.JwtAuthenticationEntryPoint;
import com.keenvil.cork.jwt.JwtAuthenticationFilter;
import com.keenvil.cork.jwt.JwtService;

/**
 * Base security configuration shared across Keenvil applications.
 */
public class KeenvilWebSecurityConfigurerAdapter {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private JwtAuthenticationEntryPoint authenticationEntryPoint;

  /**
   * Defines the main security filter chain.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    List<String> endpoints = new ArrayList<>();
    endpoints.add("/");
    endpoints.add("/configuration/**");
    endpoints.add("/actuator/**");

    List<String> excluded = excludeFromAuthentication();
    if (excluded != null && !excluded.isEmpty()) {
      endpoints.addAll(excluded);
    }

    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
    filter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));

    http.cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(endpoints.toArray(new String[0])).permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Gets list of endpoints that must be excluded from authentication.
   *
   * @return The list of endpoints in the form of Ant matchers.
   */
  @SuppressWarnings("unchecked")
  public List<String> excludeFromAuthentication() {
    return Collections.EMPTY_LIST;
  }
}
