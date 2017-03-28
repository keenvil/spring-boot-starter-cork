package com.keenvil.web.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.keenvil.web.security.jwt.JwtAuthenticationEntryPoint;
import com.keenvil.web.security.jwt.JwtAuthenticationFilter;
import com.keenvil.web.security.jwt.JwtService;

public class KeenvilWebSecurityConfigurerAdapter
    extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private JwtAuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    List<String> endpoints = new ArrayList<String>();
    endpoints.add("/");
    endpoints.add("/account/auth");
    endpoints.add("/account/validate");
    endpoints.add("/configuration/**");
    endpoints.add("/health");
    endpoints.add("/info");
    endpoints.add("/status");
    
    String notSecurized = getNotSecurizedEndpoint();
    if (notSecurized != null && !notSecurized.isEmpty()) {
      endpoints.add(notSecurized);
    }

    http.csrf().disable()
        .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers(endpoints.toArray(new String[]{}))
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .addFilterBefore(authenticationTokenFilter(),
          UsernamePasswordAuthenticationFilter.class);
  }

  private JwtAuthenticationFilter authenticationTokenFilter() 
      throws Exception {
    JwtAuthenticationFilter filter =
        new JwtAuthenticationFilter(jwtService);
    filter.setAuthenticationManager(authenticationManagerBean());
    return filter;
  }

  /**
   * Gets the not securized end points for a securized module.
   * 
   * @return securized end points as AntMatchers.
   */
  public String getNotSecurizedEndpoint() {
    return "";
  }
}
