package io.mycommunity.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration
  .EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration
  .EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration
  .WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication
  .UsernamePasswordAuthenticationFilter;

import io.mycommunity.security.filter.JwtAuthenticationEntryPoint;
import io.mycommunity.security.filter.JwtAuthenticationFilter;
import io.mycommunity.security.service.JwtService;
import io.mycommunity.security.service.PlatformSecurityService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class BaseSecurityApiConfiguration extends WebSecurityConfigurerAdapter {

  @Bean
  public PlatformSecurityService securityService() {
    return new PlatformSecurityService();
  }

  @Bean
  public JwtService jwtService() {
    return new JwtService();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    JwtAuthenticationEntryPoint authenticationEntryPoint =
        new JwtAuthenticationEntryPoint();
    http
      .csrf().disable()
      .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
      .and()
      .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      .antMatchers("/",
          "/account/auth",
          "/swagger-ui.html",
          "/swagger-resources/**",
          "/webjars/springfox-swagger-ui/**",
          "/configuration/**",
          "/v2/api-docs").permitAll()
      .anyRequest()
      .authenticated()
      .and()
      .addFilterBefore(
          authenticationTokenFilterBean(),
          UsernamePasswordAuthenticationFilter.class);
  }

  private JwtAuthenticationFilter authenticationTokenFilterBean() 
      throws Exception {
      JwtAuthenticationFilter filter =
          new JwtAuthenticationFilter(jwtService());
      filter.setAuthenticationManager(authenticationManagerBean());
      return filter;
  }
}
