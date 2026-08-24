package com.keenvil.cork.jwt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.ServletException;

public class JwtAuthenticationFilterTest {

  private JwtService jwtService = new JwtService();
  private JwtAuthenticationFilter filter =
      new JwtAuthenticationFilter(jwtService);

  @BeforeEach
  public void setUp() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  public void emptyAuthenticationParameter()
      throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(JwtService.X_AUTHORIZATION, "");
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    Authentication context =
        SecurityContextHolder.getContext().getAuthentication();
    assertThat(context, nullValue());
  }

  @Test
  public void invalidAuthenticationParameter()
      throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(JwtService.X_AUTHORIZATION, "invalid");
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);
    assertThat(SecurityContextHolder.getContext().getAuthentication(),
        nullValue());
  }

  @Test
  public void doFilterOk() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(JwtService.X_AUTHORIZATION,
        jwtService.generate("1","Joe", "Average", "B-52", "user@keenvil.com",
            Collections.emptySet()));
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    Authentication context =
        SecurityContextHolder.getContext().getAuthentication();
    assertThat(context, notNullValue());
    assertThat(context.getName(), is("user@keenvil.com"));
  }
}
