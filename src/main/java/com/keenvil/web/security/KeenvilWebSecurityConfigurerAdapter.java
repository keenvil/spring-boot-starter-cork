package com.keenvil.web.security;

import com.keenvil.web.security.jwt.JwtAuthenticationEntryPoint;
import com.keenvil.web.security.jwt.JwtAuthenticationFilter;
import com.keenvil.web.security.jwt.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class KeenvilWebSecurityConfigurerAdapter
    extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private JwtAuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
        .and()
        .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers("/",
          "/account/auth",
          "/residents/{\\d+}/validate",
          "/swagger-ui.html",
          "/swagger-resources/**",
          "/webjars/springfox-swagger-ui/**",
          "/configuration/**",
          "/health",
          "/info",
          "/status",
          "/v2/api-docs").permitAll()
          .anyRequest()
          .authenticated()
          .and()
          .addFilterBefore(
          authenticationTokenFilter(),
          UsernamePasswordAuthenticationFilter.class);
  }

  private JwtAuthenticationFilter authenticationTokenFilter() 
      throws Exception {
    JwtAuthenticationFilter filter =
        new JwtAuthenticationFilter(jwtService);
    filter.setAuthenticationManager(authenticationManagerBean());
    return filter;
  }
}
