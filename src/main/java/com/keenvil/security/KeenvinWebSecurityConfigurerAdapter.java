package com.keenvil.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration
  .WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication
  .UsernamePasswordAuthenticationFilter;

import com.keenvil.security.jwt.JwtAuthenticationEntryPoint;
import com.keenvil.security.jwt.JwtAuthenticationFilter;
import com.keenvil.security.jwt.JwtService;

public class KeenvinWebSecurityConfigurerAdapter
    extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtService jwtService;

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
          new JwtAuthenticationFilter(jwtService);
      filter.setAuthenticationManager(authenticationManagerBean());
      return filter;
  }
}
